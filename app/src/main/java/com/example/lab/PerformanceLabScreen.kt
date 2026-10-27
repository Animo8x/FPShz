package com.example.lab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.TierHigh

sealed class LabNavigationState {
  object Hub : LabNavigationState()
  data class ActiveTest(val testType: LabTestType) : LabNavigationState()
  data class Report(val stats: TestSessionStats) : LabNavigationState()
}

@Composable
fun PerformanceLabScreen(
  modifier: Modifier = Modifier,
) {
  var navState by remember { mutableStateOf<LabNavigationState>(LabNavigationState.Hub) }

  AnimatedContent(
    targetState = navState,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "lab_nav",
    modifier = modifier.fillMaxSize(),
  ) { state ->
    when (state) {
      is LabNavigationState.Hub -> {
        LabHubView(
          onStartTest = { testType ->
            navState = LabNavigationState.ActiveTest(testType)
          }
        )
      }
      is LabNavigationState.ActiveTest -> {
        when (state.testType) {
          LabTestType.SNAKE -> {
            SnakeGameView(
              onFinishTest = { stats ->
                navState = LabNavigationState.Report(stats)
              },
              onExit = { navState = LabNavigationState.Hub },
            )
          }
          LabTestType.RUNNER -> {
            RunnerGameView(
              onFinishTest = { stats ->
                navState = LabNavigationState.Report(stats)
              },
              onExit = { navState = LabNavigationState.Hub },
            )
          }
          LabTestType.STRESS_3D -> {
            StressTest3DView(
              onFinishTest = { stats ->
                navState = LabNavigationState.Report(stats)
              },
              onExit = { navState = LabNavigationState.Hub },
            )
          }
        }
      }
      is LabNavigationState.Report -> {
        TestReportView(
          stats = state.stats,
          onReplay = {
            navState = LabNavigationState.ActiveTest(state.stats.testType)
          },
          onBackToLab = {
            navState = LabNavigationState.Hub
          },
        )
      }
    }
  }
}

@Composable
private fun LabHubView(
  onStartTest: (LabTestType) -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item { Spacer(modifier = Modifier.height(4.dp)) }

    // Lab Hero Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("lab_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f)),
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .background(CyanPrimary.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp),
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "مختبر الأداء — Performance Lab",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary,
                ),
              )
              Text(
                text = "بيئات تفاعلية حقيقية لاختبار استقرار الإطارات والتردد",
                style = MaterialTheme.typography.labelSmall.copy(color = CyanGlow, fontSize = 11.sp),
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "اختر أحد الاختبارات التفاعلية لتشغيل أحمال رسومية متفاوتة من الخفيفة إلى الثقيلة جدًا، وملاحظة قياس FPS وHz الحقيقيين مع تقرير شامل بعد انتهاء كل جلسة.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = TextSecondary,
              lineHeight = 22.sp,
            ),
          )
        }
      }
    }

    // Benchmark Cards
    item {
      TestLaunchCard(
        testType = LabTestType.SNAKE,
        badgeColor = TierGood,
        icon = Icons.Default.Extension,
        onStart = { onStartTest(LabTestType.SNAKE) },
        testTag = "launch_snake_card",
      )
    }

    item {
      TestLaunchCard(
        testType = LabTestType.RUNNER,
        badgeColor = TierFair,
        icon = Icons.Default.DirectionsRun,
        onStart = { onStartTest(LabTestType.RUNNER) },
        testTag = "launch_runner_card",
      )
    }

    item {
      TestLaunchCard(
        testType = LabTestType.STRESS_3D,
        badgeColor = TierCritical,
        icon = Icons.Default.ViewInAr,
        onStart = { onStartTest(LabTestType.STRESS_3D) },
        testTag = "launch_stress3d_card",
      )
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}

@Composable
private fun TestLaunchCard(
  testType: LabTestType,
  badgeColor: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onStart: () -> Unit,
  testTag: String,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onStart() }
      .testTag(testTag),
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
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(badgeColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = testType.titleAr,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
              ),
            )
            Text(
              text = testType.subtitleAr,
              style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp),
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = badgeColor.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
        ) {
          Text(
            text = testType.categoryAr,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              color = badgeColor,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
            ),
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = testType.descriptionAr,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = TextSecondary,
          lineHeight = 21.sp,
        ),
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onStart,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = CyanPrimary,
          contentColor = Color.Black,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("btn_start_${testType.name.lowercase()}"),
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "بدء الاختبار والقياس", fontWeight = FontWeight.Bold)
      }
    }
  }
}
