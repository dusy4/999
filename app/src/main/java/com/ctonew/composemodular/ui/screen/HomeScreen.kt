package com.ctonew.composemodular.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ctonew.composemodular.featurechat.ui.ChatPlaceholderCard
import com.ctonew.composemodular.ui.theme.LocalAccentColor

@Composable
fun HomeScreen(
    onCycleAccent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentColor.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Compose Modular Placeholder",
                style = MaterialTheme.typography.headlineSmall,
            )

            RowAccentPreview(accent = accent)

            Button(
                onClick = onCycleAccent,
            ) {
                Text("Change accent")
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChatPlaceholderCard(
                modifier = Modifier.align(Alignment.Start),
            )
        }
    }
}

@Composable
private fun RowAccentPreview(
    accent: androidx.compose.ui.graphics.Color,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Current accent",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accent),
        )
    }
}
