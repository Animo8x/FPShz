package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierCritical
import com.example.ui.theme.TierExcellent
import com.example.ui.theme.TierFair
import com.example.ui.theme.TierGood
import com.example.ui.theme.TierHigh
import com.example.ui.theme.TierLegendary
import com.example.ui.theme.TierUltra
import com.example.ui.theme.TierWeak

val FpsTierColors = listOf(
  TierCritical,   // <15
  TierWeak,       // 15-30
  TierFair,       // 30-60
  TierGood,       // 60-90
  TierHigh,       // 90-120
  TierExcellent,  // 120-144
  TierUltra,      // 144-165
  TierLegendary,  // 165+
)

val HzTierColors = listOf(
  TierWeak,       // <60
  TierFair,       // 60-90
  TierGood,       // 90-120
  TierHigh,       // 120-144
  TierExcellent,  // 144+
)

@Composable
fun RatingProgressBar(
  modifier: Modifier = Modifier,
  currentFraction: Float, // 0.0f to 1.0f
  tierColors: List<Color>,
  tierLabels: List<String>,
  thresholdTicks: List<String>,
  activeTierIndex: Int?,
  activeColor: Color,
  testTag: String,
) {
  val animatedProgress by animateFloatAsState(
    targetValue = currentFraction.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 350),
    label = "rating_progress",
  )

  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
  ) {
    // Canvas with multi-segment rating line and smooth sliding pointer
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(30.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(10.dp)
          .align(Alignment.Center)
      ) {
        val width = size.width
        val height = size.height
        val segmentCount = tierColors.size
        val segmentWidth = width / segmentCount

        // Draw track segments
        for (i in 0 until segmentCount) {
          val segX = if (isRtl) (segmentCount - 1 - i) * segmentWidth else i * segmentWidth
          val color = tierColors[i]
          val isCurrent = (activeTierIndex == i)
          val alpha = if (isCurrent) 1.0f else 0.45f

          drawRoundRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(segX + 1.dp.toPx(), 0f),
            size = Size(segmentWidth - 2.dp.toPx(), height),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
          )
        }

        // Pointer indicator line position
        val pointerX = if (isRtl) {
          width * (1f - animatedProgress)
        } else {
          width * animatedProgress
        }

        if (activeTierIndex != null) {
          // Glow halo
          drawCircle(
            color = activeColor.copy(alpha = 0.35f),
            radius = 12.dp.toPx(),
            center = Offset(pointerX, height / 2),
          )
          // Solid outer ring
          drawCircle(
            color = TextPrimary,
            radius = 6.dp.toPx(),
            center = Offset(pointerX, height / 2),
          )
          // Inner colored core
          drawCircle(
            color = activeColor,
            radius = 4.dp.toPx(),
            center = Offset(pointerX, height / 2),
          )
        }
      }
    }

    // Tick labels beneath the bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      thresholdTicks.forEach { tick ->
        Text(
          text = tick,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium,
          ),
        )
      }
    }
  }
}
