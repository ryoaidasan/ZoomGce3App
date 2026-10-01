package com.example.guitarlabandroid

import android.content.Context
import android.media.midi.*
import android.os.Handler
import android.os.Looper
import android.util.Log

class ZoomMidiManager(private val context: Context) {
    private val TAG = "ZoomMidiManager"
    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
    private var midiDevice: MidiDevice? = null
    private var inputPort: MidiInputPort? = null
    private var outputPort: MidiOutputPort? = null

    fun connectToGce3(onConnected: (Boolean) -> Unit) {
        val infos = midiManager.devices
        val gceInfo = infos.firstOrNull { info ->
            val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: ""
            name.contains("GCE-3", ignoreCase = true) || name.contains("ZOOM", ignoreCase = true)
        }

        if (gceInfo == null) {
            Log.e(TAG, "GCE-3 not found")
            onConnected(false)
            return
        }

        midiManager.openDevice(gceInfo, { device ->
            midiDevice = device
            inputPort = device.openInputPort(0)
            outputPort = device.openOutputPort(0)

            outputPort?.connect(object : MidiReceiver() {
                override fun onSend(data: ByteArray?, offset: Int, count: Int, timestamp: Long) {
                    if (data == null) return
                    val received = data.copyOfRange(offset, offset + count)
                    Log.d(TAG, "Received MIDI: " + received.joinToString(" ") { "%02X".format(it) })
                }
            })

            // Handshake
            val identityRequest = byteArrayOf(0xF0.toByte(), 0x7E.toByte(), 0x7F.toByte(), 0x06.toByte(), 0x01.toByte(), 0xF7.toByte())
            inputPort?.send(identityRequest, 0, identityRequest.size)
            onConnected(true)
        }, Handler(Looper.getMainLooper()))
    }

    fun selectPatch(patchIndex: Int) {
        val msg = byteArrayOf(0xC0.toByte(), (patchIndex and 0x7F).toByte())
        inputPort?.send(msg, 0, msg.size)
    }

    fun sendParameterChange(paramId: Byte, value: Byte) {
        val sysEx = byteArrayOf(
            0xF0.toByte(), 0x52.toByte(), 0x00.toByte(), 0x6E.toByte(),
            0x31.toByte(), paramId, value, 0xF7.toByte()
        )
        inputPort?.send(sysEx, 0, sysEx.size)
    }

    fun close() {
        inputPort?.close()
        outputPort?.close()
        midiDevice?.close()
    }
}
