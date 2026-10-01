package com.example.guitarlabandroid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuitarLabScreen(zoomManager: ZoomMidiManager) {
    var isConnected by remember { mutableStateOf(false) }
    var selectedPatch by remember { mutableIntStateOf(0) }
    var driveGain by remember { mutableFloatStateOf(50f) }

    val defaultPatches = listOf(
        "001: Lead BGN",
        "002: Clean Echo",
        "003: Modern Crunch",
        "004: Heavy Rhythm",
        "005: Acoustic Sim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZOOM GCE-3 Controller") },
                actions = {
                    Button(onClick = {
                        zoomManager.connectToGce3 { success -> isConnected = success }
                    }) {
                        Text(if (isConnected) "Connected" else "Connect USB")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Patch Select", style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(defaultPatches) { index, patchName ->
                    ListItem(
                        headlineContent = { Text(patchName) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selectedPatch == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPatch = index
                                zoomManager.selectPatch(index)
                            }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Drive Gain: ${driveGain.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = driveGain,
                onValueChange = {
                    driveGain = it
                    zoomManager.sendParameterChange(paramId = 0x01, value = it.toInt().toByte())
                },
                valueRange = 0f..100f
            )
        }
    }
}
