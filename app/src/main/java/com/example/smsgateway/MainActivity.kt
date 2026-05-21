package com.example.smsgateway

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private var server: SmsServer? = null

    private val smsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestSmsPermission()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1A237E),
                    onPrimary = Color.White,
                    secondary = Color(0xFF3F51B5),
                    onSecondary = Color.White,
                    tertiary = Color(0xFF303F9F),
                    onTertiary = Color.White,
                    background = Color(0xFFF5F7FA),
                    surface = Color.White,
                    onSurface = Color(0xFF1A237E)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Red // Temporary for debugging
                ) {
                    val clipboardManager = LocalClipboardManager.current
                    var serverRunning by remember { mutableStateOf(false) }
                    var ipAddress by remember { mutableStateOf("Loading...") }
                    
                    LaunchedEffect(Unit) {
                        ipAddress = NetworkUtils.getLocalIpAddress()
                    }
                    
                    val port = 8080
                    val endpoint = "http://$ipAddress:$port/sendSms"

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("SMS Gateway", fontWeight = FontWeight.Bold) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFF1A237E),
                                    titleContentColor = Color.White
                                )
                            )
                        }
                    ) { padding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                StatusCard(serverRunning, "$ipAddress:$port")
                            }

                            item {
                                ActionButtons(
                                    serverRunning = serverRunning,
                                    onStart = {
                                        if (!serverRunning) {
                                            try {
                                                server = SmsServer(this@MainActivity)
                                                server?.start()
                                                serverRunning = true
                                                Toast.makeText(this@MainActivity, "Server Started", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onStop = {
                                        server?.stop()
                                        serverRunning = false
                                        Toast.makeText(this@MainActivity, "Server Stopped", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            item {
                                InfoCard(
                                    title = "API Endpoint",
                                    content = endpoint,
                                    icon = Icons.Default.Info,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(endpoint))
                                        Toast.makeText(this@MainActivity, "URL Copied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            item {
                                UsageCard()
                            }

                            item {
                                InstructionsCard()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StatusCard(isRunning: Boolean, address: String) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isRunning) Color(0xFFE8EAF6) else Color(0xFFFFEBEE)
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Server Status",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF3F51B5)
                    )
                    Text(
                        text = if (isRunning) "Running" else "Stopped",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) Color(0xFF1A237E) else Color(0xFFC62828)
                    )
                    if (isRunning) {
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF283593)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) Color(0xFF4CAF50) else Color(0xFFF44336))
                )
            }
        }
    }

    @Composable
    fun ActionButtons(
        serverRunning: Boolean,
        onStart: () -> Unit,
        onStop: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onStart,
                enabled = !serverRunning,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A237E),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = onStop,
                enabled = serverRunning,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Stop")
            }
        }
    }

    @Composable
    fun InfoCard(title: String, content: String, icon: ImageVector, onCopy: () -> Unit) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            }
        }
    }

    @Composable
    fun UsageCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Usage Example (JSON POST)", style = MaterialTheme.typography.titleSmall, color = Color(0xFF1A237E))
                Spacer(Modifier.height(8.dp))
                SelectionContainer {
                    Text(
                        text = """
                            {
                              "phone": "+911234567890",
                              "message": "Hello from SMS Gateway!"
                            }
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF283593)
                    )
                }
            }
        }
    }

    @Composable
    fun InstructionsCard() {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("How to use:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            InstructionItem("1", "Connect phone and PC to the same WiFi network.")
            InstructionItem("2", "Start the server using the button above.")
            InstructionItem("3", "Send a POST request to the API endpoint.")
        }
    }

    @Composable
    fun InstructionItem(number: String, text: String) {
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "$number.",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(24.dp)
            )
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }

    private fun requestSmsPermission() {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermissionLauncher.launch(
                Manifest.permission.SEND_SMS
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }
}