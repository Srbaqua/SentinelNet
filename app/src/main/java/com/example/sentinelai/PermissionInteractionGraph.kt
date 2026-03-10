package com.example.sentinelai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
data class PermissionNode(
    val name: String
)

data class PermissionEdge(
    val from: String,
    val to: String,
    val risk: String
)

data class PermissionGraph(
    val nodes: List<PermissionNode>,
    val edges: List<PermissionEdge>
)

object PermissionGraphBuilder {

    fun buildGraph(permissions: List<String>): PermissionGraph {

        val nodes = mutableListOf<PermissionNode>()
        val edges = mutableListOf<PermissionEdge>()

        fun has(p: String) = permissions.any { it.contains(p) }

        if (has("CAMERA")) nodes.add(PermissionNode("Camera"))
        if (has("LOCATION")) nodes.add(PermissionNode("Location"))
        if (has("CONTACT")) nodes.add(PermissionNode("Contacts"))
        if (has("AUDIO")) nodes.add(PermissionNode("Microphone"))
        if (has("INTERNET")) nodes.add(PermissionNode("Internet"))

        if (has("CAMERA") && has("INTERNET")) {
            edges.add(
                PermissionEdge(
                    "Camera",
                    "Internet",
                    "Media Upload Risk"
                )
            )
        }

        if (has("LOCATION") && has("INTERNET")) {
            edges.add(
                PermissionEdge(
                    "Location",
                    "Internet",
                    "Tracking Risk"
                )
            )
        }

        if (has("CONTACT") && has("INTERNET")) {
            edges.add(
                PermissionEdge(
                    "Contacts",
                    "Internet",
                    "Data Leakage Risk"
                )
            )
        }

        if (has("AUDIO") && has("INTERNET")) {
            edges.add(
                PermissionEdge(
                    "Microphone",
                    "Internet",
                    "Audio Upload Risk"
                )
            )
        }

        return PermissionGraph(nodes, edges)
    }
}

@Composable
fun PermissionInteractionGraph(permissions: List<String>) {

    val graph = remember {
        PermissionGraphBuilder.buildGraph(permissions)
    }

    Column {

        Text(
            text = "Permission Interaction Graph",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        graph.edges.forEach { edge ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        text = "${edge.from} ➜ ${edge.to}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = edge.risk,
                        color = Color.Red
                    )
                }
            }
        }

        if (graph.edges.isEmpty()) {
            Text("No risky permission interactions detected")
        }
    }
}