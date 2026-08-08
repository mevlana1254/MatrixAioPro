package com.matrixaiopro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.matrixaiopro.data.*
import com.matrixaiopro.editor.*
import com.matrixaiopro.keep.MatrixSyncEngine
import com.matrixaiopro.navigation.BottomNavItem
import com.matrixaiopro.navigation.bottomNavItems
import com.matrixaiopro.pinnit.MatrixPinManager
import com.matrixaiopro.ui.DrawingCanvas
import com.matrixaiopro.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatrixAioProTheme {
                MatrixSuperAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixSuperAppScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { MatrixDatabase.getDatabase(context) }
    val dao = db.matrixDao()
    val editorEngine = remember { MatrixRichEditorEngine() }
    val pinManager = remember { MatrixPinManager(context) }
    val syncEngine = remember { MatrixSyncEngine(context) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Notes.route

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Logo",
                            tint = CyberNeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Matrix Aio Pro", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MatrixSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MatrixSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(CyberNeonCyan, ElectricPurple)),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Text(item.iconText, fontSize = if (isSelected) 22.sp else 18.sp) },
                        label = {
                            Text(
                                item.label,
                                color = if (isSelected) CyberNeonCyan else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        },
        containerColor = MatrixDarkBg
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Notes.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Notes.route) { NotesScreen(editorEngine, dao) }
            composable(BottomNavItem.Pinnit.route) { PinnitScreen(dao, pinManager) }
            composable(BottomNavItem.NotifLog.route) { NotifLogScreen(dao) }
            composable(BottomNavItem.Finance.route) { FinanceScreen(dao) }
            composable(BottomNavItem.Drawing.route) { DrawingScreen() }
            composable(BottomNavItem.Shopping.route) { ShoppingScreen(dao) }
            composable(BottomNavItem.About.route) { AboutScreen(syncEngine) }
        }
    }
}

@Composable
fun NotesScreen(engine: MatrixRichEditorEngine, dao: MatrixDao) {
    val scope = rememberCoroutineScope()
    val state = engine.state
    val notes by dao.getAllNotes().collectAsState(initial = emptyList())
    var title by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MatrixSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📝 Rich Editor", color = CyberNeonCyan, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { engine.undo() }) { Text("↩️") }
                        IconButton(onClick = { engine.redo() }) { Text("↪️") }
                    }
                }
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.textFieldValue,
                    onValueChange = { engine.onTextChange(it) },
                    label = { Text("Notunuzu yazın...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        textAlign = when(state.alignment) {
                            TextAlignment.CENTER -> TextAlign.Center
                            TextAlignment.END -> TextAlign.End
                            else -> TextAlign.Start
                        }
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = { engine.setAlignment(TextAlignment.START) }) { Text("⬅️") }
                    IconButton(onClick = { engine.setAlignment(TextAlignment.CENTER) }) { Text("⬌") }
                    IconButton(onClick = { engine.setAlignment(TextAlignment.END) }) { Text("➡️") }
                    Button(onClick = { engine.convertToChecklist() }, colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)) {
                        Text("☑️ Checklist", fontSize = 10.sp)
                    }
                    Button(onClick = {
                        scope.launch {
                            dao.insertNote(MatrixKeepNote(title = title, content = state.textFieldValue.text))
                            title = ""
                            engine.onTextChange(androidx.compose.ui.text.input.TextFieldValue(""))
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = CyberNeonCyan)) {
                        Text("Kaydet", color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, color = CyberNeonCyan, fontWeight = FontWeight.Bold)
                            Text(note.content, color = TextPrimary)
                        }
                        IconButton(onClick = { scope.launch { dao.deleteNote(note.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinnitScreen(dao: MatrixDao, pinManager: MatrixPinManager) {
    val notes by dao.getAllNotes().collectAsState(initial = emptyList())
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📌 Sabitlenmiş Bildirimler", style = MaterialTheme.typography.headlineSmall, color = CyberNeonCyan)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(notes) { note ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MatrixSurface, RoundedCornerShape(8.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(note.title.ifBlank { "Başlıksız" }, color = Color.White)
                    Button(onClick = { pinManager.pinNote(note.id.toInt(), note.title, note.content) }) {
                        Text("Sabitle")
                    }
                }
            }
        }
    }
}

@Composable
fun NotifLogScreen(dao: MatrixDao) {
    val logs by dao.getAllNotificationLogs().collectAsState(initial = emptyList())
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🔔 Bildirim Geçmişi", style = MaterialTheme.typography.headlineSmall, color = CyberNeonCyan)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(logs) { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MatrixSurface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(log.title ?: "Matrix App", fontWeight = FontWeight.Bold, color = CyberNeonCyan)
                        Text(log.text ?: "", color = Color.White)
                        Text(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(log.timestamp), color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceScreen(dao: MatrixDao) {
    val scope = rememberCoroutineScope()
    val txs by dao.getAllTransactions().collectAsState(initial = emptyList())
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("₺ Cüzdan & Bütçe", style = MaterialTheme.typography.headlineSmall, color = CyberNeonCyan)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Tutar") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.weight(1f))
        }
        Button(onClick = {
            scope.launch {
                dao.insertTransaction(FinanceTransaction(amount = amount.toDoubleOrNull() ?: 0.0, category = category))
                amount = ""; category = ""
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)) {
            Text("İşlem Ekle")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(txs) { tx ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MatrixSurface).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(tx.category, color = Color.White)
                    Text("${tx.amount} ₺", color = CyberNeonCyan)
                }
            }
        }
    }
}

@Composable
fun DrawingScreen() {
    var color by remember { mutableStateOf(CyberNeonCyan) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🎨 Matrix Çizim", style = MaterialTheme.typography.headlineSmall, color = CyberNeonCyan)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(CyberNeonCyan, ElectricPurple, MatrixAccent, Color.Red, Color.Yellow).forEach { c ->
                Box(modifier = Modifier.size(30.dp).background(c, RoundedCornerShape(15.dp)).clickable { color = c })
            }
        }
        DrawingCanvas(modifier = Modifier.fillMaxSize().border(1.dp, Color.Gray), selectedColor = color)
    }
}

@Composable
fun ShoppingScreen(dao: MatrixDao) {
    val scope = rememberCoroutineScope()
    val tasks by dao.getAllTasks().collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🛒 Alışveriş Listesi", style = MaterialTheme.typography.headlineSmall, color = CyberNeonCyan)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
            Button(onClick = { scope.launch { dao.insertTask(Task(description = text)); text = "" } }) { Text("Ekle") }
        }
        LazyColumn {
            items(tasks) { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = t.isCompleted, onCheckedChange = { scope.launch { dao.updateTaskStatus(t.id, it) } })
                    Text(t.description, color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { dao.deleteTask(t.id) } }) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(syncEngine: MatrixSyncEngine) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(80.dp), tint = CyberNeonCyan)
        Text("Matrix Aio Pro", style = MaterialTheme.typography.headlineLarge, color = CyberNeonCyan, fontWeight = FontWeight.Bold)
        Text("Sürüm 1.0.0-PRO", color = TextSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            scope.launch {
                syncEngine.syncWithCloud()
                Toast.makeText(context, "🔄 Google Keep & Drive Senkronizasyonu Başarılı!", Toast.LENGTH_SHORT).show()
            }
        }, colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)) {
            Text("🔄 Google Keep & Drive Eşitle")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Hepsi bir arada süper uygulama ekosistemi.", color = TextPrimary, textAlign = TextAlign.Center)
    }
}
