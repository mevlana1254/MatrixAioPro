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

class MainActivity : ComponentActivity() {
    private lateinit var editorEngine: MatrixRichEditorEngine
    private lateinit var pinManager: MatrixPinManager
    private lateinit var syncEngine: MatrixSyncEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Engines
        editorEngine = MatrixRichEditorEngine()
        pinManager = MatrixPinManager(this)
        syncEngine = MatrixSyncEngine(this)

        enableEdgeToEdge()
        setContent {
            MatrixAioProTheme {
                MatrixSuperAppScreen(editorEngine, pinManager, syncEngine)
            }
        }
    }
}

@Composable
fun MatrixSuperAppScreen(
    editorEngine: MatrixRichEditorEngine,
    pinManager: MatrixPinManager,
    syncEngine: MatrixSyncEngine
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                var selectedItem by remember { mutableIntStateOf(0) }
                BottomNavItem.items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item.route)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray
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
            composable(BottomNavItem.Notes.route) {
                NotesEditorScreen(editorEngine, pinManager, syncEngine)
            }
            composable(BottomNavItem.Pinnit.route) { PlaceholderScreen("Pinnit") }
            composable(BottomNavItem.NotifLog.route) { PlaceholderScreen("Notification Log") }
            composable(BottomNavItem.Finance.route) { PlaceholderScreen("Finance") }
            composable(BottomNavItem.Drawing.route) { PlaceholderScreen("Drawing") }
            composable(BottomNavItem.Shopping.route) { PlaceholderScreen("Shopping") }
            composable(BottomNavItem.About.route) { PlaceholderScreen("About") }
        }
    }
}

@Composable
fun NotesEditorScreen(
    engine: MatrixRichEditorEngine,
    pinManager: MatrixPinManager,
    syncEngine: MatrixSyncEngine
) {
    val scope = rememberCoroutineScope()
    val state = engine.state

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matrix Rich Editor", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextField(
            value = state.textFieldValue,
            onValueChange = { engine.onTextChange(it) },
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { engine.undo() }, modifier = Modifier.weight(1f)) { Text("Undo") }
            Button(onClick = { engine.redo() }, modifier = Modifier.weight(1f)) { Text("Redo") }
            Button(onClick = { engine.convertToChecklist() }, modifier = Modifier.weight(1f)) { Text("List") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { 
                pinManager.pinNote(1, "Matrix Note", state.textFieldValue.text) 
            }, modifier = Modifier.weight(1f)) { Text("Pin It") }
            
            Button(onClick = { 
                scope.launch { syncEngine.syncWithCloud() }
            }, modifier = Modifier.weight(1f)) { Text("Sync") }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Welcome to $name", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
    }
}
