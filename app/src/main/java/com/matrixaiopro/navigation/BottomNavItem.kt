package com.matrixaiopro.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Notes : BottomNavItem("notes", "Notes", Icons.Default.Edit)
    object Pinnit : BottomNavItem("pinnit", "Pinned", Icons.Default.PushPin)
    object NotifLog : BottomNavItem("notif_log", "Logs", Icons.Default.History)
    object Finance : BottomNavItem("finance", "Finance", Icons.Default.AttachMoney)
    object Drawing : BottomNavItem("drawing", "Drawing", Icons.Default.Brush)
    object Shopping : BottomNavItem("shopping", "Shopping", Icons.Default.ShoppingCart)
    object About : BottomNavItem("about", "About", Icons.Default.Info)

    companion object {
        val items = listOf(Notes, Pinnit, NotifLog, Finance, Drawing, Shopping, About)
    }
}
