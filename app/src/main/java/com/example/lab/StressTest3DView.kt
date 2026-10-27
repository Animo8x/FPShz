package com.example.lab

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// 3D Point in World Space
class Vec3(var x: Float, var y: Float, var z: Float)

// 3D Polyhedron representation
class Mesh3D(
  val type: Int, // 0: Cube, 1: Octahedron, 2: Pyramid
  var orbitRadius: Float,
  var orbitSpeed: Float,
  var orbitAngle: Float,
  var orbitY: Float,
  var rotX: Float,
  var rotY: Float,
  var rotZ: Float,
  var vRotX: Float,
  var vRotY: Float,
  var vRotZ: Float,
  val baseScaleFactor: Float,
  val baseColor: Color,
) {
  // Preallocated world vertices buffer to avoid allocations in draw loop
  val worldVertices = Array(8) { Vec3(0f, 0f, 0f) }
  val projectedPoints = Array(8) { Offset(0f, 0f) }
  var centerZ: Float = 0f
}

// 3D Projected Polygon Face for Depth Sorting
class ProjectedFace(
  val p0: Offset,
  val p1: Offset,
  val p2: Offset,
  val p3: Offset?,
  val depthZ: Float,
  val shadedColor: Color,
  val edgeColor: Color,
)

// Base geometry models (Normalized unit vertices for cube)
val CubeBaseVertices = arrayOf(
  Vec3(-1f, -1f, -1f), Vec3(1f, -1f, -1f), Vec3(1f, 1f, -1f), Vec3(-1f, 1f, -1f),
  Vec3(-1f, -1f, 1f),  Vec3(1f, -1f, 1f),  Vec3(1f, 1f, 1f),  Vec3(-1f, 1f, 1f)
)

val CubeFaces = arrayOf(
  intArrayOf(0, 1, 2, 3), // Front
  intArrayOf(5, 4, 7, 6), // Back
  intArrayOf(4, 0, 3, 7), // Left
  intArrayOf(1, 5, 6, 2), // Right
  intArrayOf(4, 5, 1, 0), // Top
  intArrayOf(3, 2, 6, 7), // Bottom
)

@Composable
fun StressTest3DView(
  onFinishTest: (TestSessionStats) -> Unit,
  onExit: () -> Unit,
) {
  val context = LocalContext.current
  val tracker = remember { LabSessionTracker(context, LabTestType.STRESS_3D) }
  val stats by tracker.stats.collectAsState()

  // Object count constrained strictly between 1 and 1000
  var objectCount by remember { mutableIntStateOf(50) }
  var quality by remember { mutableStateOf(QualityLevel.MEDIUM) }
  var animTick by remember { mutableFloatStateOf(0f) }

  // Preallocated pool of 1000 3D meshes with golden ratio spatial distribution
  val meshPool = remember {
    val list = mutableListOf<Mesh3D>()
    val colors = listOf(
      CyanPrimary,
      CyanGlow,
      CyanSecondary,
      Color(0xFF60A5FA), // Blue 400
      Color(0xFF818CF8), // Indigo 400
      Color(0xFFA78BFA), // Purple 400
      Color(0xFF38BDF8), // Sky 400
      Color(0xFF22D3EE), // Cyan 400
      Color(0xFFF472B6), // Pink 400
    )

    val goldenRatio = 0.61803398875f
    repeat(1000) { index ->
      // Golden spiral distribution spreading beautifully across 3D spherical volume
      val radius = 45f + (index % 30) * 8.5f // Radius ranges from 45f to 300f
      val speed = (0.010f + (index % 8) * 0.0025f) * (if (index % 2 == 0) 1f else -1f)
      val y = (((index % 25) - 12) * 12.5f).coerceIn(-150f, 150f)

      list.add(
        Mesh3D(
          type = index % 3,
          orbitRadius = radius,
          orbitSpeed = speed,
          orbitAngle = (index * goldenRatio * 6.28318f),
          orbitY = y,
          rotX = (index * 0.35f),
          rotY = (index * 0.45f),
          rotZ = (index * 0.25f),
          vRotX = 0.02f + (index % 5) * 0.006f,
          vRotY = 0.022f + (index % 4) * 0.007f,
          vRotZ = 0.018f + (index % 6) * 0.005f,
          baseScaleFactor = 0.95f + (index % 5) * 0.10f,
          baseColor = colors[index % colors.size],
        )
      )
    }
    list
  }

  // Lifecycle
  DisposableEffect(Unit) {
    tracker.startTracking(initialObjectCount = objectCount)
    onDispose { tracker.stopTracking() }
  }

  BackHandler {
    val finalStats = tracker.stopTracking()
    onFinishTest(finalStats)
  }

  // Animation and physics update loop
  LaunchedEffect(objectCount) {
    tracker.updateObjectCount(objectCount)
    while (true) {
      delay(16L) // ~60 FPS update
      animTick += 0.025f
      val activeCount = objectCount.coerceIn(1, 1000)
      for (i in 0 until activeCount) {
        val m = meshPool[i]
        m.orbitAngle += m.orbitSpeed
        m.rotX += m.vRotX
        m.rotY += m.vRotY
        m.rotZ += m.vRotZ
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
          modifier = Modifier.testTag("stress3d_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة", tint = TextPrimary)
        }
        Text(
          text = "3D Stress Test — ثقيل",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
          ),
        )
      }

      // Quality Toggle
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
            modifier = Modifier.testTag("stress_quality_label"),
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
      additionalInfo = "المجسمات ثلاثية الأبعاد: $objectCount / 1000 | حجم مكبر واضح ومجسم",
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 3D Render Canvas with STRICT CLIPPING BOUNDS to completely prevent overflow over HUD
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(18.dp))
        .border(BorderStroke(1.dp, SurfaceBorder), RoundedCornerShape(18.dp))
        .background(SurfaceDark)
        .clipToBounds(),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .clipToBounds()
      ) {
        // Enforce strict canvas drawing boundary clipping
        clipRect(0f, 0f, size.width, size.height) {
          val w = size.width
          val h = size.height
          val cx = w / 2f
          val cy = h / 2f
          val focalLength = 360f
          val cameraZ = 430f

          // Directional 3D Light Vector (Normalized)
          val lx = 0.577f
          val ly = -0.577f
          val lz = -0.577f

          // Prominent, large, substantial 3D scales across all counts
          val globalScale = when {
            objectCount <= 15 -> 54f   // Very large imposing cubes
            objectCount <= 50 -> 42f   // Large detailed cubes
            objectCount <= 150 -> 32f  // Chunky, bold 3D geometry
            objectCount <= 350 -> 24f  // Clearly visible solid polyhedra
            objectCount <= 650 -> 18f  // Substantial, solid 3D cubes
            else -> 14f                // At 1000: large tangible 3D cubes (3.5x larger than before!)
          }

          val facesToRender = mutableListOf<ProjectedFace>()
          val activeCount = objectCount.coerceIn(1, 1000)

          for (i in 0 until activeCount) {
            val mesh = meshPool[i]

            // 1. Bounded orbital world position
            val worldCenterX = cos(mesh.orbitAngle) * mesh.orbitRadius
            val worldCenterY = mesh.orbitY + sin(animTick + i) * 12f
            val worldCenterZ = sin(mesh.orbitAngle) * mesh.orbitRadius
            mesh.centerZ = worldCenterZ

            // Rotation angles
            val cosX = cos(mesh.rotX); val sinX = sin(mesh.rotX)
            val cosY = cos(mesh.rotY); val sinY = sin(mesh.rotY)
            val cosZ = cos(mesh.rotZ); val sinZ = sin(mesh.rotZ)

            val objectScale = globalScale * mesh.baseScaleFactor

            // 2. Transform 8 local vertices of the Cube
            for (vIdx in 0 until 8) {
              val baseV = CubeBaseVertices[vIdx]
              val sx = baseV.x * objectScale
              val sy = baseV.y * objectScale
              val sz = baseV.z * objectScale

              // Rotate Y
              val x1 = sx * cosY + sz * sinY
              val y1 = sy
              val z1 = -sx * sinY + sz * cosY

              // Rotate X
              val x2 = x1
              val y2 = y1 * cosX - z1 * sinX
              val z2 = y1 * sinX + z1 * cosX

              // Rotate Z
              val x3 = x2 * cosZ - y2 * sinZ
              val y3 = x2 * sinZ + y2 * cosZ
              val z3 = z2

              // Translate to World Space
              val wx = x3 + worldCenterX
              val wy = y3 + worldCenterY
              val wz = z3 + worldCenterZ

              mesh.worldVertices[vIdx].x = wx
              mesh.worldVertices[vIdx].y = wy
              mesh.worldVertices[vIdx].z = wz

              // Safe Perspective Projection bounded within viewport
              val pZ = wz + cameraZ
              val scale = if (pZ > 15f) focalLength / pZ else 0.1f
              val px = (cx + wx * scale).coerceIn(-10f, w + 10f)
              val py = (cy + wy * scale).coerceIn(-10f, h + 10f)
              mesh.projectedPoints[vIdx] = Offset(px, py)
            }

            // 3. Process Faces with 3D Normal & Lighting
            for (face in CubeFaces) {
              val v0 = mesh.worldVertices[face[0]]
              val v1 = mesh.worldVertices[face[1]]
              val v2 = mesh.worldVertices[face[2]]

              // Surface normal
              val ax = v1.x - v0.x; val ay = v1.y - v0.y; val az = v1.z - v0.z
              val bx = v2.x - v0.x; val by = v2.y - v0.y; val bz = v2.z - v0.z

              val nx = ay * bz - az * by
              val ny = az * bx - ax * bz
              val nz = ax * by - ay * bx

              // Back-face Culling
              if (nz < 0) {
                val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
                val normX = nx / len; val normY = ny / len; val normZ = nz / len

                // Diffuse Shading
                val dotLight = (normX * lx + normY * ly + normZ * lz)
                val diffuse = max(0.22f, dotLight)

                val specular = if (quality == QualityLevel.HIGH && dotLight > 0.60f) {
                  (dotLight - 0.60f) * 0.85f
                } else 0f

                val r = min(1f, mesh.baseColor.red * diffuse + specular)
                val g = min(1f, mesh.baseColor.green * diffuse + specular)
                val b = min(1f, mesh.baseColor.blue * diffuse + specular)
                val shadedColor = Color(r, g, b, 0.95f)
                val edgeColor = mesh.baseColor.copy(alpha = 0.7f)

                val avgFaceZ = (v0.z + v1.z + v2.z + mesh.worldVertices[face[3]].z) / 4f

                facesToRender.add(
                  ProjectedFace(
                    p0 = mesh.projectedPoints[face[0]],
                    p1 = mesh.projectedPoints[face[1]],
                    p2 = mesh.projectedPoints[face[2]],
                    p3 = mesh.projectedPoints[face[3]],
                    depthZ = avgFaceZ,
                    shadedColor = shadedColor,
                    edgeColor = edgeColor,
                  )
                )
              }
            }
          }

          // 4. Depth Sorting (Painter's Algorithm)
          facesToRender.sortByDescending { it.depthZ }

          // 5. Draw 3D Polygons
          val path = Path()
          for (f in facesToRender) {
            path.reset()
            path.moveTo(f.p0.x, f.p0.y)
            path.lineTo(f.p1.x, f.p1.y)
            path.lineTo(f.p2.x, f.p2.y)
            f.p3?.let { path.lineTo(it.x, it.y) }
            path.close()

            // Fill shaded facet
            drawPath(path = path, color = f.shadedColor)

            // Distinct geometry borders for crisp, visible edges
            if (activeCount <= 400 || quality != QualityLevel.LOW) {
              drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.35f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx()),
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Interactive Slider and Controls (Min 1, Max 1000)
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = SurfaceCard,
      border = BorderStroke(1.dp, SurfaceBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // Top row: Counter Title & Stepper
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "عدد المجسمات: ",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary,
                ),
              )
              Text(
                text = "$objectCount",
                modifier = Modifier.testTag("stress_object_count_text"),
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Black,
                  color = when {
                    objectCount <= 100 -> TierGood
                    objectCount <= 300 -> CyanPrimary
                    objectCount <= 600 -> TierFair
                    else -> TierCritical
                  },
                  fontFamily = FontFamily.Monospace,
                ),
              )
              Text(
                text = " / 1000",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
              )
            }
            Text(
              text = if (objectCount == 1000) "الحد الأقصى المسموح (1000 مجسم)" else "اسحب الشريط لتغيير الحمل (1 إلى 1000)",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (objectCount == 1000) TierCritical else CyanGlow,
                fontSize = 11.sp,
              ),
            )
          }

          // Stepper Buttons (-25 / +25)
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { if (objectCount > 1) objectCount = (objectCount - 25).coerceAtLeast(1) },
              modifier = Modifier
                .size(34.dp)
                .background(SurfaceDark, CircleShape)
                .testTag("stress_decrement_btn"),
            ) {
              Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
              onClick = { if (objectCount < 1000) objectCount = (objectCount + 25).coerceAtMost(1000) },
              modifier = Modifier
                .size(34.dp)
                .background(SurfaceDark, CircleShape)
                .testTag("stress_increment_btn"),
            ) {
              Icon(Icons.Default.Add, contentDescription = "زيادة", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Interactive Slider (1 to 1000)
        Slider(
          value = objectCount.toFloat(),
          onValueChange = { objectCount = it.roundToInt().coerceIn(1, 1000) },
          valueRange = 1f..1000f,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("stress_object_slider"),
          colors = SliderDefaults.colors(
            thumbColor = CyanPrimary,
            activeTrackColor = CyanPrimary,
            inactiveTrackColor = SurfaceDark,
          ),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Preset Chips (1, 50, 100, 250, 500, 750, 1000)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf(1, 50, 100, 250, 500, 750, 1000).forEach { tier ->
            val isSelected = objectCount == tier
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CyanPrimary else SurfaceDark,
              border = BorderStroke(1.dp, if (isSelected) CyanGlow else SurfaceBorder),
              modifier = Modifier
                .height(30.dp)
                .testTag("preset_${tier}_chip"),
              onClick = { objectCount = tier },
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 10.dp),
              ) {
                Text(
                  text = if (tier == 1000) "1000 أقصى" else "$tier",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.Black else TextSecondary,
                    fontSize = 11.sp,
                  ),
                )
              }
            }
          }
        }
      }
    }
  }
}
