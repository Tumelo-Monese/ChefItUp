package com.chefitup.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chefitup.app.R
import com.chefitup.app.domain.model.BadgeDefinition
import com.chefitup.app.domain.model.ChefLevel
import com.chefitup.app.domain.model.UserGamification
import com.chefitup.app.presentation.theme.EmberGold
import com.chefitup.app.presentation.theme.FlameDeep
import com.chefitup.app.presentation.theme.FlameOrange

@Composable
fun chefLevelLabel(level: ChefLevel): String = when (level) {
    ChefLevel.HOME_COOK -> stringResource(R.string.chef_level_home_cook)
    ChefLevel.SOUS_CHEF -> stringResource(R.string.chef_level_sous_chef)
    ChefLevel.MASTER_CHEF -> stringResource(R.string.chef_level_master_chef)
    ChefLevel.AWARD_WINNING_CHEF -> stringResource(R.string.chef_level_award_winning)
}

@Composable
fun ChefLevelCard(
    gamification: UserGamification,
    modifier: Modifier = Modifier
) {
    val progress = if (gamification.xpNeededForNextLevel <= 0) {
        1f
    } else {
        (gamification.xpInCurrentLevel.toFloat() / gamification.xpNeededForNextLevel)
            .coerceIn(0f, 1f)
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "xp_progress")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(FlameOrange, FlameDeep, EmberGold.copy(alpha = 0.85f))
                )
            )
            .padding(18.dp)
    ) {
        Column {
            RowBadgeHeader(levelLabel = chefLevelLabel(gamification.level))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_xp, gamification.xp),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.28f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.profile_xp_progress,
                    gamification.xpInCurrentLevel,
                    gamification.xpNeededForNextLevel
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun RowBadgeHeader(levelLabel: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = levelLabel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun BadgeCard(
    badge: BadgeDefinition,
    earned: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (earned) {
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            )
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (earned) EmberGold.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (earned) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (earned) EmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(if (earned) R.string.badge_earned else R.string.badge_locked),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
