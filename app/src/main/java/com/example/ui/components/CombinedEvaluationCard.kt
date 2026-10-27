package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CombinedEvaluation
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierFair

@Composable
fun CombinedEvaluationCard(
  modifier: Modifier = Modifier,
  evaluation: CombinedEvaluation,
  fps: Double?,
  hz: Double?,
) {
  val overallColor = evaluation.overallColor

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("combined_evaluation_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    border = BorderStroke(1.dp, overallColor.copy(alpha = 0.4f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      // Main Heading
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(CyanGlow.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Combined Evaluation",
              tint = CyanGlow,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "تقييم اللعب",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
              ),
            )
            Text(
              text = "تقدير السلاسة استنادًا إلى القراءات المتاحة",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 11.sp,
              ),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section with the THREE MANDATORY ELEMENTS
      Text(
        text = "العناصر الأساسية للتقييم:",
        style = MaterialTheme.typography.labelMedium.copy(
          color = TextSecondary,
          fontWeight = FontWeight.SemiBold,
        ),
      )

      Spacer(modifier = Modifier.height(10.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // 1. تقييم FPS
        ElementRatingRow(
          title = "1. تقييم FPS",
          value = if (fps != null) "${String.format("%.1f", fps)} FPS" else "جارٍ القياس",
          ratingText = evaluation.fpsRating?.shortLabel ?: "غير متوفر",
          color = evaluation.fpsRating?.color ?: CyanPrimary,
          icon = Icons.Default.Speed,
          testTag = "element_fps_rating",
        )

        // 2. تقييم Hz
        ElementRatingRow(
          title = "2. تقييم Hz",
          value = if (hz != null) "${hz.toInt()} Hz" else "غير متاح",
          ratingText = evaluation.hzRating?.shortLabel ?: "غير متوفر",
          color = evaluation.hzRating?.color ?: CyanPrimary,
          icon = Icons.Default.Tv,
          testTag = "element_hz_rating",
        )

        // 3. التقييم المشترك
        ElementRatingRow(
          title = "3. التقييم المشترك",
          value = evaluation.summaryTitle,
          ratingText = if (evaluation.isReliable) "تقدير فعلي" else "مؤقت",
          color = overallColor,
          icon = Icons.Default.CheckCircle,
          isHighlight = true,
          testTag = "element_combined_rating",
        )
      }

      Spacer(modifier = Modifier.height(18.dp))
      HorizontalDivider(color = SurfaceBorder)
      Spacer(modifier = Modifier.height(16.dp))

      // Detailed Smoothness Assessment
      Text(
        text = "تحليل السلاسة والتوازن:",
        style = MaterialTheme.typography.labelMedium.copy(
          color = CyanPrimary,
          fontWeight = FontWeight.Bold,
        ),
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = evaluation.summaryDescription,
        modifier = Modifier.testTag("combined_description_text"),
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextPrimary,
          lineHeight = 22.sp,
        ),
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Ratio Insight Box (Rules 3 & 4)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "توضيح النسبة",
            tint = CyanPrimary,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = evaluation.ratioInsight,
            modifier = Modifier.testTag("ratio_insight_text"),
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextSecondary,
              lineHeight = 20.sp,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Scientific Hardware Limitations Note (Rule 5 & 6)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceBorder.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = "ملاحظة العتاد",
            tint = TextMuted,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = evaluation.hardwareNotice,
            modifier = Modifier.testTag("hardware_notice_text"),
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              lineHeight = 18.sp,
              fontSize = 11.sp,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun ElementRatingRow(
  title: String,
  value: String,
  ratingText: String,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isHighlight: Boolean = false,
  testTag: String,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isHighlight) color.copy(alpha = 0.12f) else SurfaceDark,
    border = BorderStroke(1.dp, if (isHighlight) color.copy(alpha = 0.5f) else SurfaceBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = color,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isHighlight) TextPrimary else TextSecondary,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
          ),
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = value,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
          ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = color.copy(alpha = 0.2f),
        ) {
          Text(
            text = ratingText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = color,
              fontWeight = FontWeight.Bold,
            ),
          )
        }
      }
    }
  }
}
