package com.example.lab

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Choreographer
import android.view.Display
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class LabTestType(
  val titleAr: String,
  val categoryAr: String,
  val subtitleAr: String,
  val descriptionAr: String,
  val iconName: String,
) {
  SNAKE(
    titleAr = "Snake — خفيف",
    categoryAr = "حمل خفيف (2D Canvas)",
    subtitleAr = "لعبة الدودة الأصلية ثنائية الأبعاد",
    descriptionAr = "قياس كفاءة معالجة الإطارات في بيئة ثنائية الأبعاد خفيفة تعتمد على مدخلات اللمس السريعة ومنطق الشبكة.",
    iconName = "snake",
  ),
  RUNNER(
    titleAr = "Runner — متوسط",
    categoryAr = "حمل متوسط (Pseudo-3D / Runner)",
    subtitleAr = "لعبة ركض لا نهائي ثلاثية الأبعاد",
    descriptionAr = "اختبار تأثير المنظور ثلاثي الأبعاد وحركة الكاميرا وتوليد المسارات والمجسمات المتحركة على استقرار الإطارات.",
    iconName = "runner",
  ),
  STRESS_3D(
    titleAr = "3D Stress Test — ثقيل",
    categoryAr = "حمل ثقيل (3D Mesh & Matrix)",
    subtitleAr = "مشهد اختبار رسومي ثلاثي الأبعاد",
    descriptionAr = "ضغط رسومي حقيقي عبر مصفوفات التحويل ثلاثية الأبعاد، وفرز العمق (Painter's Algorithm)، وحساب الإضاءة والظلال على مئات المجسمات.",
    iconName = "cube",
  ),
}

enum class QualityLevel(val labelAr: String) {
  LOW("منخفض"),
  MEDIUM("متوسط"),
  HIGH("مرتفع"),
}

data class TestSessionStats(
  val testType: LabTestType,
  val durationSeconds: Double = 0.0,
  val currentFps: Double = 0.0,
  val avgFps: Double = 0.0,
  val minFps: Double = 0.0,
  val maxFps: Double = 0.0,
  val currentFrameTimeMs: Double = 0.0,
  val avgFrameTimeMs: Double = 0.0,
  val displayHz: Double = 60.0,
  val fpsHistory: List<Float> = emptyList(),
  val frameTimeHistory: List<Float> = emptyList(),
  val activeObjectCount: Int = 0,
  val isRunning: Boolean = false,
  val isCompleted: Boolean = false,
)

/**
 * Independent Session Tracker measuring real rendered frame callbacks
 * from Android Choreographer, isolated from game logic ticks.
 */
class LabSessionTracker(
  private val context: Context,
  val testType: LabTestType,
) {
  private val _stats = MutableStateFlow(TestSessionStats(testType = testType))
  val stats: StateFlow<TestSessionStats> = _stats.asStateFlow()

  private var isTracking = false
  private var sessionStartTimeNanos = 0L
  private var lastFrameTimeNanos = 0L

  private var frameCountInWindow = 0
  private var windowStartNanos = 0L
  private val windowDurationNanos = 500_000_000L // 500 ms window

  private var totalFramesInSession = 0
  private var minRecordedFps = Double.MAX_VALUE
  private var maxRecordedFps = 0.0

  private val recordedFpsSamples = mutableListOf<Float>()
  private val recordedFrameTimes = mutableListOf<Float>()

  private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager

  private val frameCallback = object : Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      if (!isTracking) return

      if (sessionStartTimeNanos == 0L) {
        sessionStartTimeNanos = frameTimeNanos
        windowStartNanos = frameTimeNanos
        lastFrameTimeNanos = frameTimeNanos
        frameCountInWindow = 1
        totalFramesInSession = 1
      } else {
        totalFramesInSession++
        frameCountInWindow++

        val frameDeltaNanos = frameTimeNanos - lastFrameTimeNanos
        val frameTimeMs = if (frameDeltaNanos > 0) frameDeltaNanos / 1_000_000.0 else 16.6
        lastFrameTimeNanos = frameTimeNanos

        val windowElapsed = frameTimeNanos - windowStartNanos
        if (windowElapsed >= windowDurationNanos) {
          val calculatedFps = (frameCountInWindow.toDouble() * 1_000_000_000.0) / windowElapsed.toDouble()
          frameCountInWindow = 0
          windowStartNanos = frameTimeNanos

          if (calculatedFps in 1.0..360.0) {
            recordedFpsSamples.add(calculatedFps.toFloat())
            recordedFrameTimes.add(frameTimeMs.toFloat())

            if (calculatedFps < minRecordedFps) minRecordedFps = calculatedFps
            if (calculatedFps > maxRecordedFps) maxRecordedFps = calculatedFps

            val totalElapsedSec = (frameTimeNanos - sessionStartTimeNanos) / 1_000_000_000.0
            val overallAvgFps = if (totalElapsedSec > 0) totalFramesInSession / totalElapsedSec else calculatedFps
            val overallAvgFrameTime = if (overallAvgFps > 0) 1000.0 / overallAvgFps else 16.6

            val currentHz = readSystemHz()

            _stats.update { current ->
              current.copy(
                durationSeconds = totalElapsedSec,
                currentFps = calculatedFps,
                avgFps = overallAvgFps,
                minFps = if (minRecordedFps == Double.MAX_VALUE) calculatedFps else minRecordedFps,
                maxFps = maxRecordedFps,
                currentFrameTimeMs = frameTimeMs,
                avgFrameTimeMs = overallAvgFrameTime,
                displayHz = currentHz,
                fpsHistory = recordedFpsSamples.toList(),
                frameTimeHistory = recordedFrameTimes.toList(),
                isRunning = true,
              )
            }
          }
        }
      }

      Choreographer.getInstance().postFrameCallback(this)
    }
  }

  fun startTracking(initialObjectCount: Int = 0) {
    if (isTracking) return
    isTracking = true
    sessionStartTimeNanos = 0L
    lastFrameTimeNanos = 0L
    frameCountInWindow = 0
    windowStartNanos = 0L
    totalFramesInSession = 0
    minRecordedFps = Double.MAX_VALUE
    maxRecordedFps = 0.0
    recordedFpsSamples.clear()
    recordedFrameTimes.clear()

    _stats.value = TestSessionStats(
      testType = testType,
      displayHz = readSystemHz(),
      activeObjectCount = initialObjectCount,
      isRunning = true,
      isCompleted = false,
    )

    Choreographer.getInstance().postFrameCallback(frameCallback)
  }

  fun updateObjectCount(count: Int) {
    _stats.update { it.copy(activeObjectCount = count) }
  }

  fun stopTracking(): TestSessionStats {
    isTracking = false
    Choreographer.getInstance().removeFrameCallback(frameCallback)

    val finalHz = readSystemHz()
    val finalStats = _stats.value.copy(
      isRunning = false,
      isCompleted = true,
      displayHz = finalHz,
    )
    _stats.value = finalStats
    return finalStats
  }

  private fun readSystemHz(): Double {
    val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
    return display?.refreshRate?.toDouble() ?: 60.0
  }
}
