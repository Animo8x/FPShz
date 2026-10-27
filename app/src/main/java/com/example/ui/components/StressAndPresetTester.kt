package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierCritical
import com.example.ui.theme.TierGood
import com.example.ui.theme.TierWeak
import kotlin.random.Random

data class PresetOption(
  val label: String,
  val fps: Double,
  val hz: Double,
  val description: String,
)

val BenchmarkPresets = listOf(
  PresetOption("14.9 FPS / 60 Hz", 14.9, 60.0, "حد غير مناسب (< 15)"),
  PresetOption("15.0 FPS / 60 Hz", 15.0, 60.0, "حد ضعيف (15-30)"),
  PresetOption("29.9 FPS / 60 Hz", 29.9, 60.0, "نهاية فئة ضعيف"),
  PresetOption("30.0 FPS / 60 Hz", 30.0, 60.0, "حد تقطع بسيط (30-60)"),
  PresetOption("59.9 FPS / 60 Hz", 59.9, 60.0, "نهاية فئة تقطع بسيط"),
  PresetOption("60.0 FPS / 60 Hz", 60.0, 60.0, "حد جيد جدًا (60-90)"),
  PresetOption("89.9 FPS / 90 Hz", 89.9, 90.0, "نهاية فئة جيد جدًا"),
  PresetOption("90.0 FPS / 90 Hz", 90.0, 90.0, "حد لعب ثقيل (90-120)"),
  PresetOption("119.9 FPS / 120 Hz", 119.9, 120.0, "نهاية فئة لعب ثقيل"),
  PresetOption("120.0 FPS / 120 Hz", 120.0, 120.0, "حد ممتاز (120-144)"),
  PresetOption("143.9 FPS / 144 Hz", 143.9, 144.0, "نهاية فئة ممتاز"),
  PresetOption("144.0 FPS / 144 Hz", 144.0, 144.0, "حد خارق (144-165)"),
  PresetOption("164.9 FPS / 165 Hz", 164.9, 165.0, "نهاية فئة خارق"),
  PresetOption("165.0 FPS / 165 Hz", 165.0, 165.0, "حد خيالي (165+)"),
  PresetOption("25 FPS / 120 Hz", 25.0, 120.0, "FPS منخفض مع Hz مرتفع"),
  PresetOption("120 FPS / 60 Hz", 120.0, 60.0, "FPS أعلى من قدرة الشاشة"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StressAndPresetTester(
  modifier: Modifier = Modifier,
  isStressRunning: Boolean,
  isSyntheticMode: Boolean,
  onToggleStress: () -> Unit,
  onSelectPreset: (Double?, Double?) -> Unit,
) {
  var isPresetsExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("stress_preset_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    border = BorderStroke(1.dp, SurfaceBorder),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Stress Testing Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(CyanPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "Stress Test",
              tint = CyanPrimary,
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "اختبار الإجهاد الرسومي المباشر",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
              ),
            )
            Text(
              text = "لتوليد حمل رسومي وملاحظة تغير FPS والتقييم كل 500ms",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 11.sp,
              ),
            )
          }
        }

        Button(
          onClick = onToggleStress,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isStressRunning) TierCritical else CyanPrimary,
            contentColor = Color.White,
          ),
          modifier = Modifier.testTag("toggle_stress_button"),
        ) {
          Icon(
            imageVector = if (isStressRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = if (isStressRunning) "إيقاف الحمل" else "تشغيل الحمل")
        }
      }

      // If stress is active, draw animated canvas particles
      if (isStressRunning) {
        Spacer(modifier = Modifier.height(14.dp))
        StressCanvas()
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Presets Toggle for Instant Boundary Testing
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isPresetsExpanded = !isPresetsExpanded }
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = CyanGlow,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "معاينة حدود التقييم (اختبار الحدود والمطابقة)",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = CyanGlow,
              fontWeight = FontWeight.SemiBold,
            ),
          )
        }
        Icon(
          imageVector = if (isPresetsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = TextSecondary,
        )
      }

      AnimatedVisibility(visible = isPresetsExpanded) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          if (isSyntheticMode) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = TierWeak.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, TierWeak.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "أنت الآن في وضع معاينة الحدود (بيانات اختبار اصطناعية)",
                  style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary),
                )
                OutlinedButton(
                  onClick = { onSelectPreset(null, null) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.testTag("reset_to_live_telemetry_button"),
                ) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "العودة للقياس الحي", fontSize = 11.sp)
                }
              }
            }
          }

          Text(
            text = "اضغط على أي حد لاختبار تغير بطاقة FPS والتقييم المشترك فورًا:",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
            modifier = Modifier.padding(bottom = 8.dp),
          )

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            BenchmarkPresets.forEach { preset ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier
                  .clickable { onSelectPreset(preset.fps, preset.hz) }
                  .testTag("preset_${preset.fps}_${preset.hz}"),
              ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                  Text(
                    text = preset.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary,
                    ),
                  )
                  Text(
                    text = preset.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      color = TextMuted,
                    ),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StressCanvas() {
  val infiniteTransition = rememberInfiniteTransition(label = "stress_anim")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "phase",
  )

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = SurfaceDark,
    border = BorderStroke(1.dp, TierCritical.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .height(110.dp),
  ) {
    Canvas(modifier = Modifier.fillMaxWidth()) {
      val w = size.width
      val h = size.height
      val count = 120

      for (i in 0 until count) {
        val angle = Math.toRadians((phase + i * (360f / count)).toDouble())
        val radius = (h * 0.38f) * ((i % 5) + 1) / 5f
        val x = (w / 2) + (Math.cos(angle) * radius).toFloat()
        val y = (h / 2) + (Math.sin(angle) * radius).toFloat()

        drawCircle(
          color = if (i % 2 == 0) CyanPrimary else TierCritical,
          radius = 3.5.dp.toPx(),
          center = Offset(x, y),
        )
      }
    }
  }
}
