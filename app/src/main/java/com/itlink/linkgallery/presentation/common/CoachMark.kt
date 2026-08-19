package com.itlink.linkgallery.presentation.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CoachMark(
    targetCoordinates: LayoutCoordinates?,
    text: String,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (targetCoordinates == null || !targetCoordinates.isAttached) return

    val bounds = targetCoordinates.boundsInRoot()

    // Pulsing animation for the highlight
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNext
            )
    ) {
        // Overlay with a hole
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f)) {
            drawRect(color = Color.Black.copy(alpha = 0.8f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(bounds.left - 4.dp.toPx(), bounds.top - 4.dp.toPx()),
                size = Size(bounds.width + 8.dp.toPx(), bounds.height + 8.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }

        // Animated border around the target
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inflatedWidth = (bounds.width + 8.dp.toPx()) * pulseScale
            val inflatedHeight = (bounds.height + 8.dp.toPx()) * pulseScale
            val diffX = (inflatedWidth - (bounds.width + 8.dp.toPx())) / 2
            val diffY = (inflatedHeight - (bounds.height + 8.dp.toPx())) / 2

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(bounds.left - 4.dp.toPx() - diffX, bounds.top - 4.dp.toPx() - diffY),
                size = Size(inflatedWidth, inflatedHeight),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }

        // Description Text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            val isTargetInTopHalf = bounds.top < 800 // Simple heuristic
            
            Column(
                modifier = Modifier
                    .align(if (isTargetInTopHalf) Alignment.Center else Alignment.TopCenter)
                    .padding(top = if (isTargetInTopHalf) 120.dp else 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нажмите в любое место, чтобы продолжить",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
