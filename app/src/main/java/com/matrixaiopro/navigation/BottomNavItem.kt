package com.matrixaiopro.navigation

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconText: String
) {
    object Notes : BottomNavItem("notes", "Notlar", "📝")
    object Pinnit : BottomNavItem("pinnit", "Sabitler", "📌")
    object NotifLog : BottomNavItem("notif_log", "Loglar", "🔔")
    object Finance : BottomNavItem("finance", "Finans", "₺")
    object Drawing : BottomNavItem("drawing", "Çizim", "🎨")
    object Shopping : BottomNavItem("shopping", "Market", "🛒")
    object About : BottomNavItem("about", "Hakkında", "ℹ️")
}

val bottomNavItems = listOf(
    BottomNavItem.Notes,
    BottomNavItem.Pinnit,
    BottomNavItem.NotifLog,
    BottomNavItem.Finance,
    BottomNavItem.Drawing,
    BottomNavItem.Shopping,
    BottomNavItem.About
)
