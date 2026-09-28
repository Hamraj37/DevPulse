package com.hamraj37.devpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamraj37.devpulse.data.model.AppTab
import com.hamraj37.devpulse.ui.theme.DevPulseTheme
import kotlinx.coroutines.launch

@Composable
fun DevPulseTabRow(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = AppTab.entriesOrdered
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val selectedIndex = tabs.indexOf(selectedTab)

    // Automatically scroll the active pill into view when selectedIndex changes
    LaunchedEffect(selectedIndex) {
        if (selectedIndex in tabs.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(tabs) { index, tab ->
                val isSelected = tab == selectedTab

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "pillBgColor"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "pillContentColor"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.03f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "pillScale"
                )

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = backgroundColor,
                    contentColor = contentColor,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .scale(scale)
                        .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                            onTabSelected(tab)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = tab.title,
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DevPulseTabRow(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = AppTab.entriesOrdered
    val selectedTab = tabs.getOrElse(selectedIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))) { AppTab.DASHBOARD }
    DevPulseTabRow(
        selectedTab = selectedTab,
        onTabSelected = { tab ->
            val index = tabs.indexOf(tab)
            if (index in tabs.indices) {
                onTabSelected(index)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun DevPulseTabRowPreview() {
    DevPulseTheme {
        DevPulseTabRow(
            selectedTab = AppTab.DASHBOARD,
            onTabSelected = {}
        )
    }
}
