package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lab.PerformanceLabScreen
import com.example.ui.MainViewModel
import com.example.ui.components.CombinedEvaluationCard
import com.example.ui.components.FpsCard
import com.example.ui.components.HzCard
import com.example.ui.components.StressAndPresetTester
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierGood

enum class MainAppTab(val titleAr: String) {
  MONITOR("الرئيسية والتقييم"),
  LAB("مختبر الأداء"),
}

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        // Enforce Arabic RTL layout direction
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          FpsHzApp(viewModel = viewModel)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FpsHzApp(viewModel: MainViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  var selectedTab by remember { mutableStateOf(MainAppTab.MONITOR) }

  // Stop measuring when leaving the screen and resume upon return (Requirement 5)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> viewModel.onResume()
        Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> viewModel.onPause()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // Handle back button from Lab to return to Monitor
  if (selectedTab == MainAppTab.LAB) {
    BackHandler {
      selectedTab = MainAppTab.MONITOR
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark),
    containerColor = BackgroundDark,
    contentWindowInsets = WindowInsets.statusBars,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = SurfaceDark,
          titleContentColor = TextPrimary,
        ),
        title = {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .background(CyanPrimary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.Speed,
                  contentDescription = "Logo",
                  tint = BackgroundDark,
                  modifier = Modifier.size(20.dp),
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "FPShz",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp,
                  ),
                )
                Text(
                  text = if (selectedTab == MainAppTab.MONITOR) "نظام التقييم اللحظي للأداء" else "مختبر الأداء التفاعلي",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = CyanGlow,
                    fontSize = 11.sp,
                  ),
                )
              }
            }

            // Live Pulse Status Indicator
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (uiState.telemetry.isSyntheticTestMode) Color(0xFF33200B) else Color(0xFF063024),
              modifier = Modifier.padding(end = 12.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.FiberManualRecord,
                  contentDescription = null,
                  tint = if (uiState.telemetry.isSyntheticTestMode) Color(0xFFF97316) else TierGood,
                  modifier = Modifier.size(10.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (uiState.telemetry.isSyntheticTestMode) "معاينة حدود" else "قياس حي (500ms)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (uiState.telemetry.isSyntheticTestMode) Color(0xFFFDBA74) else TierGood,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                  ),
                )
              }
            }
          }
        },
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextSecondary,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("main_bottom_nav"),
      ) {
        NavigationBarItem(
          selected = selectedTab == MainAppTab.MONITOR,
          onClick = { selectedTab = MainAppTab.MONITOR },
          icon = { Icon(Icons.Default.Speed, contentDescription = "الرئيسية") },
          label = { Text("الرئيسية والتقييم") },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = CyanPrimary,
            indicatorColor = CyanPrimary,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted,
          ),
          modifier = Modifier.testTag("nav_tab_monitor"),
        )
        NavigationBarItem(
          selected = selectedTab == MainAppTab.LAB,
          onClick = { selectedTab = MainAppTab.LAB },
          icon = { Icon(Icons.Default.Science, contentDescription = "مختبر الأداء") },
          label = { Text("مختبر الأداء") },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = CyanPrimary,
            indicatorColor = CyanPrimary,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted,
          ),
          modifier = Modifier.testTag("nav_tab_lab"),
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "tab_switch",
        modifier = Modifier.fillMaxSize(),
      ) { tab ->
        when (tab) {
          MainAppTab.MONITOR -> {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = 16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              item { Spacer(modifier = Modifier.height(4.dp)) }

              // 1. بطاقة FPS والتقييم
              item {
                FpsCard(
                  fps = uiState.telemetry.effectiveFps,
                  rating = uiState.fpsRating,
                  source = uiState.telemetry.effectiveFpsSource,
                  isMeasuring = uiState.telemetry.isFpsMeasuring && !uiState.telemetry.isSyntheticTestMode,
                )
              }

              // 2. بطاقة Hz والتقييم
              item {
                HzCard(
                  hz = uiState.telemetry.effectiveHz,
                  rating = uiState.hzRating,
                  source = uiState.telemetry.effectiveHzSource,
                  supportedRates = uiState.telemetry.supportedRefreshRates,
                  isAvailable = uiState.telemetry.isHzAvailable || uiState.telemetry.isSyntheticTestMode,
                )
              }

              // 3. تقييم مشترك: FPS + Hz
              item {
                CombinedEvaluationCard(
                  evaluation = uiState.combinedEvaluation,
                  fps = uiState.telemetry.effectiveFps,
                  hz = uiState.telemetry.effectiveHz,
                )
              }

              // 4. اختبار الإجهاد ومعاينة الحدود
              item {
                StressAndPresetTester(
                  isStressRunning = uiState.isStressTesting,
                  isSyntheticMode = uiState.telemetry.isSyntheticTestMode,
                  onToggleStress = { viewModel.toggleStressTest() },
                  onSelectPreset = { fps, hz -> viewModel.selectPreset(fps, hz) },
                )
              }

              item {
                Spacer(modifier = Modifier.height(24.dp))
              }
            }
          }

          MainAppTab.LAB -> {
            PerformanceLabScreen(
              modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            )
          }
        }
      }
    }
  }
}
