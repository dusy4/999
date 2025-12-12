package com.ctonew.composemodular.featurechat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatPlaceholderCard(
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "feature-chat",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "This module is a placeholder for a future modular / dynamic-feature chat implementation.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
