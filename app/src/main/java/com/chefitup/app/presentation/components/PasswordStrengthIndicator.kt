package com.chefitup.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chefitup.app.R
import com.chefitup.app.domain.model.PasswordStrength
import com.chefitup.app.presentation.theme.EmberGold
import com.chefitup.app.presentation.theme.FlameDeep
import com.chefitup.app.presentation.theme.Sage

@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    if (strength == PasswordStrength.EMPTY) return

    val progress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.EMPTY -> 0f
            PasswordStrength.WEAK -> 0.25f
            PasswordStrength.FAIR -> 0.5f
            PasswordStrength.GOOD -> 0.75f
            PasswordStrength.STRONG -> 1f
        },
        label = "password_strength_progress"
    )

    val barColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.EMPTY -> Color.Transparent
            PasswordStrength.WEAK -> FlameDeep
            PasswordStrength.FAIR -> EmberGold
            PasswordStrength.GOOD -> Color(0xFF2A9D8F)
            PasswordStrength.STRONG -> Sage
        },
        label = "password_strength_color"
    )

    val labelRes = when (strength) {
        PasswordStrength.EMPTY -> return
        PasswordStrength.WEAK -> R.string.password_strength_weak
        PasswordStrength.FAIR -> R.string.password_strength_fair
        PasswordStrength.GOOD -> R.string.password_strength_good
        PasswordStrength.STRONG -> R.string.password_strength_strong
    }

    val strengthLabel = stringResource(labelRes)
    val description = stringResource(R.string.password_strength_label)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "$description: $strengthLabel"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strengthLabel,
                style = MaterialTheme.typography.labelMedium,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(barColor)
            )
        }
    }
}
