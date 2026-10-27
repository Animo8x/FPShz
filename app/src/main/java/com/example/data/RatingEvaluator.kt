package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TierCritical
import com.example.ui.theme.TierWeak
import com.example.ui.theme.TierFair
import com.example.ui.theme.TierGood
import com.example.ui.theme.TierHigh
import com.example.ui.theme.TierExcellent
import com.example.ui.theme.TierUltra
import com.example.ui.theme.TierLegendary

/**
 * FPS Rating tiers as strictly defined in user requirements.
 */
enum class FpsRating(
  val shortLabel: String,
  val description: String,
  val tierIndex: Int,
  val minFps: Double,
  val maxFps: Double,
  val color: Color,
) {
  UNPLAYABLE(
    shortLabel = "غير مناسب",
    description = "غير مناسب أبدًا للعب.",
    tierIndex = 0,
    minFps = 0.0,
    maxFps = 15.0,
    color = TierCritical,
  ),
  WEAK(
    shortLabel = "أداء ضعيف",
    description = "أداء ضعيف.",
    tierIndex = 1,
    minFps = 15.0,
    maxFps = 30.0,
    color = TierWeak,
  ),
  SLIGHT_STUTTER(
    shortLabel = "تقطع بسيط",
    description = "قد تلاحظ تقطعًا بسيطًا جدًا.",
    tierIndex = 2,
    minFps = 30.0,
    maxFps = 60.0,
    color = TierFair,
  ),
  VERY_GOOD(
    shortLabel = "جيد جدًا",
    description = "جيد جدًا.",
    tierIndex = 3,
    minFps = 60.0,
    maxFps = 90.0,
    color = TierGood,
  ),
  GOOD_HEAVY(
    shortLabel = "جيد للعب الثقيل",
    description = "جيد للعب الثقيل.",
    tierIndex = 4,
    minFps = 90.0,
    maxFps = 120.0,
    color = TierHigh,
  ),
  EXCELLENT(
    shortLabel = "ممتاز",
    description = "ممتاز للمهام الثقيلة واللعب الثقيل جدًا.",
    tierIndex = 5,
    minFps = 120.0,
    maxFps = 144.0,
    color = TierExcellent,
  ),
  ULTRA(
    shortLabel = "خارق الأداء",
    description = "خارق الأداء — ممتاز للمهام الثقيلة جدًا واللعب الثقيل جدًا.",
    tierIndex = 6,
    minFps = 144.0,
    maxFps = 165.0,
    color = TierUltra,
  ),
  LEGENDARY(
    shortLabel = "خيالي",
    description = "خيالي — ممتاز للعب الثقيل جدًا جدًا والمهام الثقيلة جدًا جدًا.",
    tierIndex = 7,
    minFps = 165.0,
    maxFps = 240.0,
    color = TierLegendary,
  );

  val normalizedProgress: Float
    get() = (tierIndex + 0.5f) / 8f
}

/**
 * Hz Rating tiers as strictly defined in user requirements.
 */
enum class HzRating(
  val shortLabel: String,
  val description: String,
  val tierIndex: Int,
  val minHz: Double,
  val maxHz: Double,
  val color: Color,
) {
  LIMITED(
    shortLabel = "محدود السلاسة",
    description = "محدود السلاسة.",
    tierIndex = 0,
    minHz = 0.0,
    maxHz = 60.0,
    color = TierWeak,
  ),
  NORMAL(
    shortLabel = "عادي",
    description = "عادي.",
    tierIndex = 1,
    minHz = 60.0,
    maxHz = 90.0,
    color = TierFair,
  ),
  GOOD(
    shortLabel = "جيد للعب",
    description = "جيد للعب.",
    tierIndex = 2,
    minHz = 90.0,
    maxHz = 120.0,
    color = TierGood,
  ),
  VERY_GOOD(
    shortLabel = "سلاسة أكبر",
    description = "جيد جدًا للعب — ستشعر بسلاسة أكبر.",
    tierIndex = 3,
    minHz = 120.0,
    maxHz = 144.0,
    color = TierHigh,
  ),
  EXCELLENT(
    shortLabel = "ممتاز",
    description = "ممتاز — سلاسة عالية جدًا.",
    tierIndex = 4,
    minHz = 144.0,
    maxHz = 240.0,
    color = TierExcellent,
  );

  val normalizedProgress: Float
    get() = (tierIndex + 0.5f) / 5f
}

/**
 * Combined Gaming Evaluation result with strict adherence to analytical rules.
 */
data class CombinedEvaluation(
  val fpsRating: FpsRating?,
  val hzRating: HzRating?,
  val summaryTitle: String,
  val summaryDescription: String,
  val ratioInsight: String,
  val hardwareNotice: String = HARDWARE_LIMITATION_NOTICE,
  val overallColor: Color,
  val isReliable: Boolean,
)

const val HARDWARE_LIMITATION_NOTICE =
  "تنبيه تقني: معدل الإطارات (FPS) ومعدل التحديث (Hz) وحدهما لا يحددان أداء الألعاب الثقيلة؛ إذ تلعب قوة المعالج المركزي (CPU) ومعالج الرسوميات (GPU) وحرارة الجهاز (Thermal Throttling) وتحسين محرك اللعبة نفسه عوامل حاسمة في استقرار التجربة. هذا التقييم هو تقدير للسلاسة استنادًا إلى القراءات المتاحة وليس اختبارًا شاملاً لقدرة الجهاز."

object RatingEvaluator {

  /**
   * Strictly evaluates the FPS without rounding up if below the threshold.
   * Returns null if reading is not yet available.
   */
  fun evaluateFps(fps: Double?): FpsRating? {
    if (fps == null || fps < 0.0) return null
    return when {
      fps < 15.0 -> FpsRating.UNPLAYABLE
      fps < 30.0 -> FpsRating.WEAK
      fps < 60.0 -> FpsRating.SLIGHT_STUTTER
      fps < 90.0 -> FpsRating.VERY_GOOD
      fps < 120.0 -> FpsRating.GOOD_HEAVY
      fps < 144.0 -> FpsRating.EXCELLENT
      fps < 165.0 -> FpsRating.ULTRA
      else -> FpsRating.LEGENDARY
    }
  }

  /**
   * Strictly evaluates the Hz reported by the display subsystem.
   * Returns null if reading is not available.
   */
  fun evaluateHz(hz: Double?): HzRating? {
    if (hz == null || hz <= 0.0) return null
    return when {
      hz < 60.0 -> HzRating.LIMITED
      hz < 90.0 -> HzRating.NORMAL
      hz < 120.0 -> HzRating.GOOD
      hz < 144.0 -> HzRating.VERY_GOOD
      else -> HzRating.EXCELLENT
    }
  }

  /**
   * Combined Gaming Evaluation (تقييم اللعب المشترك).
   * Adheres strictly to the following rules:
   * 1. Never grants a rating higher than the actual measured FPS bottleneck.
   * 2. If FPS is very low, does NOT describe experience as excellent even if Hz is high.
   * 3. Explains if Hz is significantly higher than FPS (screen capability underutilized).
   * 4. Explains if FPS exceeds Hz (screen cannot display all frames as distinct refreshes).
   * 5. Includes the technical hardware notice.
   * 6. Strictly designates this as a smoothness estimation based on available readings.
   */
  fun evaluateCombined(fps: Double?, hz: Double?): CombinedEvaluation {
    val fRating = evaluateFps(fps)
    val hRating = evaluateHz(hz)

    if (fRating == null || hRating == null || fps == null || hz == null) {
      return CombinedEvaluation(
        fpsRating = fRating,
        hzRating = hRating,
        summaryTitle = "بانتظار اكتمال القراءات",
        summaryDescription = "جارٍ جمع قراءات معدل الإطارات ومعدل التحديث بدقة قبل إصدار التقييم.",
        ratioInsight = "يرجى الانتظار للحصول على عينة قياس موثوقة.",
        overallColor = TierFair,
        isReliable = false,
      )
    }

    // Ratio insight analysis
    val ratioInsight = when {
      fps < 30.0 && hz >= 90.0 -> {
        "ملاحظة هامة: الشاشة تعمل بتردد عالي (${hz.toInt()} Hz)، لكن معدل الإطارات الفعلي منخفض (${String.format("%.1f", fps)} FPS)، لذا فإن السلاسة الفعلية مقيدة بعدد الإطارات المقاسة فقط."
      }
      hz - fps >= 25.0 -> {
        "الشاشة تستطيع التحديث بمعدل أعلى (${hz.toInt()} Hz) من عدد الإطارات المقاسة (${String.format("%.1f", fps)} FPS)، مما يعني وجود سعة إضافية في معدل التحديث لم تُستغل بالكامل."
      }
      fps - hz >= 10.0 -> {
        "معدل توليد الإطارات (${String.format("%.1f", fps)} FPS) يتجاوز معدل تحديث الشاشة (${hz.toInt()} Hz)؛ وهذا لا يعني أن الشاشة تعرض كل هذه الإطارات كتحديثات منفصلة، بل تتقيد بسقف ترددها."
      }
      else -> {
        "تناغم متقارب بين معدل الإطارات ومعدل التحديث، مما يمنح استقرارًا بصريًا متوازنًا."
      }
    }

    // Summary Title & Description - Bottlenecked by FPS capability (Rule 1 & Rule 2)
    val (title, description, color) = when {
      fRating == FpsRating.UNPLAYABLE -> {
        Triple(
          "غير مناسب إطلاقًا للألعاب",
          "التجربة الفعلية تعاني من هبوط حاد في الإطارات يجعل اللعب مستحيلاً، بغض النظر عن تردد الشاشة.",
          TierCritical,
        )
      }
      fRating == FpsRating.WEAK -> {
        Triple(
          "أداء ألعاب ضعيف",
          "معدل الإطارات منخفض ويسبب تقطيعًا واضحًا يعيق التفاعل السريع، حتى لو كانت الشاشة سريعة.",
          TierWeak,
        )
      }
      fRating == FpsRating.SLIGHT_STUTTER -> {
        Triple(
          "أداء مقبول مع تقطع خفيف",
          "الإطارات توفر تجربة مقبولة ولكن قد تلاحظ تقطعًا بسيطًا أثناء المشاهد السريعة والحركات المكثفة.",
          TierFair,
        )
      }
      fRating == FpsRating.VERY_GOOD -> {
        if (hRating == HzRating.LIMITED) {
          Triple(
            "أداء جيد مع محدودية سلاسة الشاشة",
            "معدل الإطارات جيد جدًا (60+ FPS) لكن معدل تحديث الشاشة الأقل من 60 Hz يحد من السلاسة البصرية.",
            TierFair,
          )
        } else {
          Triple(
            "أداء لعب سلس ومستقر",
            "معدل الإطارات وتحديث الشاشة يقدمان سلاسة جيدة جدًا تلائم معظم الألعاب التنافسية والعادية.",
            TierGood,
          )
        }
      }
      fRating == FpsRating.GOOD_HEAVY -> {
        Triple(
          "تقدير ممتاز للألعاب الثقيلة",
          "أداء قوي يوفر سرعة استجابة وسلاسة ممتازة للعب المشاهد الحركية السريعة والمهام الكثيفة.",
          TierHigh,
        )
      }
      fRating == FpsRating.EXCELLENT -> {
        Triple(
          "سلاسة ممتازة للمهام واللعب الثقيل جدًا",
          "تجربة لعب فائقة السلاسة مع استجابة فائقة السرعة للمهام الثقيلة جداً.",
          TierExcellent,
        )
      }
      fRating == FpsRating.ULTRA -> {
        Triple(
          "أداء خارق للسلاسة الفائقة",
          "معدل إطارات خارق يمنح ميزة تنافسية قصوى وسلاسة فائقة للمهام واللعب المكثف.",
          TierUltra,
        )
      }
      else -> {
        Triple(
          "أداء خيالي وسلاسة استثنائية",
          "أعلى درجات السلاسة والسرعة الممكنة استنادًا إلى القراءات اللحظية المتاحة.",
          TierLegendary,
        )
      }
    }

    return CombinedEvaluation(
      fpsRating = fRating,
      hzRating = hRating,
      summaryTitle = title,
      summaryDescription = description,
      ratioInsight = ratioInsight,
      hardwareNotice = HARDWARE_LIMITATION_NOTICE,
      overallColor = color,
      isReliable = true,
    )
  }
}
