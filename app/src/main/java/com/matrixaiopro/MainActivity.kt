package com.matrixaiopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matrixaiopro.navigation.BottomNavItem
import com.matrixaiopro.ui.theme.MatrixAioProTheme
import com.matrixaiopro.editor.MatrixRichEditorEngine
import com.matrixaiopro.pinnit.MatrixPinManager
import com.matrixaiopro.keep.MatrixSyncEngine
import kotlinx.coroutines.launch

import com.matrixaiopro.data.MatrixDao
import com.matrixaiopro.data.MatrixDatabase
import com.matrixaiopro.data.Note
import com.matrixaiopro.data.FinanceTransaction
import com.matrixaiopro.data.Task
import com.matrixaiopro.data.NotificationLog
import com.matrixaiopro.ui.DrawingCanvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var editorEngine: MatrixRichEditorEngine
    private lateinit var pinManager: MatrixPinManager
    private lateinit var syncEngine: MatrixSyncEngine
    private lateinit var matrixDao: MatrixDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = MatrixDatabase.getDatabase(this)
        matrixDao = db.matrixDao()
        
        editorEngine = MatrixRichEditorEngine()
        pinManager = MatrixPinManager(this)
        syncEngine = MatrixSyncEngine(this)

        enableEdgeToEdge()
        setContent {
            MatrixAioProTheme {
                MatrixSuperAppScreen(editorEngine, pinManager, syncEngine, matrixDao)
            }
        }
    }
}

@Composable
fun MatrixSuperAppScreen(
    editorEngine: MatrixRichEditorEngine,
    pinManager: MatrixPinManager,
    syncEngine: MatrixSyncEngine,
    matrixDao: MatrixDao
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                BottomNavItem.items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Notes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Notes.route) { NotesScreen(editorEngine, matrixDao) }
            composable(BottomNavItem.Pinnit.route) { PinnitScreen(matrixDao, pinManager) }
            composable(BottomNavItem.NotifLog.route) { NotifLogScreen(matrixDao) }
            composable(BottomNavItem.Finance.route) { FinanceScreen(matrixDao) }
            composable(BottomNavItem.Drawing.route) { DrawingScreen() }
            composable(BottomNavItem.Shopping.route) { ShoppingScreen(matrixDao) }
            composable(BottomNavItem.About.route) { AboutScreen() }
        }
    }
}

@Composable
fun NotesScreen(engine: MatrixRichEditorEngine, dao: MatrixDao) {
    val scope = rememberCoroutineScope()
    val state = engine.state
    val notes by dao.getAllNotes().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matrix Notes", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = state.textFieldValue,
            onValueChange = { engine.onTextChange(it) },
            modifier = Modifier.fillMaxWidth().weight(0.4f),
            label = { Text("Write your thoughts...") },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { engine.undo() }) { Icon(Icons.Default.Undo, "Undo") }
            IconButton(onClick = { engine.redo() }) { Icon(Icons.Default.Redo, "Redo") }
            IconButton(onClick = { engine.convertToChecklist() }) { Icon(Icons.Default.List, "List") }
            Button(onClick = {
                scope.launch {
                    dao.insertNote(Note(title = "Matrix Note", content = state.textFieldValue.text))
                }
            }, modifier = Modifier.weight(1f)) {
                Text("Save to Matrix")
            }
        }

        Divider(color = MaterialTheme.colorScheme.secondary, thickness = 1.dp)

        LazyColumn(modifier = Modifier.weight(0.6f)) {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(note.content, color = Color.White)
                        Text(
                            java.text.SimpleDateFormat("dd MMM, HH:mm").format(note.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
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
        Text("Pinned Notifications", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(notes) { note ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(note.content.take(30) + "...", color = Color.White)
                    Button(onClick = { pinManager.pinNote(note.id.toInt(), "Matrix Aio Pro", note.content) }) {
                        Text("Pin")
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
        Text("Matrix Notification Log", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(log.title ?: "No Title", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(log.text ?: "", color = Color.White)
                        Text(log.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceScreen(dao: MatrixDao) {
    val transactions by dao.getAllTransactions().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matrix Finance Tracker", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f))
        }
        
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                scope.launch {
                    dao.insertTransaction(FinanceTransaction(amount = amt, category = category, note = "Matrix Transaction"))
                    amount = ""; category = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Transaction")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(transactions) { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surface).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(tx.category, color = Color.White)
                    Text("$${tx.amount}", color = if (tx.isIncome) MatrixAccent else Color.Red)
                }
            }
        }
    }
}

@Composable
fun DrawingScreen() {
    var selectedColor by remember { mutableStateOf(MatrixCyan) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matrix Canvas", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = listOf(MatrixCyan, MatrixPurple, MatrixAccent, Color.White, Color.Yellow)
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, RoundedCornerShape(20.dp))
                        .clickable { selectedColor = color }
                        .padding(2.dp)
                        .background(if (selectedColor == color) Color.Black.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(20.dp))
                )
            }
        }
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(4.dp)) {
            DrawingCanvas(selectedColor = selectedColor)
        }
    }
}

@Composable
fun ShoppingScreen(dao: MatrixDao) {
    val tasks by dao.getAllTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var newTask by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matrix Shopping List", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = newTask, onValueChange = { newTask = it }, label = { Text("New Item") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                if (newTask.isNotBlank()) {
                    scope.launch {
                        dao.insertTask(Task(description = newTask))
                        newTask = ""
                    }
                }
            }) { Text("Add") }
        }

        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surface).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = task.isCompleted, onCheckedChange = {
                        scope.launch { dao.updateTaskStatus(task.id, it) }
                    })
                    Text(task.description, color = if (task.isCompleted) Color.Gray else Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { scope.launch { dao.deleteTask(task.id) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Adb, contentDescription = null, modifier = Modifier.size(100.dp), tint = MatrixCyan)
        Spacer(modifier = Modifier.height(16.dp))
        Text("MATRIX AIO PRO", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MatrixCyan)
        Text("Version 2.0.0", color = MatrixPurple)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "The Ultimate Cyber-Super-App.\nAll your notes, finance, and creativity in one secure location.",
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(64.dp))
        Text("Powered by Matrix AI Engine", style = MaterialTheme.typography.labelSmall, color = MatrixAccent)
    }
}
