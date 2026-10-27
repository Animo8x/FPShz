package com.example

import com.example.lab.LabTestType
import com.example.lab.QualityLevel
import com.example.lab.TestSessionStats
import com.example.lab.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceLabTest {

  @Test
  fun testLabTestTypeDefinitions() {
    assertEquals("Snake — خفيف", LabTestType.SNAKE.titleAr)
    assertEquals("حمل خفيف (2D Canvas)", LabTestType.SNAKE.categoryAr)

    assertEquals("Runner — متوسط", LabTestType.RUNNER.titleAr)
    assertEquals("حمل متوسط (Pseudo-3D / Runner)", LabTestType.RUNNER.categoryAr)

    assertEquals("3D Stress Test — ثقيل", LabTestType.STRESS_3D.titleAr)
    assertEquals("حمل ثقيل (3D Mesh & Matrix)", LabTestType.STRESS_3D.categoryAr)
  }

  @Test
  fun testDurationFormatting() {
    assertEquals("00:00", formatDuration(0.0))
    assertEquals("00:45", formatDuration(45.0))
    assertEquals("01:05", formatDuration(65.0))
    assertEquals("02:30", formatDuration(150.0))
  }

  @Test
  fun testQualityLevels() {
    assertEquals("منخفض", QualityLevel.LOW.labelAr)
    assertEquals("متوسط", QualityLevel.MEDIUM.labelAr)
    assertEquals("مرتفع", QualityLevel.HIGH.labelAr)
  }

  @Test
  fun testSessionStatsDataIntegrity() {
    val stats = TestSessionStats(
      testType = LabTestType.STRESS_3D,
      durationSeconds = 60.0,
      currentFps = 58.5,
      avgFps = 57.2,
      minFps = 42.0,
      maxFps = 61.0,
      currentFrameTimeMs = 17.1,
      avgFrameTimeMs = 17.5,
      displayHz = 120.0,
      fpsHistory = listOf(60f, 58f, 55f, 42f, 61f),
      activeObjectCount = 200,
    )

    assertEquals(LabTestType.STRESS_3D, stats.testType)
    assertEquals(58.5, stats.currentFps, 0.01)
    assertEquals(57.2, stats.avgFps, 0.01)
    assertEquals(42.0, stats.minFps, 0.01)
    assertEquals(61.0, stats.maxFps, 0.01)
    assertEquals(120.0, stats.displayHz, 0.01)
    assertEquals(200, stats.activeObjectCount)
    assertEquals(5, stats.fpsHistory.size)
  }

  @Test
  fun testObjectCountBounds() {
    val minCount = 1.coerceIn(1, 1000)
    val maxCount = 1000.coerceIn(1, 1000)
    val overCount = 1500.coerceIn(1, 1000)
    val underCount = 0.coerceIn(1, 1000)

    assertEquals(1, minCount)
    assertEquals(1000, maxCount)
    assertEquals(1000, overCount)
    assertEquals(1, underCount)
  }
}
