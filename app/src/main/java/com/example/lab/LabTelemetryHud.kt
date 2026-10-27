package com.example.lab

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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RatingEvaluator
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
import com.example.ui.theme.TierGood

@Composable
fun LabTelemetryHud(
  modifier: Modifier = Modifier,
  stats: TestSessionStats,
  onStopSession: () -> Unit,
  additionalInfo: String? = null,
) {
  val fpsColor = if (stats.currentFps > 0) {
    RatingEvaluator.evaluateFps(stats.currentFps)?.color ?: CyanPrimary
  } else CyanPrimary

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("lab_telemetry_hud"),
    shape = RoundedCornerShape(16.dp),
    color = SurfaceCard.copy(alpha = 0.94f),
    border = BorderStroke(1.dp, SurfaceBorder),
    shadowElevation = 8.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      // Top bar: Live status, Duration, and Stop Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(if (stats.isRunning) TierGood else TierCritical, CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (stats.isRunning) "قياس مباشر (Choreographer)" else "متوقف",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (stats.isRunning) TierGood else TextMuted,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
            ),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "المدة: ${formatDuration(stats.durationSeconds)}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondary,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
            ),
          )
        }

        // Prominent Emergency Stop Button (Safety requirement)
        Button(
          onClick = onStopSession,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = TierCritical,
            contentColor = Color.White,
          ),
          modifier = Modifier
            .height(30.dp)
            .testTag("emergency_stop_button"),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop",
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "إيقاف وإنهاء", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Main Metric Numbers Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Current FPS
        Column {
          Text(
            text = "FPS اللحظي",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
          )
          Text(
            text = if (stats.currentFps > 0) String.format("%.1f", stats.currentFps) else "N/A",
            modifier = Modifier.testTag("hud_current_fps"),
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Black,
              color = fpsColor,
              fontFamily = FontFamily.Monospace,
              fontSize = 22.sp,
            ),
          )
        }

        // Frame Time
        Column {
          Text(
            text = "زمن الإطار",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
          )
          Text(
            text = if (stats.currentFrameTimeMs > 0) "${String.format("%.1f", stats.currentFrameTimeMs)} ms" else "N/A",
            modifier = Modifier.testTag("hud_frame_time"),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              fontFamily = FontFamily.Monospace,
            ),
          )
        }

        // Screen Refresh Rate
        Column {
          Text(
            text = "تردد الشاشة",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
          )
          Text(
            text = "${stats.displayHz.toInt()} Hz",
            modifier = Modifier.testTag("hud_screen_hz"),
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = CyanSecondary,
              fontFamily = FontFamily.Monospace,
            ),
          )
        }

        // Stats: Avg / Min / Max
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "المتوسط: ${if (stats.avgFps > 0) String.format("%.0f", stats.avgFps) else "N/A"}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondary,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
            ),
          )
          Text(
            text = "أدنى: ${if (stats.minFps > 0 && stats.minFps != Double.MAX_VALUE) String.format("%.0f", stats.minFps) else "N/A"} | أعلى: ${if (stats.maxFps > 0) String.format("%.0f", stats.maxFps) else "N/A"}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
            ),
          )
        }
      }

      if (!additionalInfo.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = additionalInfo,
          style = MaterialTheme.typography.labelSmall.copy(
            color = CyanGlow,
            fontSize = 10.sp,
          ),
        )
      }
    }
  }
}

fun formatDuration(seconds: Double): String {
  val totalSec = seconds.toInt()
  val m = totalSec / 60
  val s = totalSec % 60
  return String.format("%02d:%02d", m, s)
}
