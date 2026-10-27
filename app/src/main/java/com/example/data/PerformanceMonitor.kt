package com.example.data

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.Display
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State representing live telemetry readings and their metadata.
 */
data class TelemetryState(
  val currentFps: Double? = null,
  val currentHz: Double? = null,
  val fpsSource: String = "Choreographer (معدل إطارات المعالجة الرسومية الحية)",
  val hzSource: String = "DisplayManager (تردد شاشة العرض الفعلي للنظام)",
  val isFpsMeasuring: Boolean = true,
  val isHzAvailable: Boolean = false,
  val supportedRefreshRates: List<Float> = emptyList(),
  val fpsHistory: List<Float> = emptyList(),
  val isPaused: Boolean = false,
  val isSyntheticTestMode: Boolean = false,
  val syntheticFpsValue: Double? = null,
  val syntheticHzValue: Double? = null,
) {
  val effectiveFps: Double?
    get() = if (isSyntheticTestMode) syntheticFpsValue else currentFps

  val effectiveHz: Double?
    get() = if (isSyntheticTestMode) syntheticHzValue else currentHz

  val effectiveFpsSource: String
    get() = if (isSyntheticTestMode) "معاينة حدود التقييم (بيانات اختبار اصطناعية)" else fpsSource

  val effectiveHzSource: String
    get() = if (isSyntheticTestMode) "معاينة حدود التردد (بيانات اختبار اصطناعية)" else hzSource
}

class PerformanceMonitor(
  private val context: Context,
) {
  private val _state = MutableStateFlow(TelemetryState())
  val state: StateFlow<TelemetryState> = _state.asStateFlow()

  private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
  private val mainHandler = Handler(Looper.getMainLooper())

  private var isRunning = false
  private var frameCount = 0
  private var windowStartNanos: Long = 0L

  private val slidingWindowMillis = 500L
  private val slidingWindowNanos = slidingWindowMillis * 1_000_000L

  private val frameCallback = object : Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      if (!isRunning) return

      if (windowStartNanos == 0L) {
        windowStartNanos = frameTimeNanos
        frameCount = 1
      } else {
        frameCount++
        val elapsed = frameTimeNanos - windowStartNanos
        if (elapsed >= slidingWindowNanos) {
          val calculatedFps = (frameCount.toDouble() * 1_000_000_000.0) / elapsed.toDouble()
          updateFps(calculatedFps)
          windowStartNanos = frameTimeNanos
          frameCount = 0
        }
      }

      Choreographer.getInstance().postFrameCallback(this)
    }
  }

  private val displayListener = object : DisplayManager.DisplayListener {
    override fun onDisplayAdded(displayId: Int) {}
    override fun onDisplayRemoved(displayId: Int) {}
    override fun onDisplayChanged(displayId: Int) {
      if (displayId == Display.DEFAULT_DISPLAY) {
        readDisplayRefreshRate()
      }
    }
  }

  fun start() {
    if (isRunning) return
    isRunning = true
    _state.update { it.copy(isPaused = false, isFpsMeasuring = true) }

    windowStartNanos = 0L
    frameCount = 0
    Choreographer.getInstance().postFrameCallback(frameCallback)

    readDisplayRefreshRate()
    displayManager?.registerDisplayListener(displayListener, mainHandler)
  }

  fun stop() {
    isRunning = false
    Choreographer.getInstance().removeFrameCallback(frameCallback)
    displayManager?.unregisterDisplayListener(displayListener)
    _state.update { it.copy(isPaused = true) }
  }

  fun readDisplayRefreshRate() {
    val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
    if (display != null) {
      val rate = display.refreshRate.toDouble()
      val supported = display.supportedModes.map { it.refreshRate }.distinct().sorted()
      _state.update {
        it.copy(
          currentHz = rate,
          isHzAvailable = true,
          supportedRefreshRates = supported,
        )
      }
    } else {
      _state.update {
        it.copy(
          currentHz = null,
          isHzAvailable = false,
        )
      }
    }
  }

  private fun updateFps(fps: Double) {
    _state.update { current ->
      val history = (current.fpsHistory + fps.toFloat()).takeLast(30)
      current.copy(
        currentFps = fps,
        isFpsMeasuring = false,
        fpsHistory = history,
      )
    }
  }

  fun enableSyntheticPreview(fps: Double?, hz: Double?) {
    _state.update {
      it.copy(
        isSyntheticTestMode = true,
        syntheticFpsValue = fps,
        syntheticHzValue = hz,
      )
    }
  }

  fun disableSyntheticPreview() {
    _state.update {
      it.copy(
        isSyntheticTestMode = false,
        syntheticFpsValue = null,
        syntheticHzValue = null,
      )
    }
  }
}
