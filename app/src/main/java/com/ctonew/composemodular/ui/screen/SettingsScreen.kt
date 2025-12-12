package com.ctonew.composemodular.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctonew.composemodular.ui.theme.LocalAccentColor
import com.ctonew.composemodular.viewmodel.ThemeViewModel

/**
 * Settings screen for theme and app preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val themePreferences by themeViewModel.preferences.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Appearance section
            SettingsSection(
                title = "Appearance"
            ) {
                ThemeSettings(
                    currentAccentColor = themePreferences.accentColorArgb,
                    onAccentChanged = { newColor ->
                        themeViewModel.setAccentColor(newColor)
                    }
                )
            }
            
            Divider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Notifications section
            SettingsSection(
                title = "Notifications"
            ) {
                NotificationSettings()
            }
            
            Divider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // About section
            SettingsSection(
                title = "About"
            ) {
                AboutSettings()
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        
        // Section content
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun ThemeSettings(
    currentAccentColor: Int,
    onAccentChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Accent color picker
        ThemeOption(
            icon = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Accent Color",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            title = "Accent Color",
            subtitle = "Choose your favorite accent color",
            trailing = {
                AccentColorPicker(
                    currentColor = Color(currentAccentColor),
                    onColorSelected = onAccentChanged
                )
            }
        )
    }
}

@Composable
private fun ThemeOption(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        trailing()
    }
}

@Composable
private fun AccentColorPicker(
    currentColor: Color,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColors = remember {
        listOf(
            0xFF00E5FF.toInt(), // Cyan
            0xFFFF4081.toInt(), // Pink
            0xFFB2FF59.toInt(), // Light Green
            0xFFFFD740.toInt(), // Yellow
            0xFFAB47BC.toInt(), // Purple
            0xFFFF5722.toInt(), // Deep Orange
            0xFF4CAF50.toInt(), // Green
            0xFF2196F3.toInt(), // Blue
        )
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        accentColors.forEach { colorArgb ->
            val color = Color(colorArgb)
            val isSelected = colorArgb == currentColor.toArgb()
            
            Surface(
                color = color,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = if (isSelected) 2.dp else 0.dp,
                shadowElevation = if (isSelected) 4.dp else 0.dp,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onColorSelected(colorArgb) }
            ) {
                if (isSelected) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = if (color.luminance() > 0.5f) Color.Black else Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettings(
    modifier: Modifier = Modifier
) {
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        NotificationOption(
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Push Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            title = "Push Notifications",
            subtitle = "Receive notifications for new messages",
            checked = pushNotificationsEnabled,
            onCheckedChange = { pushNotificationsEnabled = it }
        )
        
        NotificationOption(
            icon = {
                Text(
                    text = "🔊",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            title = "Sound",
            subtitle = "Play sound for notifications",
            checked = soundEnabled,
            onCheckedChange = { soundEnabled = it }
        )
        
        NotificationOption(
            icon = {
                Text(
                    text = "📳",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            title = "Vibration",
            subtitle = "Vibrate for notifications",
            checked = vibrationEnabled,
            onCheckedChange = { vibrationEnabled = it }
        )
    }
}

@Composable
private fun NotificationOption(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LocalAccentColor.current,
                checkedTrackColor = LocalAccentColor.current.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun AboutSettings(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        AboutOption(
            title = "Version",
            subtitle = "1.0.0",
            onClick = { }
        )
        
        AboutOption(
            title = "Privacy Policy",
            subtitle = "Read our privacy policy",
            onClick = { }
        )
        
        AboutOption(
            title = "Terms of Service",
            subtitle = "Terms and conditions",
            onClick = { }
        )
        
        AboutOption(
            title = "Open Source Licenses",
            subtitle = "View third-party licenses",
            onClick = { }
        )
    }
}

@Composable
private fun AboutOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = ">",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}