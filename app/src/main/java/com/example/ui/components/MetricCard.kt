package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FpsRating
import com.example.data.HzRating
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierFair

@Composable
fun FpsCard(
  modifier: Modifier = Modifier,
  fps: Double?,
  rating: FpsRating?,
  source: String,
  isMeasuring: Boolean,
) {
  val tierLabels = FpsRating.values().map { it.shortLabel }
  val thresholdTicks = listOf("0", "15", "30", "60", "90", "120", "144", "165+")
  val activeColor = rating?.color ?: CyanPrimary
  val progressFraction = rating?.normalizedProgress ?: 0f

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("fps_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    border = BorderStroke(1.dp, activeColor.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header: Title, Icon, and Tier Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(CyanPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = "FPS Icon",
              tint = CyanPrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "معدل الإطارات (FPS)",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
              ),
            )
            Text(
              text = "المصدر: $source",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 11.sp,
              ),
            )
          }
        }

        // Tier status pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = activeColor.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)),
        ) {
          Text(
            text = rating?.shortLabel ?: if (isMeasuring) "جارٍ القياس" else "غير متاح",
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("fps_tier_badge"),
            style = MaterialTheme.typography.labelMedium.copy(
              color = activeColor,
              fontWeight = FontWeight.Bold,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Big Numeric Value Display
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
      ) {
        if (fps != null && !isMeasuring) {
          Text(
            text = String.format("%.1f", fps),
            modifier = Modifier.testTag("fps_value_text"),
            style = MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = TextPrimary,
              fontFamily = FontFamily.Monospace,
              fontSize = 50.sp,
            ),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "FPS",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = CyanPrimary,
            ),
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = CyanPrimary,
              strokeWidth = 2.5.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "جارٍ القياس...",
              modifier = Modifier.testTag("fps_measuring_text"),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyanPrimary,
              ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Rating Horizontal Bar
      RatingProgressBar(
        currentFraction = progressFraction,
        tierColors = FpsTierColors,
        tierLabels = tierLabels,
        thresholdTicks = thresholdTicks,
        activeTierIndex = rating?.tierIndex,
        activeColor = activeColor,
        testTag = "fps_rating_bar",
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Arabic Description Box
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceBorder.copy(alpha = 0.5f),
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(activeColor, CircleShape)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = rating?.description ?: "يتم جمع الإطارات الفعلية لحساب التقييم الدقيق...",
            modifier = Modifier.testTag("fps_description_text"),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (rating != null) TextPrimary else TextSecondary,
              fontWeight = FontWeight.Medium,
              lineHeight = 22.sp,
            ),
          )
        }
      }
    }
  }
}

@Composable
fun HzCard(
  modifier: Modifier = Modifier,
  hz: Double?,
  rating: HzRating?,
  source: String,
  supportedRates: List<Float>,
  isAvailable: Boolean,
) {
  val tierLabels = HzRating.values().map { it.shortLabel }
  val thresholdTicks = listOf("<60", "60", "90", "120", "144+")
  val activeColor = rating?.color ?: CyanSecondary
  val progressFraction = rating?.normalizedProgress ?: 0f

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("hz_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    border = BorderStroke(1.dp, activeColor.copy(alpha = 0.35f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Header: Title, Icon, and Tier Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(CyanSecondary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Tv,
              contentDescription = "Hz Icon",
              tint = CyanSecondary,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "معدل التحديث (Hz)",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
              ),
            )
            Text(
              text = "المصدر: $source",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 11.sp,
              ),
            )
          }
        }

        // Tier status pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = activeColor.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, activeColor.copy(alpha = 0.5f)),
        ) {
          Text(
            text = rating?.shortLabel ?: "غير متاح",
            modifier = Modifier
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("hz_tier_badge"),
            style = MaterialTheme.typography.labelMedium.copy(
              color = activeColor,
              fontWeight = FontWeight.Bold,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Big Numeric Value Display
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
      ) {
        if (hz != null && isAvailable) {
          Text(
            text = if (hz % 1.0 == 0.0) hz.toInt().toString() else String.format("%.1f", hz),
            modifier = Modifier.testTag("hz_value_text"),
            style = MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = TextPrimary,
              fontFamily = FontFamily.Monospace,
              fontSize = 50.sp,
            ),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Hz",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = CyanSecondary,
            ),
          )

          if (supportedRates.isNotEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SurfaceBorder,
              modifier = Modifier.padding(bottom = 8.dp),
            ) {
              Text(
                text = "الأنماط: ${supportedRates.map { it.toInt() }.joinToString(", ")} Hz",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextSecondary,
                  fontSize = 10.sp,
                ),
              )
            }
          }
        } else {
          Text(
            text = "غير متاح",
            modifier = Modifier
              .padding(vertical = 8.dp)
              .testTag("hz_unavailable_text"),
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = TextMuted,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Rating Horizontal Bar
      RatingProgressBar(
        currentFraction = progressFraction,
        tierColors = HzTierColors,
        tierLabels = tierLabels,
        thresholdTicks = thresholdTicks,
        activeTierIndex = rating?.tierIndex,
        activeColor = activeColor,
        testTag = "hz_rating_bar",
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Arabic Description Box
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceBorder.copy(alpha = 0.5f),
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(activeColor, CircleShape)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = rating?.description ?: "في انتظار معلومات معدل تحديث الشاشة من نظام التشغيل...",
            modifier = Modifier.testTag("hz_description_text"),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (rating != null) TextPrimary else TextSecondary,
              fontWeight = FontWeight.Medium,
              lineHeight = 22.sp,
            ),
          )
        }
      }
    }
  }
}
