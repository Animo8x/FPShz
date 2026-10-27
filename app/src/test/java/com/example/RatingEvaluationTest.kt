package com.example

import com.example.data.FpsRating
import com.example.data.HzRating
import com.example.data.RatingEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingEvaluationTest {

  @Test
  fun testFpsRatingExactArabicDescriptions() {
    assertEquals("غير مناسب أبدًا للعب.", FpsRating.UNPLAYABLE.description)
    assertEquals("أداء ضعيف.", FpsRating.WEAK.description)
    assertEquals("قد تلاحظ تقطعًا بسيطًا جدًا.", FpsRating.SLIGHT_STUTTER.description)
    assertEquals("جيد جدًا.", FpsRating.VERY_GOOD.description)
    assertEquals("جيد للعب الثقيل.", FpsRating.GOOD_HEAVY.description)
    assertEquals("ممتاز للمهام الثقيلة واللعب الثقيل جدًا.", FpsRating.EXCELLENT.description)
    assertEquals("خارق الأداء — ممتاز للمهام الثقيلة جدًا واللعب الثقيل جدًا.", FpsRating.ULTRA.description)
    assertEquals("خيالي — ممتاز للعب الثقيل جدًا جدًا والمهام الثقيلة جدًا جدًا.", FpsRating.LEGENDARY.description)
  }

  @Test
  fun testHzRatingExactArabicDescriptions() {
    assertEquals("محدود السلاسة.", HzRating.LIMITED.description)
    assertEquals("عادي.", HzRating.NORMAL.description)
    assertEquals("جيد للعب.", HzRating.GOOD.description)
    assertEquals("جيد جدًا للعب — ستشعر بسلاسة أكبر.", HzRating.VERY_GOOD.description)
    assertEquals("ممتاز — سلاسة عالية جدًا.", HzRating.EXCELLENT.description)
  }

  @Test
  fun testFpsThresholdBoundaries() {
    // Null and invalid values
    assertNull(RatingEvaluator.evaluateFps(null))
    assertNull(RatingEvaluator.evaluateFps(-5.0))

    // Threshold 1: < 15 FPS (UNPLAYABLE)
    assertEquals(FpsRating.UNPLAYABLE, RatingEvaluator.evaluateFps(0.0))
    assertEquals(FpsRating.UNPLAYABLE, RatingEvaluator.evaluateFps(14.0))
    assertEquals(FpsRating.UNPLAYABLE, RatingEvaluator.evaluateFps(14.99))

    // Threshold 2: 15 to < 30 FPS (WEAK)
    assertEquals(FpsRating.WEAK, RatingEvaluator.evaluateFps(15.0))
    assertEquals(FpsRating.WEAK, RatingEvaluator.evaluateFps(22.5))
    assertEquals(FpsRating.WEAK, RatingEvaluator.evaluateFps(29.99))

    // Threshold 3: 30 to < 60 FPS (SLIGHT_STUTTER)
    assertEquals(FpsRating.SLIGHT_STUTTER, RatingEvaluator.evaluateFps(30.0))
    assertEquals(FpsRating.SLIGHT_STUTTER, RatingEvaluator.evaluateFps(45.0))
    assertEquals(FpsRating.SLIGHT_STUTTER, RatingEvaluator.evaluateFps(59.99))

    // Threshold 4: 60 to < 90 FPS (VERY_GOOD)
    assertEquals(FpsRating.VERY_GOOD, RatingEvaluator.evaluateFps(60.0))
    assertEquals(FpsRating.VERY_GOOD, RatingEvaluator.evaluateFps(75.0))
    assertEquals(FpsRating.VERY_GOOD, RatingEvaluator.evaluateFps(89.99))

    // Threshold 5: 90 to < 120 FPS (GOOD_HEAVY)
    assertEquals(FpsRating.GOOD_HEAVY, RatingEvaluator.evaluateFps(90.0))
    assertEquals(FpsRating.GOOD_HEAVY, RatingEvaluator.evaluateFps(105.0))
    assertEquals(FpsRating.GOOD_HEAVY, RatingEvaluator.evaluateFps(119.99))

    // Threshold 6: 120 to < 144 FPS (EXCELLENT)
    assertEquals(FpsRating.EXCELLENT, RatingEvaluator.evaluateFps(120.0))
    assertEquals(FpsRating.EXCELLENT, RatingEvaluator.evaluateFps(132.0))
    assertEquals(FpsRating.EXCELLENT, RatingEvaluator.evaluateFps(143.99))

    // Threshold 7: 144 to < 165 FPS (ULTRA)
    assertEquals(FpsRating.ULTRA, RatingEvaluator.evaluateFps(144.0))
    assertEquals(FpsRating.ULTRA, RatingEvaluator.evaluateFps(155.0))
    assertEquals(FpsRating.ULTRA, RatingEvaluator.evaluateFps(164.99))

    // Threshold 8: 165+ FPS (LEGENDARY)
    assertEquals(FpsRating.LEGENDARY, RatingEvaluator.evaluateFps(165.0))
    assertEquals(FpsRating.LEGENDARY, RatingEvaluator.evaluateFps(180.0))
    assertEquals(FpsRating.LEGENDARY, RatingEvaluator.evaluateFps(240.0))
  }

  @Test
  fun testHzThresholdBoundaries() {
    // Null and invalid values
    assertNull(RatingEvaluator.evaluateHz(null))
    assertNull(RatingEvaluator.evaluateHz(0.0))
    assertNull(RatingEvaluator.evaluateHz(-60.0))

    // Threshold 1: < 60 Hz (LIMITED)
    assertEquals(HzRating.LIMITED, RatingEvaluator.evaluateHz(30.0))
    assertEquals(HzRating.LIMITED, RatingEvaluator.evaluateHz(59.9))

    // Threshold 2: 60 to < 90 Hz (NORMAL)
    assertEquals(HzRating.NORMAL, RatingEvaluator.evaluateHz(60.0))
    assertEquals(HzRating.NORMAL, RatingEvaluator.evaluateHz(75.0))
    assertEquals(HzRating.NORMAL, RatingEvaluator.evaluateHz(89.9))

    // Threshold 3: 90 to < 120 Hz (GOOD)
    assertEquals(HzRating.GOOD, RatingEvaluator.evaluateHz(90.0))
    assertEquals(HzRating.GOOD, RatingEvaluator.evaluateHz(100.0))
    assertEquals(HzRating.GOOD, RatingEvaluator.evaluateHz(119.9))

    // Threshold 4: 120 to < 144 Hz (VERY_GOOD)
    assertEquals(HzRating.VERY_GOOD, RatingEvaluator.evaluateHz(120.0))
    assertEquals(HzRating.VERY_GOOD, RatingEvaluator.evaluateHz(130.0))
    assertEquals(HzRating.VERY_GOOD, RatingEvaluator.evaluateHz(143.9))

    // Threshold 5: 144+ Hz (EXCELLENT)
    assertEquals(HzRating.EXCELLENT, RatingEvaluator.evaluateHz(144.0))
    assertEquals(HzRating.EXCELLENT, RatingEvaluator.evaluateHz(165.0))
    assertEquals(HzRating.EXCELLENT, RatingEvaluator.evaluateHz(240.0))
  }

  @Test
  fun testCombinedEvaluationRules() {
    // Unreliable when inputs are null
    val pending = RatingEvaluator.evaluateCombined(null, 60.0)
    assertFalse(pending.isReliable)
    assertNull(pending.fpsRating)

    // Rule 1 & 2: Low FPS cannot produce excellent experience even with 144 Hz display
    val bottleneckEval = RatingEvaluator.evaluateCombined(12.0, 144.0)
    assertTrue(bottleneckEval.isReliable)
    assertEquals(FpsRating.UNPLAYABLE, bottleneckEval.fpsRating)
    assertEquals(HzRating.EXCELLENT, bottleneckEval.hzRating)
    assertTrue(bottleneckEval.summaryTitle.contains("غير مناسب"))

    // Rule 3: High Hz but low FPS explains underutilized screen capability
    val ratioHighHzEval = RatingEvaluator.evaluateCombined(22.0, 120.0)
    assertTrue(ratioHighHzEval.ratioInsight.contains("الشاشة تعمل بتردد عالي") || ratioHighHzEval.ratioInsight.contains("سعة إضافية"))

    // Rule 4: High FPS exceeding Hz explains screen refresh ceiling
    val fpsExceedsHzEval = RatingEvaluator.evaluateCombined(120.0, 60.0)
    assertTrue(fpsExceedsHzEval.ratioInsight.contains("يتجاوز معدل تحديث الشاشة"))

    // Rule 5 & 6: Hardware limitations notice present and designated as smoothness estimation
    assertNotNull(bottleneckEval.hardwareNotice)
    assertTrue(bottleneckEval.hardwareNotice.contains("تنبيه تقني"))
    assertTrue(bottleneckEval.hardwareNotice.contains("المعالج المركزي"))
    assertTrue(bottleneckEval.hardwareNotice.contains("حرارة الجهاز"))
    assertTrue(bottleneckEval.hardwareNotice.contains("تقدير للسلاسة"))
  }
}
