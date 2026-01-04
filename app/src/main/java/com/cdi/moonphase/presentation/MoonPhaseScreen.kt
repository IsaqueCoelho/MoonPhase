package com.cdi.moonphase.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cdi.moonphase.presentation.components.MoonPhaseAnimation

@Composable
fun MoonPhaseScreen(viewModel: MoonPhaseViewModel) {
    val phase by viewModel.state.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MoonPhaseAnimation(
                modifier = Modifier.size(240.dp),
                illumination = phase?.illumination ?: 0.0f,
                waxing = phase?.isWaxing ?: true
            )
            Text(
                text = phase?.name ?: "Calculating…",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            if (phase != null) {
                Text(
                    text = if (phase!!.isWaxing) "Waxing" else "Waning",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
