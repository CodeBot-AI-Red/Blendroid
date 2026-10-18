package com.blendroid.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blendroid.app.engine.ai.McpBridge
import com.blendroid.app.engine.ai.NpmPluginManager
import com.blendroid.app.engine.ai.ScriptRunner
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.ui.theme.DarkCardBorder
import com.blendroid.app.ui.theme.DarkSurface
import com.blendroid.app.ui.theme.DarkSurfaceVariant
import com.blendroid.app.ui.theme.PrimaryIndigo
import com.blendroid.app.ui.theme.PrimaryIndigoLight
import com.blendroid.app.ui.theme.StatusGreen
import com.blendroid.app.ui.theme.TextMuted
import com.blendroid.app.ui.theme.TextPrimary
import com.blendroid.app.ui.theme.TextSecondary

@Composable
fun AiAssistantDrawer(
    scene: SceneGraph,
    mcpBridge: McpBridge,
    npmPluginManager: NpmPluginManager,
    scriptRunner: ScriptRunner,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("ASSISTANT") } // ASSISTANT, MCP, NPM, LOGS
    var promptInput by remember { mutableStateOf("") }
    var newNpmPackageInput by remember { mutableStateOf("") }

    val logsList = remember {
        mutableStateListOf(
            "Sistema MCP e IAs inicializado.",
            "Pronto para aceitar conexões e comandos."
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Painel de IA, MCP & NPM Extensions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onCloseDrawer, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("ASSISTANT", "IA Prompt / Gerador"),
                    Pair("MCP", "Conexão MCP"),
                    Pair("NPM", "Pacotes NPM"),
                    Pair("LOGS", "Console / Logs")
                ).forEach { (tabKey, label) ->
                    val isSel = selectedTab == tabKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF8B5CF6) else DarkSurfaceVariant)
                            .clickable { selectedTab = tabKey }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = if (isSel) Color.White else TextSecondary
                        )
                    }
                }
            }

            Divider(color = DarkSurfaceVariant, modifier = Modifier.padding(vertical = 6.dp))

            // Tab Content
            when (selectedTab) {
                "ASSISTANT" -> {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Text("Prompt de Controle 3D por IA:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = { Text("Ex: Crie uma cidade 3D com prédio azul e extrude...", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant,
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    if (promptInput.isNotBlank()) {
                                        val result = scriptRunner.executeNaturalLanguageCommand(promptInput, scene)
                                        logsList.add(0, "Prompt: '$promptInput' -> $result")
                                        promptInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.height(50.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ações Rápidas por IA:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cidade 3D", "Floresta 3D", "Robô Estilizado", "Subdividir e Extrudar").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryIndigo.copy(alpha = 0.25f))
                                        .clickable {
                                            val result = scriptRunner.executeNaturalLanguageCommand(preset, scene)
                                            logsList.add(0, "Ação '$preset': $result")
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(preset, fontSize = 11.sp, color = PrimaryIndigoLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "MCP" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Servidores MCP (Model Context Protocol):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))

                        mcpBridge.getConnections().forEach { config ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(config.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${config.serverUrl} | Status: ${config.statusText}", fontSize = 10.sp, color = if (config.isConnected) StatusGreen else TextMuted)
                                }

                                Button(
                                    onClick = {
                                        mcpBridge.toggleConnection(config)
                                        logsList.add(0, "MCP: Servidor '${config.name}' alterado.")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (config.isConnected) Color(0xFFEF4444) else StatusGreen)
                                ) {
                                    Text(if (config.isConnected) "Desconectar" else "Conectar", fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ferramentas MCP Registradas (${mcpBridge.getTools().size}):", fontSize = 11.sp, color = TextSecondary)
                        mcpBridge.getTools().forEach { tool ->
                            Text("• ${tool.name}: ${tool.description}", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }

                "NPM" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newNpmPackageInput,
                                onValueChange = { newNpmPackageInput = it },
                                placeholder = { Text("Instalar pacote ex: @ai-3d/mesh-gen", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant,
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = DarkCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    if (newNpmPackageInput.isNotBlank()) {
                                        val res = npmPluginManager.installPackage(newNpmPackageInput)
                                        logsList.add(0, res)
                                        newNpmPackageInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                            ) {
                                Text("Instalar", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(npmPluginManager.getInstalledPackages()) { pkg ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${pkg.packageName} v${pkg.version}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(if (pkg.isEnabled) "Ativo" else "Inativo", fontSize = 10.sp, color = if (pkg.isEnabled) StatusGreen else TextMuted)
                                    }

                                    Text(pkg.description, fontSize = 9.sp, color = TextSecondary)

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        pkg.actions.forEach { action ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PrimaryIndigo.copy(alpha = 0.3f))
                                                    .clickable {
                                                        val res = npmPluginManager.executePackageAction(pkg.packageName, action, scene)
                                                        logsList.add(0, "NPM [${pkg.packageName}]: $res")
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(action, fontSize = 9.sp, color = PrimaryIndigoLight)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "LOGS" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        items(logsList) { log ->
                            Text("> $log", fontSize = 10.sp, color = PrimaryIndigoLight)
                        }
                    }
                }
            }
        }
    }
}
