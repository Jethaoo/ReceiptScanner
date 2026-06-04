package com.example.receiptscanner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.receiptscanner.navigation.MainTab
import com.example.receiptscanner.ui.theme.GlassBackgroundDark
import com.example.receiptscanner.ui.theme.GlassSurfaceDark
import com.example.receiptscanner.ui.theme.GlassSurfaceLight

@Composable
fun MainBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = glassSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItem(
            tab = MainTab.Home,
            label = "Home",
            icon = Icons.Default.Home,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
        bottomNavItem(
            tab = MainTab.Scan,
            label = "Scan",
            icon = Icons.Default.CameraAlt,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
        bottomNavItem(
            tab = MainTab.Receipts,
            label = "Receipts",
            icon = Icons.Default.Receipt,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
        bottomNavItem(
            tab = MainTab.Settings,
            label = "Settings",
            icon = Icons.Default.Settings,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
    }
}

@Composable
private fun RowScope.bottomNavItem(
    tab: MainTab,
    label: String,
    icon: ImageVector,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    val selected = selectedTab == tab
    NavigationBarItem(
        selected = selected,
        onClick = { onTabSelected(tab) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun glassSurfaceColor(): Color {
    val isDark = MaterialTheme.colorScheme.background == GlassBackgroundDark
    return if (isDark) GlassSurfaceDark else GlassSurfaceLight
}
