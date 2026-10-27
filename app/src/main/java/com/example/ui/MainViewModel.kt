package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CombinedEvaluation
import com.example.data.FpsRating
import com.example.data.HzRating
import com.example.data.PerformanceMonitor
import com.example.data.RatingEvaluator
import com.example.data.TelemetryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class UiRatingState(
  val telemetry: TelemetryState = TelemetryState(),
  val fpsRating: FpsRating? = null,
  val hzRating: HzRating? = null,
  val combinedEvaluation: CombinedEvaluation = RatingEvaluator.evaluateCombined(null, null),
  val isStressTesting: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val performanceMonitor = PerformanceMonitor(application)
  private val _isStressTesting = MutableStateFlow(false)

  val uiState: StateFlow<UiRatingState> = combine(
    performanceMonitor.state,
    _isStressTesting,
  ) { telemetry, isStress ->
    val fps = telemetry.effectiveFps
    val hz = telemetry.effectiveHz
    val fpsRating = RatingEvaluator.evaluateFps(fps)
    val hzRating = RatingEvaluator.evaluateHz(hz)
    val combined = RatingEvaluator.evaluateCombined(fps, hz)

    UiRatingState(
      telemetry = telemetry,
      fpsRating = fpsRating,
      hzRating = hzRating,
      combinedEvaluation = combined,
      isStressTesting = isStress,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = UiRatingState(),
  )

  init {
    performanceMonitor.start()
  }

  fun onResume() {
    performanceMonitor.start()
  }

  fun onPause() {
    performanceMonitor.stop()
  }

  fun toggleStressTest() {
    _isStressTesting.value = !_isStressTesting.value
  }

  fun selectPreset(fps: Double?, hz: Double?) {
    if (fps == null && hz == null) {
      performanceMonitor.disableSyntheticPreview()
    } else {
      performanceMonitor.enableSyntheticPreview(fps, hz)
    }
  }

  override fun onCleared() {
    super.onCleared()
    performanceMonitor.stop()
  }
}
