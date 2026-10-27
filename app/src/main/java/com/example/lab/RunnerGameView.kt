package com.example.lab

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class RunnerObstacle(
  val id: Long,
  val lane: Int, // -1 (left), 0 (center), 1 (right)
  var z: Float,  // 1000 down to 0
  val type: Int, // 0: barrier, 1: pylon
)

data class RunnerCoin(
  val id: Long,
  val lane: Int,
  var z: Float,
  var collected: Boolean = false,
)

data class SpeedParticle(
  var x: Float,
  var y: Float,
  var z: Float,
  var speed: Float,
)

@Composable
fun RunnerGameView(
  onFinishTest: (TestSessionStats) -> Unit,
  onExit: () -> Unit,
) {
  val context = LocalContext.current
  val tracker = remember { LabSessionTracker(context, LabTestType.RUNNER) }
  val stats by tracker.stats.collectAsState()

  var quality by remember { mutableStateOf(QualityLevel.MEDIUM) }
  var playerLane by remember { mutableIntStateOf(0) } // -1 (left), 0 (center), 1 (right)
  var playerYOffset by remember { mutableFloatStateOf(0f) }
  var isJumping by remember { mutableStateOf(false) }

  var score by remember { mutableIntStateOf(0) }
  var coinsCount by remember { mutableIntStateOf(0) }
  var isGameOver by remember { mutableStateOf(false) }
  var speed by remember { mutableFloatStateOf(14f) }

  val obstacles = remember { mutableStateListOf<RunnerObstacle>() }
  val coins = remember { mutableStateListOf<RunnerCoin>() }
  val particles = remember {
    mutableStateListOf<SpeedParticle>().apply {
      repeat(50) {
        add(
          SpeedParticle(
            x = (Random.nextFloat() - 0.5f) * 600f,
            y = (Random.nextFloat() - 0.5f) * 400f,
            z = Random.nextFloat() * 1000f,
            speed = Random.nextFloat() * 15f + 10f,
          )
        )
      }
    }
  }

  val animatedLaneOffset by animateFloatAsState(
    targetValue = playerLane.toFloat(),
    animationSpec = tween(140),
    label = "lane_shift",
  )

  fun restartGame() {
    obstacles.clear()
    coins.clear()
    playerLane = 0
    playerYOffset = 0f
    isJumping = false
    score = 0
    coinsCount = 0
    speed = 14f
    isGameOver = false
    tracker.startTracking(initialObjectCount = 10)
  }

  DisposableEffect(Unit) {
    tracker.startTracking(initialObjectCount = 10)
    onDispose { tracker.stopTracking() }
  }

  BackHandler {
    val finalStats = tracker.stopTracking()
    onFinishTest(finalStats)
  }

  // Jump animation handler
  LaunchedEffect(isJumping) {
    if (isJumping) {
      val jumpDuration = 450L
      val steps = 18
      val stepTime = jumpDuration / steps
      for (i in 0..steps) {
        val rad = (i.toFloat() / steps) * Math.PI.toFloat()
        playerYOffset = sin(rad) * 60f
        delay(stepTime)
      }
      playerYOffset = 0f
      isJumping = false
    }
  }

  // Main game tick loop
  LaunchedEffect(isGameOver, quality) {
    var nextSpawnZ = 900f
    var nextCoinZ = 750f

    while (!isGameOver) {
      delay(16L) // ~60fps logic tick

      score += 1
      speed = (14f + (score / 200f)).coerceAtMost(28f)

      // Move obstacles
      val iterator = obstacles.iterator()
      while (iterator.hasNext()) {
        val obs = iterator.next()
        obs.z -= speed

        // Collision check when obstacle is near player plane (z between 0 and 55)
        if (obs.z in 0f..55f && obs.lane == playerLane) {
          if (playerYOffset < 35f) { // Can jump over low barriers
            isGameOver = true
            break
          }
        }
        if (obs.z < -50f) {
          iterator.remove()
        }
      }

      // Move Coins
      val coinIterator = coins.iterator()
      while (coinIterator.hasNext()) {
        val c = coinIterator.next()
        c.z -= speed
        if (!c.collected && c.z in 0f..50f && c.lane == playerLane) {
          c.collected = true
          coinsCount++
          score += 20
        }
        if (c.z < -40f || c.collected) {
          coinIterator.remove()
        }
      }

      // Spawn new obstacles
      if (obstacles.isEmpty() || (obstacles.lastOrNull()?.z ?: 0f) < nextSpawnZ - 260f) {
        val lane = Random.nextInt(-1, 2)
        val id = System.nanoTime()
        obstacles.add(RunnerObstacle(id = id, lane = lane, z = nextSpawnZ, type = Random.nextInt(0, 2)))
      }

      // Spawn new coins
      if (coins.isEmpty() || (coins.lastOrNull()?.z ?: 0f) < nextCoinZ - 200f) {
        val coinLane = Random.nextInt(-1, 2)
        val id = System.nanoTime()
        coins.add(RunnerCoin(id = id, lane = coinLane, z = nextCoinZ))
      }

      // Move Speed Particles
      val particleCount = when (quality) {
        QualityLevel.LOW -> 15
        QualityLevel.MEDIUM -> 40
        QualityLevel.HIGH -> 80
      }
      for (i in 0 until particleCount.coerceAtMost(particles.size)) {
        val p = particles[i]
        p.z -= p.speed + speed
        if (p.z < 10f) {
          p.z = 1000f
          p.x = (Random.nextFloat() - 0.5f) * 600f
          p.y = (Random.nextFloat() - 0.5f) * 400f
        }
      }

      tracker.updateObjectCount(obstacles.size + coins.size + particleCount)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            val finalStats = tracker.stopTracking()
            onFinishTest(finalStats)
          },
          modifier = Modifier.testTag("runner_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة", tint = TextPrimary)
        }
        Text(
          text = "Runner — متوسط",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
          ),
        )
      }

      // Quality Toggle Chip
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, SurfaceBorder),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = "الجودة: ", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
          Text(
            text = quality.labelAr,
            modifier = Modifier.testTag("runner_quality_toggle"),
            style = MaterialTheme.typography.labelMedium.copy(
              color = CyanPrimary,
              fontWeight = FontWeight.Bold,
            ),
          )
          Spacer(modifier = Modifier.width(6.dp))
          IconButton(
            onClick = {
              quality = when (quality) {
                QualityLevel.LOW -> QualityLevel.MEDIUM
                QualityLevel.MEDIUM -> QualityLevel.HIGH
                QualityLevel.HIGH -> QualityLevel.LOW
              }
            },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(Icons.Default.Tune, contentDescription = "تغيير الجودة", tint = CyanSecondary, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Real-Time In-Game Telemetry HUD
    LabTelemetryHud(
      stats = stats,
      onStopSession = {
        val finalStats = tracker.stopTracking()
        onFinishTest(finalStats)
      },
      additionalInfo = "النقاط: $score | العملات: $coinsCount | السرعة: ${String.format("%.1f", speed)}x",
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 3D / Pseudo-3D Runner Canvas with Dual Gesture Support (Swipe & Tap)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(SurfaceDark, RoundedCornerShape(18.dp))
        // Swipe: Left/Right to change lane, Up to jump
        .pointerInput(Unit) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            val (dx, dy) = dragAmount
            if (dy < -25f && !isJumping) {
              isJumping = true
            } else if (dx > 25f && playerLane < 1) {
              playerLane++
            } else if (dx < -25f && playerLane > -1) {
              playerLane--
            }
          }
        }
        // Tap: Left third -> Left lane (-1), Center third -> Center lane (0), Right third -> Right lane (1)
        .pointerInput(Unit) {
          detectTapGestures { tapOffset ->
            val w = size.width
            val third = w / 3f
            if (tapOffset.y < size.height * 0.4f && !isJumping) {
              isJumping = true
            } else if (tapOffset.x < third) {
              playerLane = -1
            } else if (tapOffset.x < third * 2f) {
              playerLane = 0
            } else {
              playerLane = 1
            }
          }
        },
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val horizonY = h * 0.32f
        val focalLength = 320f
        val baseLaneWidth = w * 0.26f

        // Draw Sky and Grid Horizon
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF070B12), Color(0xFF10192A)),
            startY = 0f,
            endY = horizonY,
          ),
          size = Size(w, horizonY),
        )

        // Draw Receding Track (3 Lanes)
        val trackBottomWidth = w * 0.95f
        val trackTopWidth = w * 0.12f

        val trackPath = Path().apply {
          moveTo(cx - trackTopWidth / 2, horizonY)
          lineTo(cx + trackTopWidth / 2, horizonY)
          lineTo(cx + trackBottomWidth / 2, h)
          lineTo(cx - trackBottomWidth / 2, h)
          close()
        }
        drawPath(
          path = trackPath,
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF131D2E), Color(0xFF0F172A)),
            startY = horizonY,
            endY = h,
          ),
        )

        // Draw 3-Lane Separator Lines with Perspective
        val laneOffsets = listOf(-0.5f, 0.5f)
        laneOffsets.forEach { lOffset ->
          val topX = cx + (trackTopWidth * lOffset * 0.45f)
          val botX = cx + (trackBottomWidth * lOffset * 0.35f)
          drawLine(
            color = CyanPrimary.copy(alpha = 0.45f),
            start = Offset(topX, horizonY),
            end = Offset(botX, h),
            strokeWidth = 2.dp.toPx(),
          )
        }

        // Draw 3D Speed Lines / Particles
        val particleCount = when (quality) {
          QualityLevel.LOW -> 15
          QualityLevel.MEDIUM -> 40
          QualityLevel.HIGH -> 80
        }
        for (i in 0 until particleCount.coerceAtMost(particles.size)) {
          val p = particles[i]
          val pScale = focalLength / (p.z + focalLength)
          val px = cx + p.x * pScale
          val py = horizonY + p.y * pScale
          if (px in 0f..w && py in 0f..h) {
            drawCircle(
              color = CyanGlow.copy(alpha = (pScale * 1.5f).coerceIn(0.2f, 0.9f)),
              radius = (3.dp.toPx() * pScale).coerceAtLeast(1.dp.toPx()),
              center = Offset(px, py),
            )
          }
        }

        // Draw Obstacles with true Perspective Z-Scaling
        obstacles.sortedByDescending { it.z }.forEach { obs ->
          val scale = focalLength / (obs.z + focalLength)
          val obsX = cx + (obs.lane * baseLaneWidth * scale * 2.2f)
          val obsY = horizonY + (h - horizonY) * scale
          val obsWidth = 44.dp.toPx() * scale * 2.2f
          val obsHeight = 44.dp.toPx() * scale * 2.2f

          if (obs.z > 0f) {
            // Shadow
            drawOval(
              color = Color.Black.copy(alpha = 0.5f),
              topLeft = Offset(obsX - obsWidth / 2, obsY - 4.dp.toPx()),
              size = Size(obsWidth, obsHeight * 0.3f),
            )
            // Obstacle Mesh (Red Energy Barrier / 3D Box)
            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(TierCritical, Color(0xFF991B1B)),
              ),
              topLeft = Offset(obsX - obsWidth / 2, obsY - obsHeight),
              size = Size(obsWidth, obsHeight),
              cornerRadius = CornerRadius(6.dp.toPx() * scale, 6.dp.toPx() * scale),
            )
          }
        }

        // Draw Coins with Perspective
        coins.sortedByDescending { it.z }.forEach { coin ->
          if (!coin.collected && coin.z > 0f) {
            val scale = focalLength / (coin.z + focalLength)
            val coinX = cx + (coin.lane * baseLaneWidth * scale * 2.2f)
            val coinY = horizonY + (h - horizonY) * scale - (15.dp.toPx() * scale)
            val radius = 14.dp.toPx() * scale * 1.5f

            drawCircle(
              color = TierFair,
              radius = radius,
              center = Offset(coinX, coinY),
            )
            drawCircle(
              color = Color.White.copy(alpha = 0.8f),
              radius = radius * 0.5f,
              center = Offset(coinX, coinY),
            )
          }
        }

        // Draw Player Character (Futuristic Cyan Skimmer)
        val playerBaseY = h * 0.86f - playerYOffset
        val playerX = cx + (animatedLaneOffset * baseLaneWidth * 0.72f)
        val playerWidth = 56.dp.toPx()
        val playerHeight = 28.dp.toPx()

        // Player Shadow
        drawOval(
          color = Color.Black.copy(alpha = 0.6f),
          topLeft = Offset(playerX - playerWidth / 2, h * 0.88f),
          size = Size(playerWidth, 12.dp.toPx()),
        )

        // Skimmer Body (3D Diamond/Wing shape)
        val skimmerPath = Path().apply {
          moveTo(playerX, playerBaseY - playerHeight) // Nose
          lineTo(playerX + playerWidth / 2, playerBaseY) // Right wing
          lineTo(playerX, playerBaseY - playerHeight * 0.35f) // Center engine
          lineTo(playerX - playerWidth / 2, playerBaseY) // Left wing
          close()
        }
        drawPath(
          path = skimmerPath,
          brush = Brush.verticalGradient(
            colors = listOf(CyanGlow, CyanDark),
          ),
        )

        // Thruster glow
        drawCircle(
          color = CyanPrimary,
          radius = 6.dp.toPx(),
          center = Offset(playerX, playerBaseY - playerHeight * 0.2f),
        )
      }

      // Game Over Screen
      if (isGameOver) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(18.dp)),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
          ) {
            Text(
              text = "اصطدام! انتهى الركض",
              style = MaterialTheme.typography.titleLarge.copy(
                color = TierCritical,
                fontWeight = FontWeight.Bold,
              ),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "النقاط المحققة: $score | العملات: $coinsCount",
              style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = { restartGame() },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("runner_replay_button"),
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إعادة المحاولة")
              }
              Button(
                onClick = {
                  val finalStats = tracker.stopTracking()
                  onFinishTest(finalStats)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("runner_view_report_button"),
              ) {
                Text("عرض التقرير")
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Interactive Buttons: Left (-1), Center (0), Right (1), and Jump
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Left Lane Button
      val isLeft = playerLane == -1
      Button(
        onClick = { playerLane = -1 },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isLeft) CyanPrimary else SurfaceCard,
          contentColor = if (isLeft) Color.Black else TextPrimary,
        ),
        border = BorderStroke(1.dp, if (isLeft) CyanGlow else SurfaceBorder),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("runner_btn_lane_left"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
      ) {
        Icon(Icons.Default.ArrowLeft, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "يسار", fontSize = 12.sp, fontWeight = if (isLeft) FontWeight.Bold else FontWeight.Medium)
      }

      // Center Lane Button
      val isCenter = playerLane == 0
      Button(
        onClick = { playerLane = 0 },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isCenter) CyanPrimary else SurfaceCard,
          contentColor = if (isCenter) Color.Black else TextPrimary,
        ),
        border = BorderStroke(1.dp, if (isCenter) CyanGlow else SurfaceBorder),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("runner_btn_lane_center"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
      ) {
        Icon(Icons.Default.HorizontalRule, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "الوسط", fontSize = 12.sp, fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Medium)
      }

      // Right Lane Button
      val isRight = playerLane == 1
      Button(
        onClick = { playerLane = 1 },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isRight) CyanPrimary else SurfaceCard,
          contentColor = if (isRight) Color.Black else TextPrimary,
        ),
        border = BorderStroke(1.dp, if (isRight) CyanGlow else SurfaceBorder),
        modifier = Modifier
          .weight(1f)
          .height(50.dp)
          .testTag("runner_btn_lane_right"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
      ) {
        Text(text = "يمين", fontSize = 12.sp, fontWeight = if (isRight) FontWeight.Bold else FontWeight.Medium)
        Spacer(modifier = Modifier.width(2.dp))
        Icon(Icons.Default.ArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
      }

      // Jump Button
      Button(
        onClick = { if (!isJumping) isJumping = true },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF0369A1),
          contentColor = Color.White,
        ),
        border = BorderStroke(1.dp, CyanSecondary),
        modifier = Modifier
          .weight(0.9f)
          .height(50.dp)
          .testTag("runner_btn_jump"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
      ) {
        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "قفز", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}
