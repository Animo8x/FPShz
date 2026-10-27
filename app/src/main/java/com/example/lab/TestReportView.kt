package com.example.lab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RatingEvaluator
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierCritical
import com.example.ui.theme.TierFair
import com.example.ui.theme.TierGood

@Composable
fun TestReportView(
  stats: TestSessionStats,
  onReplay: () -> Unit,
  onBackToLab: () -> Unit,
) {
  BackHandler { onBackToLab() }

  val avgRating = RatingEvaluator.evaluateFps(stats.avgFps)
  val ratingColor = avgRating?.color ?: CyanPrimary
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .verticalScroll(scrollState)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackToLab, modifier = Modifier.testTag("report_back_to_lab")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "العودة للمختبر", tint = TextPrimary)
        }
        Column {
          Text(
            text = "تقرير النتيجة",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
            ),
          )
          Text(
            text = stats.testType.titleAr,
            style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontSize = 11.sp),
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = ratingColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, ratingColor.copy(alpha = 0.5f)),
      ) {
        Text(
          text = avgRating?.shortLabel ?: "تقييم الجلسة",
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          style = MaterialTheme.typography.labelMedium.copy(
            color = ratingColor,
            fontWeight = FontWeight.Bold,
          ),
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Main Summary Card
    Card(
      modifier = Modifier.fillMaxWidth().testTag("report_summary_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      border = BorderStroke(1.dp, SurfaceBorder),
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Assessment, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ملخص مؤشرات الأداء خلال الجلسة",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
            ),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 6 primary metrics
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          ReportMetricBox(
            modifier = Modifier.weight(1f),
            title = "متوسط FPS",
            value = String.format("%.1f", stats.avgFps),
            unit = "FPS",
            color = ratingColor,
            testTag = "report_avg_fps",
          )
          ReportMetricBox(
            modifier = Modifier.weight(1f),
            title = "مدة الاختبار",
            value = formatDuration(stats.durationSeconds),
            unit = "دقيقة",
            color = TextPrimary,
            testTag = "report_duration",
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          ReportMetricBox(
            modifier = Modifier.weight(1f),
            title = "أدنى FPS (1% Low)",
            value = if (stats.minFps > 0 && stats.minFps != Double.MAX_VALUE) String.format("%.1f", stats.minFps) else "N/A",
            unit = "FPS",
            color = TierCritical,
            testTag = "report_min_fps",
          )
          ReportMetricBox(
            modifier = Modifier.weight(1f),
            title = "أعلى FPS",
            value = if (stats.maxFps > 0) String.format("%.1f", stats.maxFps) else "N/A",
            unit = "FPS",
            color = TierGood,
            testTag = "report_max_fps",
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          ReportMetricBox(
            modifier = Modifier.weight(1f),
            title = "متوسط زمن الإطار",
            value = String.format("%.1f", stats.avgFrameTimeMs),
            unit = "ms",
            color = CyanSecondary,
            testTag = "report_avg_frametime",
          )
          ReportMetricBox(
            modifier = Modifier.weight(1f),
            title = "معدل تحديث الشاشة",
            value = "${stats.displayHz.toInt()}",
            unit = "Hz",
            color = CyanPrimary,
            testTag = "report_display_hz",
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Performance Graph Over Time (رسم بياني للأداء خلال الجلسة)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("report_chart_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = SurfaceCard),
      border = BorderStroke(1.dp, SurfaceBorder),
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShowChart, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "الرسم البياني للأداء خلال الجلسة",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
              ),
            )
          }
          Text(
            text = "عينات كل 500ms",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Performance Chart Canvas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val maxScaleFps = (stats.displayHz * 1.15f).coerceAtLeast(60.0).toFloat()

            // Draw horizontal threshold grid lines (30, 60, 90, 120 FPS)
            val gridTiers = listOf(30f, 60f, 90f, 120f, 144f).filter { it <= maxScaleFps }
            gridTiers.forEach { tierVal ->
              val y = h - (tierVal / maxScaleFps * h)
              drawLine(
                color = SurfaceBorder.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
              )
            }

            // Draw Target Hz Reference Line
            val hzY = h - (stats.displayHz.toFloat() / maxScaleFps * h).coerceIn(0f, h)
            drawLine(
              color = CyanSecondary.copy(alpha = 0.7f),
              start = Offset(0f, hzY),
              end = Offset(w, hzY),
              strokeWidth = 1.5.dp.toPx(),
              pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
            )

            val samples = stats.fpsHistory
            if (samples.size >= 2) {
              val stepX = w / (samples.size - 1).toFloat()
              val linePath = Path()
              val fillPath = Path()

              samples.forEachIndexed { i, fpsVal ->
                val x = i * stepX
                val y = (h - (fpsVal / maxScaleFps * h)).coerceIn(4f, h - 4f)
                if (i == 0) {
                  linePath.moveTo(x, y)
                  fillPath.moveTo(x, h)
                  fillPath.lineTo(x, y)
                } else {
                  linePath.lineTo(x, y)
                  fillPath.lineTo(x, y)
                }
              }

              fillPath.lineTo(w, h)
              fillPath.close()

              // Gradient area under curve
              drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                  colors = listOf(CyanPrimary.copy(alpha = 0.35f), Color.Transparent),
                  startY = 0f,
                  endY = h,
                ),
              )

              // Line curve
              drawPath(
                path = linePath,
                color = CyanPrimary,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
              )
            } else {
              // Flat line if not enough samples
              val y = h - (stats.avgFps.toFloat() / maxScaleFps * h).coerceIn(10f, h - 10f)
              drawLine(
                color = CyanPrimary,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 2.dp.toPx(),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "بداية الاختبار", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
          Text(text = "خط التردد الأقصى (${stats.displayHz.toInt()} Hz)", style = MaterialTheme.typography.labelSmall.copy(color = CyanSecondary, fontSize = 10.sp))
          Text(text = "نهاية الاختبار", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action Buttons: Replay & Back to Lab
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Button(
        onClick = onReplay,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
        modifier = Modifier.weight(1f).height(48.dp).testTag("report_replay_button"),
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "إعادة الاختبار", fontWeight = FontWeight.Bold)
      }

      OutlinedButton(
        onClick = onBackToLab,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SurfaceBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        modifier = Modifier.weight(1f).height(48.dp).testTag("report_return_lab_button"),
      ) {
        Text(text = "العودة إلى المختبر")
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun ReportMetricBox(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  unit: String,
  color: Color,
  testTag: String,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = SurfaceDark,
    border = BorderStroke(1.dp, SurfaceBorder),
    modifier = modifier.testTag(testTag),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp))
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Black,
            color = color,
            fontFamily = FontFamily.Monospace,
          ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = unit,
          modifier = Modifier.padding(bottom = 2.dp),
          style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp),
        )
      }
    }
  }
}
