package com.example.lab

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierCritical
import com.example.ui.theme.TierFair
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class SnakeDirection { UP, DOWN, LEFT, RIGHT }

data class GridPoint(val x: Int, val y: Int)

@Composable
fun SnakeGameView(
  onFinishTest: (TestSessionStats) -> Unit,
  onExit: () -> Unit,
) {
  val context = LocalContext.current
  val tracker = remember { LabSessionTracker(context, LabTestType.SNAKE) }
  val stats by tracker.stats.collectAsState()

  val gridSize = 20
  var snake = remember {
    mutableStateListOf(
      GridPoint(10, 10),
      GridPoint(10, 11),
      GridPoint(10, 12),
    )
  }
  var direction by remember { mutableStateOf(SnakeDirection.UP) }
  var nextDirection by remember { mutableStateOf(SnakeDirection.UP) }
  var food by remember { mutableStateOf(GridPoint(5, 5)) }
  var goldenFood by remember { mutableStateOf<GridPoint?>(null) }
  var score by remember { mutableIntStateOf(0) }
  var isGameOver by remember { mutableStateOf(false) }
  var isPaused by remember { mutableStateOf(false) }

  fun restartGame() {
    snake.clear()
    snake.addAll(listOf(GridPoint(10, 10), GridPoint(10, 11), GridPoint(10, 12)))
    direction = SnakeDirection.UP
    nextDirection = SnakeDirection.UP
    food = GridPoint(Random.nextInt(1, gridSize - 1), Random.nextInt(1, gridSize - 1))
    goldenFood = null
    score = 0
    isGameOver = false
    isPaused = false
    tracker.startTracking(initialObjectCount = 3)
  }

  // Handle lifecycle and initial start
  DisposableEffect(Unit) {
    tracker.startTracking(initialObjectCount = snake.size)
    onDispose {
      tracker.stopTracking()
    }
  }

  BackHandler {
    val finalStats = tracker.stopTracking()
    onFinishTest(finalStats)
  }

  // Separate Game Logic Loop (independent of render loop)
  LaunchedEffect(isGameOver, isPaused) {
    while (!isGameOver && !isPaused) {
      delay(110L) // 110ms game tick
      direction = nextDirection
      val head = snake.first()
      val newHead = when (direction) {
        SnakeDirection.UP -> GridPoint(head.x, head.y - 1)
        SnakeDirection.DOWN -> GridPoint(head.x, head.y + 1)
        SnakeDirection.LEFT -> GridPoint(head.x - 1, head.y)
        SnakeDirection.RIGHT -> GridPoint(head.x + 1, head.y)
      }

      // Wall collision check
      if (newHead.x < 0 || newHead.x >= gridSize || newHead.y < 0 || newHead.y >= gridSize) {
        isGameOver = true
        break
      }

      // Self collision check
      if (snake.contains(newHead)) {
        isGameOver = true
        break
      }

      snake.add(0, newHead)

      // Food check
      if (newHead == food) {
        score += 10
        tracker.updateObjectCount(snake.size)
        // Spawn new food not on snake
        var newFood: GridPoint
        do {
          newFood = GridPoint(Random.nextInt(0, gridSize), Random.nextInt(0, gridSize))
        } while (snake.contains(newFood))
        food = newFood

        // Chance to spawn golden food
        if (Random.nextFloat() < 0.25f && goldenFood == null) {
          goldenFood = GridPoint(Random.nextInt(0, gridSize), Random.nextInt(0, gridSize))
        }
      } else if (newHead == goldenFood) {
        score += 30
        goldenFood = null
        tracker.updateObjectCount(snake.size)
      } else {
        snake.removeAt(snake.size - 1)
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Top Bar with back button, game title, pause toggle, and score
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
          modifier = Modifier.testTag("snake_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "عودة",
            tint = TextPrimary,
          )
        }
        Text(
          text = "Snake — خفيف",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
          ),
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Pause / Resume Toggle
        IconButton(
          onClick = { isPaused = !isPaused },
          modifier = Modifier
            .size(36.dp)
            .background(SurfaceCard, CircleShape)
            .border(BorderStroke(1.dp, SurfaceBorder), CircleShape)
            .testTag("snake_pause_toggle"),
        ) {
          Icon(
            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = "إيقاف مؤقت",
            tint = CyanPrimary,
            modifier = Modifier.size(18.dp),
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = SurfaceCard,
          border = BorderStroke(1.dp, SurfaceBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(text = "النقاط: ", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Text(
              text = "$score",
              style = MaterialTheme.typography.titleMedium.copy(
                color = CyanPrimary,
                fontWeight = FontWeight.Black,
              ),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Real-Time Telemetry HUD inside the test screen
    LabTelemetryHud(
      stats = stats,
      onStopSession = {
        val finalStats = tracker.stopTracking()
        onFinishTest(finalStats)
      },
      additionalInfo = "أجسام الدودة النشطة: ${snake.size} | تحكم كامل باللمس والسحب",
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Game Board Canvas with both Tap and Drag/Swipe gesture controls
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.Center,
    ) {
      BoxWithConstraints(
        modifier = Modifier
          .aspectRatio(1f)
          .background(SurfaceDark, RoundedCornerShape(16.dp))
          .border(BorderStroke(1.dp, SurfaceBorder), RoundedCornerShape(16.dp))
          // Swipe in any direction
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              val (dx, dy) = dragAmount
              if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0 && direction != SnakeDirection.LEFT) nextDirection = SnakeDirection.RIGHT
                else if (dx < 0 && direction != SnakeDirection.RIGHT) nextDirection = SnakeDirection.LEFT
              } else {
                if (dy > 0 && direction != SnakeDirection.DOWN && dy < 0) nextDirection = SnakeDirection.UP
                else if (dy > 0 && direction != SnakeDirection.UP) nextDirection = SnakeDirection.DOWN
                else if (dy < 0 && direction != SnakeDirection.DOWN) nextDirection = SnakeDirection.UP
              }
            }
          }
          // Tap anywhere on the board relative to center to change direction
          .pointerInput(Unit) {
            detectTapGestures { tapOffset ->
              val cx = size.width / 2f
              val cy = size.height / 2f
              val dx = tapOffset.x - cx
              val dy = tapOffset.y - cy
              if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0 && direction != SnakeDirection.LEFT) nextDirection = SnakeDirection.RIGHT
                else if (dx < 0 && direction != SnakeDirection.RIGHT) nextDirection = SnakeDirection.LEFT
              } else {
                if (dy > 0 && direction != SnakeDirection.UP) nextDirection = SnakeDirection.DOWN
                else if (dy < 0 && direction != SnakeDirection.DOWN) nextDirection = SnakeDirection.UP
              }
            }
          }
      ) {
        val cellPx = constraints.maxWidth / gridSize.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
          // Draw subtle background grid
          for (i in 0..gridSize) {
            drawLine(
              color = SurfaceBorder.copy(alpha = 0.4f),
              start = Offset(i * cellPx, 0f),
              end = Offset(i * cellPx, size.height),
              strokeWidth = 0.5.dp.toPx(),
            )
            drawLine(
              color = SurfaceBorder.copy(alpha = 0.4f),
              start = Offset(0f, i * cellPx),
              end = Offset(size.width, i * cellPx),
              strokeWidth = 0.5.dp.toPx(),
            )
          }

          // Draw regular food
          drawCircle(
            color = TierCritical,
            radius = cellPx * 0.4f,
            center = Offset((food.x + 0.5f) * cellPx, (food.y + 0.5f) * cellPx),
          )

          // Draw golden food if active
          goldenFood?.let { gf ->
            drawCircle(
              color = TierFair,
              radius = cellPx * 0.48f,
              center = Offset((gf.x + 0.5f) * cellPx, (gf.y + 0.5f) * cellPx),
            )
          }

          // Draw Snake body
          snake.forEachIndexed { index, pt ->
            val isHead = index == 0
            val color = if (isHead) CyanGlow else CyanPrimary.copy(alpha = 0.9f - (index * 0.015f).coerceAtMost(0.4f))
            drawRoundRect(
              color = color,
              topLeft = Offset(pt.x * cellPx + 1.dp.toPx(), pt.y * cellPx + 1.dp.toPx()),
              size = Size(cellPx - 2.dp.toPx(), cellPx - 2.dp.toPx()),
              cornerRadius = CornerRadius(if (isHead) 6.dp.toPx() else 4.dp.toPx()),
            )
          }
        }

        // Game Over Overlay
        if (isGameOver) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(20.dp)
            ) {
              Text(
                text = "انتهت اللعبة!",
                style = MaterialTheme.typography.titleLarge.copy(
                  color = TierCritical,
                  fontWeight = FontWeight.Bold,
                ),
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "النقاط المحققة: $score",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
              )
              Spacer(modifier = Modifier.height(14.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                  onClick = { restartGame() },
                  colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("snake_replay_button"),
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("إعادة اللعب")
                }
                Button(
                  onClick = {
                    val finalStats = tracker.stopTracking()
                    onFinishTest(finalStats)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("snake_view_report_button"),
                ) {
                  Text("تقرير الأداء")
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Touch Instruction Badge (Replacing bottom D-pad buttons completely as requested)
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = SurfaceCard,
      border = BorderStroke(1.dp, SurfaceBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.TouchApp,
          contentDescription = null,
          tint = CyanPrimary,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "التحكم باللمس: اسحب أو انقر في أي اتجاه على الرقعة لتوجيه حركة الدودة",
          style = MaterialTheme.typography.labelMedium.copy(
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
          ),
        )
      }
    }
  }
}
