package com.zhelearn.CSUSTPlanet.feature.common.map

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.zhelearn.CSUSTPlanet.feature.common.data.remote.dto.CampusMapFeature
import com.zhelearn.CSUSTPlanet.feature.common.data.remote.dto.CampusMapRoad
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.CampusMapViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 不依赖第三方地图 SDK 的校园地图渲染器。
 *
 * 服务端 GeoJSON 保持 WGS-84；这里在内存中投影为校园级米制坐标，再由 Canvas 完成绘制、
 * 手势和命中检测。搜索过滤仍由 ViewModel 负责，本组件只消费命中 ID 并绘制高光。
 */
@Composable
fun IllustratedCampusMap(
    allBuildings: List<CampusMapFeature>,
    allRoads: List<CampusMapRoad>,
    interactiveBuildingIds: Set<String>,
    selectedCampus: Campus,
    selectedCategory: String?,
    searchText: String,
    selectedBuildingId: String?,
    cameraTarget: CampusMapViewModel.CameraTarget?,
    initialZoom: Float = CAMPUS_MAP_DEFAULT_ZOOM,
    onCameraTargetConsumed: () -> Unit,
    onBuildingSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val campusBuildings = remember(allBuildings, selectedCampus) {
        allBuildings.filter { it.properties.campus == selectedCampus.rawName }
    }
    val isDarkTheme = isSystemInDarkTheme()
    val palette = remember(isDarkTheme) { CampusMapPalette(isDarkTheme) }
    val campusRoads = remember(allRoads, selectedCampus) {
        allRoads.filter { it.properties.campus == selectedCampus.rawName }
    }
    val scene = remember(campusBuildings, campusRoads, palette) {
        buildCampusCanvasScene(campusBuildings, campusRoads, palette)
    }
    val backgroundPainter = remember(palette) {
        MeshGradientPainter(rows = 1, columns = 1, hasBicubicColor = true) {
            setVertex(0, 0, Offset(0f, 0f), palette.backgroundTopLeft)
            setVertex(0, 1, Offset(1f, 0f), palette.backgroundTopRight)
            setVertex(1, 0, Offset(0f, 1f), palette.backgroundBottomLeft)
            setVertex(1, 1, Offset(1f, 1f), palette.backgroundBottomRight)
        }
    }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    val startingZoom = initialZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
    val zoomState = remember { mutableFloatStateOf(startingZoom) }
    val panState = remember { mutableStateOf(Offset.Zero) }
    val currentOnBuildingSelected by rememberUpdatedState(onBuildingSelected)

    LaunchedEffect(selectedCampus, initialZoom) {
        val resetZoom = initialZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        zoomState.floatValue = resetZoom
        panState.value = Offset.Zero
    }

    LaunchedEffect(scene.key, cameraTarget?.token, viewportSize) {
        val target = cameraTarget ?: return@LaunchedEffect
        if (scene.features.isEmpty() || viewportSize == IntSize.Zero) return@LaunchedEffect

        val viewport = Size(viewportSize.width.toFloat(), viewportSize.height.toFloat())
        val baseScale = canvasBaseScale(viewport, scene.cameraBounds)
        val targetWorld = scene.projection.project(target.lat, target.lon)
        val targetSpanMeters = (target.span * METERS_PER_LATITUDE_DEGREE).toFloat()
        val desiredScale = min(viewport.width, viewport.height) / (targetSpanMeters * 1.12f)
        val targetZoom = (desiredScale / baseScale).coerceIn(MIN_ZOOM, MAX_ZOOM)

        val targetPan = clampCampusPan(
            proposedPan = (scene.cameraBounds.center - targetWorld) * (baseScale * targetZoom),
            zoom = targetZoom,
            viewport = viewport,
            worldBounds = scene.cameraBounds,
        )
        zoomState.floatValue = targetZoom
        panState.value = targetPan
        onCameraTargetConsumed()
    }

    val filterActive = searchText.isNotBlank() || selectedCategory != null
    val searchActive = searchText.isNotBlank()
    val highlightReveal = remember { Animatable(1f) }

    LaunchedEffect(searchText, selectedBuildingId) {
        if (searchActive || selectedBuildingId != null) {
            highlightReveal.snapTo(0f)
            highlightReveal.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        } else {
            highlightReveal.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .paint(backgroundPainter)
            .onSizeChanged { viewportSize = it }
            .pointerInput(scene.key, viewportSize, interactiveBuildingIds) {
                detectTapGestures { screenPoint ->
                    if (viewportSize == IntSize.Zero || scene.features.isEmpty()) {
                        return@detectTapGestures
                    }
                    val camera = CampusCanvasCamera(
                        viewport = Size(viewportSize.width.toFloat(), viewportSize.height.toFloat()),
                        worldBounds = scene.cameraBounds,
                        zoom = zoomState.floatValue,
                        pan = panState.value,
                    )
                    val worldPoint = camera.screenToWorld(screenPoint)
                    scene.features
                        .asReversed()
                        .firstOrNull { feature ->
                            feature.id in interactiveBuildingIds && feature.contains(worldPoint)
                        }
                        ?.let { currentOnBuildingSelected(it.id) }
                }
            }
            .pointerInput(scene.key, viewportSize) {
                detectTransformGestures { centroid, panChange, zoomChange, _ ->
                    if (viewportSize == IntSize.Zero) return@detectTransformGestures

                    val previousZoom = zoomState.floatValue
                    val previousPan = panState.value
                    val nextZoom = (previousZoom * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    val zoomRatio = nextZoom / previousZoom
                    val viewport = Size(viewportSize.width.toFloat(), viewportSize.height.toFloat())
                    val viewportCenter = Offset(viewport.width / 2f, viewport.height / 2f)
                    val centroidFromCenter = centroid - viewportCenter
                    val proposedPan = centroidFromCenter -
                        (centroidFromCenter - previousPan) * zoomRatio +
                        panChange
                    val nextPan = clampCampusPan(
                        proposedPan = proposedPan,
                        zoom = nextZoom,
                        viewport = viewport,
                        worldBounds = scene.cameraBounds,
                    )
                    panState.value = nextPan
                    zoomState.floatValue = nextZoom
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = panState.value.x
                    translationY = panState.value.y
                    scaleX = zoomState.floatValue
                    scaleY = zoomState.floatValue
                    transformOrigin = TransformOrigin.Center
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (scene.features.isEmpty()) return@Canvas

                val baseCamera = CampusCanvasCamera(
                    viewport = size,
                    worldBounds = scene.cameraBounds,
                    zoom = 1f,
                    pan = Offset.Zero,
                )
                withTransform({
                    translate(left = baseCamera.origin.x, top = baseCamera.origin.y)
                    scale(scaleX = baseCamera.scale, scaleY = baseCamera.scale, pivot = Offset.Zero)
                }) {
                    drawCampusPaper(scene, baseCamera, palette)
                    drawCampusBaseLayers(
                        scene = scene,
                        interactiveBuildingIds = interactiveBuildingIds,
                        filterActive = filterActive,
                        camera = baseCamera,
                        palette = palette,
                    )
                }
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (scene.features.isEmpty()) return@Canvas

                val baseCamera = CampusCanvasCamera(
                    viewport = size,
                    worldBounds = scene.cameraBounds,
                    zoom = 1f,
                    pan = Offset.Zero,
                )
                withTransform({
                    translate(left = baseCamera.origin.x, top = baseCamera.origin.y)
                    scale(scaleX = baseCamera.scale, scaleY = baseCamera.scale, pivot = Offset.Zero)
                }) {
                    drawCampusHighlights(
                        scene = scene,
                        interactiveBuildingIds = interactiveBuildingIds,
                        selectedBuildingId = selectedBuildingId,
                        searchActive = searchActive,
                        revealProgress = highlightReveal.value,
                        camera = baseCamera,
                        palette = palette,
                    )
                }
            }
            CampusMapLabelLayer(
                scene = scene,
                viewportSize = viewportSize,
                interactiveBuildingIds = interactiveBuildingIds,
                selectedBuildingId = selectedBuildingId,
                filterActive = filterActive,
                searchActive = searchActive,
                currentZoom = { zoomState.floatValue },
                palette = palette,
            )
        }
        Text(
            text = "© OpenStreetMap contributors",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp),
            color = palette.labelInk.copy(alpha = 0.68f),
            fontSize = 9.sp,
        )
    }
}

private fun DrawScope.drawCampusPaper(
    scene: CampusCanvasScene,
    camera: CampusCanvasCamera,
    palette: CampusMapPalette,
) {
    val shadowOffset = 8f / camera.scale
    if (scene.boundaries.isNotEmpty()) {
        for (boundary in scene.boundaries) {
            withTransform({ translate(left = shadowOffset, top = shadowOffset) }) {
                drawPath(boundary.path, palette.campusShadow)
            }
            drawPath(boundary.path, boundary.fillBrush)
            drawPath(
                path = boundary.path,
                color = palette.campusBorder,
                style = Stroke(width = 2.2f / camera.scale),
            )
            clipPath(boundary.path) {
                scene.paperGrain.forEachIndexed { index, point ->
                    drawCircle(
                        color = if (index % 3 == 0) palette.grainLight else palette.grainDark,
                        radius = (if (index % 5 == 0) 1.4f else 0.8f) / camera.scale,
                        center = point,
                    )
                }
            }
        }
        return
    }

    val margin = min(scene.bounds.width, scene.bounds.height) * 0.045f
    val paperBounds = scene.bounds.inflate(margin)
    val cornerRadius = CornerRadius(24f / camera.scale)
    drawRoundRect(
        color = palette.campusShadow,
        topLeft = paperBounds.topLeft + Offset(shadowOffset, shadowOffset),
        size = paperBounds.size,
        cornerRadius = cornerRadius,
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(palette.campusPaperLight, palette.campusPaper),
            start = paperBounds.topLeft,
            end = paperBounds.bottomRight,
        ),
        topLeft = paperBounds.topLeft,
        size = paperBounds.size,
        cornerRadius = cornerRadius,
    )
    drawRoundRect(
        color = palette.campusBorder,
        topLeft = paperBounds.topLeft,
        size = paperBounds.size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 2f / camera.scale),
    )
    clipRect(paperBounds.left, paperBounds.top, paperBounds.right, paperBounds.bottom) {
        scene.paperGrain.forEachIndexed { index, point ->
            drawCircle(
                color = if (index % 3 == 0) palette.grainLight else palette.grainDark,
                radius = (if (index % 5 == 0) 1.4f else 0.8f) / camera.scale,
                center = point,
            )
        }
    }
}

private fun DrawScope.drawCampusBaseLayers(
    scene: CampusCanvasScene,
    interactiveBuildingIds: Set<String>,
    filterActive: Boolean,
    camera: CampusCanvasCamera,
    palette: CampusMapPalette,
) {
    val visibleWorldBounds = camera.visibleWorldBounds(extraScreenPadding = 30f)
    for (feature in scene.features) {
        if (feature.kind != CampusFeatureKind.Water && feature.kind != CampusFeatureKind.Green) continue
        if (!feature.bounds.overlaps(visibleWorldBounds)) continue
        val interactive = feature.id in interactiveBuildingIds
        val dimmed = filterActive && !interactive
        val contentAlpha = if (dimmed) 0.13f else 1f
        if (feature.kind == CampusFeatureKind.Water) {
            drawWaterFeature(feature, contentAlpha, camera)
        } else {
            drawGreenFeature(feature, contentAlpha, camera)
        }
    }

    drawCampusRoads(scene.roads, visibleWorldBounds, camera, palette)

    for (feature in scene.features) {
        if (feature.kind == CampusFeatureKind.Water || feature.kind == CampusFeatureKind.Green) continue
        if (!feature.bounds.overlaps(visibleWorldBounds)) continue
        val interactive = feature.id in interactiveBuildingIds
        val dimmed = filterActive && !interactive
        val contentAlpha = if (dimmed) 0.13f else 1f
        if (feature.kind == CampusFeatureKind.Sport) {
            drawSportFeature(feature, contentAlpha, camera, palette)
        } else {
            drawBuildingFeature(feature, contentAlpha, dimmed, camera, palette)
        }
    }
}

private fun DrawScope.drawCampusHighlights(
    scene: CampusCanvasScene,
    interactiveBuildingIds: Set<String>,
    selectedBuildingId: String?,
    searchActive: Boolean,
    revealProgress: Float,
    camera: CampusCanvasCamera,
    palette: CampusMapPalette,
) {
    val visibleWorldBounds = camera.visibleWorldBounds(extraScreenPadding = 30f)
    val revealedAlpha = 0.72f + revealProgress * 0.28f
    if (searchActive) {
        scene.features.forEach { feature ->
            if (feature.id !in interactiveBuildingIds || !feature.bounds.overlaps(visibleWorldBounds)) {
                return@forEach
            }
            drawCampusFeatureOutline(
                feature = feature,
                color = palette.searchGlow.copy(alpha = (0.18f + revealProgress * 0.14f)),
                width = (11f + revealProgress * 3f) / camera.scale,
            )
            drawCampusFeatureOutline(
                feature = feature,
                color = palette.search.copy(alpha = revealedAlpha),
                width = 4f / camera.scale,
            )
        }
    }

    scene.features.firstOrNull { feature ->
        feature.id == selectedBuildingId && feature.id in interactiveBuildingIds
    }?.let { selected ->
        drawCampusFeatureOutline(
            feature = selected,
            color = palette.selectionGlow.copy(alpha = (0.18f + revealProgress * 0.14f)),
            width = (12f + revealProgress * 4f) / camera.scale,
        )
        drawCampusFeatureOutline(
            feature = selected,
            color = palette.selection.copy(alpha = revealedAlpha),
            width = 4.5f / camera.scale,
        )
    }
}

private fun DrawScope.drawCampusRoads(
    roads: List<ProjectedCampusRoad>,
    visibleWorldBounds: Rect,
    camera: CampusCanvasCamera,
    palette: CampusMapPalette,
) {
    val footwayDash = PathEffect.dashPathEffect(
        intervals = floatArrayOf(7f / camera.scale, 5f / camera.scale),
    )
    for (road in roads) {
        if (!road.bounds.overlaps(visibleWorldBounds)) continue
        val minimumWidth = when (road.kind) {
            CampusRoadKind.Main -> 5.5f / camera.scale
            CampusRoadKind.Service -> 4f / camera.scale
            CampusRoadKind.Footway -> 2.4f / camera.scale
        }
        val roadWidth = road.width.coerceAtLeast(minimumWidth)
        val casingWidth = roadWidth + 3f / camera.scale
        drawPath(
            path = road.path,
            color = palette.roadCasing,
            style = Stroke(width = casingWidth, cap = StrokeCap.Round),
        )
        drawPath(
            path = road.path,
            color = when (road.kind) {
                CampusRoadKind.Main -> palette.mainRoad
                CampusRoadKind.Service -> palette.serviceRoad
                CampusRoadKind.Footway -> palette.footway
            },
            style = Stroke(width = roadWidth, cap = StrokeCap.Round),
        )
        if (road.kind == CampusRoadKind.Footway && camera.zoom >= 1.05f) {
            drawPath(
                path = road.path,
                color = palette.roadHighlight,
                style = Stroke(
                    width = 0.9f / camera.scale,
                    cap = StrokeCap.Round,
                    pathEffect = footwayDash,
                ),
            )
        } else if (road.kind != CampusRoadKind.Footway) {
            drawPath(
                path = road.path,
                color = palette.roadHighlight,
                style = Stroke(width = 1f / camera.scale, cap = StrokeCap.Round),
            )
        }
    }
}

private fun DrawScope.drawBuildingFeature(
    feature: ProjectedCampusFeature,
    contentAlpha: Float,
    dimmed: Boolean,
    camera: CampusCanvasCamera,
    palette: CampusMapPalette,
) {
    val facadeOffset = Offset(2f / camera.scale, 3.5f / camera.scale)
    val shadowOffset = Offset(3f / camera.scale, 4.5f / camera.scale)
    withTransform({ translate(left = shadowOffset.x, top = shadowOffset.y) }) {
        drawPath(feature.path, palette.buildingShadow.copy(alpha = 0.08f * contentAlpha))
    }
    withTransform({ translate(left = facadeOffset.x, top = facadeOffset.y) }) {
        drawPath(feature.path, feature.sideColor.copy(alpha = contentAlpha))
        drawPath(
            path = feature.path,
            color = feature.outlineColor.copy(alpha = contentAlpha * 0.44f),
            style = Stroke(width = 0.8f / camera.scale, cap = StrokeCap.Round),
        )
    }
    if (contentAlpha == 1f) {
        drawPath(path = feature.path, brush = feature.fillBrush)
    } else {
        drawPath(path = feature.path, color = feature.baseColor.copy(alpha = contentAlpha))
    }
    drawPath(
        path = feature.path,
        color = feature.outlineColor.copy(alpha = contentAlpha),
        style = Stroke(width = 1f / camera.scale, cap = StrokeCap.Round),
    )

    if (!dimmed) {
        drawPath(
            path = feature.path,
            color = palette.buildingRoofHighlight,
            style = Stroke(width = 0.5f / camera.scale, cap = StrokeCap.Round),
        )
    }
}

private fun DrawScope.drawWaterFeature(
    feature: ProjectedCampusFeature,
    contentAlpha: Float,
    camera: CampusCanvasCamera,
) {
    if (contentAlpha == 1f) drawPath(feature.path, feature.fillBrush)
    else drawPath(feature.path, feature.baseColor.copy(alpha = contentAlpha))
    drawPath(
        path = feature.path,
        color = feature.outlineColor.copy(alpha = contentAlpha),
        style = Stroke(width = 2.5f / camera.scale),
    )
    if (contentAlpha == 1f && feature.bounds.height * camera.scale > 24f) {
        clipPath(feature.path) {
            repeat(3) { index ->
                val y = feature.bounds.top + feature.bounds.height * (index + 1) / 4f
                drawLine(
                    color = Color.White.copy(alpha = 0.38f),
                    start = Offset(feature.bounds.left, y),
                    end = Offset(feature.bounds.right, y),
                    strokeWidth = 1.4f / camera.scale,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.drawGreenFeature(
    feature: ProjectedCampusFeature,
    contentAlpha: Float,
    camera: CampusCanvasCamera,
) {
    if (contentAlpha == 1f) drawPath(feature.path, feature.fillBrush)
    else drawPath(feature.path, feature.baseColor.copy(alpha = contentAlpha))
    drawPath(
        path = feature.path,
        color = feature.outlineColor.copy(alpha = contentAlpha),
        style = Stroke(width = 1f / camera.scale),
    )
}

private fun DrawScope.drawSportFeature(
    feature: ProjectedCampusFeature,
    contentAlpha: Float,
    camera: CampusCanvasCamera,
    palette: CampusMapPalette,
) {
    if (contentAlpha == 1f) drawPath(feature.path, feature.fillBrush)
    else drawPath(feature.path, feature.baseColor.copy(alpha = contentAlpha))
    drawPath(
        path = feature.path,
        color = feature.outlineColor.copy(alpha = contentAlpha),
        style = Stroke(width = 2.4f / camera.scale),
    )
    if (contentAlpha == 1f) {
        clipPath(feature.path) {
            val trackGeometry = feature.athleticsTrackGeometry
            if (trackGeometry != null) {
                withTransform({
                    rotate(
                        degrees = trackGeometry.angleDegrees,
                        pivot = trackGeometry.center,
                    )
                }) {
                    val trackBounds = Rect(
                        left = trackGeometry.center.x - trackGeometry.length / 2f,
                        top = trackGeometry.center.y - trackGeometry.width / 2f,
                        right = trackGeometry.center.x + trackGeometry.length / 2f,
                        bottom = trackGeometry.center.y + trackGeometry.width / 2f,
                    )
                    repeat(4) { lane ->
                        val inset = trackGeometry.width * 0.035f * (lane + 1)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.58f),
                            topLeft = trackBounds.topLeft + Offset(inset, inset),
                            size = Size(
                                width = trackBounds.width - inset * 2f,
                                height = trackBounds.height - inset * 2f,
                            ),
                            cornerRadius = CornerRadius((trackBounds.height - inset * 2f) / 2f),
                            style = Stroke(width = 0.8f / camera.scale),
                        )
                    }
                    val fieldInsetX = trackGeometry.length * 0.16f
                    val fieldInsetY = trackGeometry.width * 0.18f
                    val fieldBounds = Rect(
                        left = trackBounds.left + fieldInsetX,
                        top = trackBounds.top + fieldInsetY,
                        right = trackBounds.right - fieldInsetX,
                        bottom = trackBounds.bottom - fieldInsetY,
                    )
                    drawRoundRect(
                        color = palette.fieldSurface,
                        topLeft = fieldBounds.topLeft,
                        size = fieldBounds.size,
                        cornerRadius = CornerRadius(fieldBounds.height / 2f),
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.64f),
                        start = Offset(fieldBounds.center.x, fieldBounds.top),
                        end = Offset(fieldBounds.center.x, fieldBounds.bottom),
                        strokeWidth = 1f / camera.scale,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.64f),
                        radius = min(fieldBounds.width, fieldBounds.height) * 0.12f,
                        center = fieldBounds.center,
                        style = Stroke(width = 1f / camera.scale),
                    )
                }
            } else {
                drawLine(
                    color = Color.White.copy(alpha = 0.52f),
                    start = Offset(feature.bounds.center.x, feature.bounds.top),
                    end = Offset(feature.bounds.center.x, feature.bounds.bottom),
                    strokeWidth = 1.2f / camera.scale,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.52f),
                    radius = min(feature.bounds.width, feature.bounds.height) * 0.12f,
                    center = feature.bounds.center,
                    style = Stroke(width = 1.2f / camera.scale),
                )
            }
        }
    }
}

private fun isAthleticsTrack(name: String): Boolean =
    name.contains("田径") || name.contains("运动场") || name.contains("体育场")

private fun DrawScope.drawCampusFeatureOutline(
    feature: ProjectedCampusFeature,
    color: Color,
    width: Float,
) {
    drawPath(
        path = feature.path,
        color = color,
        style = Stroke(width = width, cap = StrokeCap.Round),
    )
}

@Composable
private fun CampusMapLabelLayer(
    scene: CampusCanvasScene,
    viewportSize: IntSize,
    interactiveBuildingIds: Set<String>,
    selectedBuildingId: String?,
    filterActive: Boolean,
    searchActive: Boolean,
    currentZoom: () -> Float,
    palette: CampusMapPalette,
) {
    if (viewportSize == IntSize.Zero || scene.labelFeatures.isEmpty()) return
    val density = LocalDensity.current
    val placements = remember(
        scene.key,
        viewportSize,
        interactiveBuildingIds,
        selectedBuildingId,
        filterActive,
        searchActive,
        density.density,
        density.fontScale,
    ) {
        buildCampusLabelPlacements(
            scene = scene,
            viewportSize = viewportSize,
            interactiveBuildingIds = interactiveBuildingIds,
            selectedBuildingId = selectedBuildingId,
            filterActive = filterActive,
            searchActive = searchActive,
            density = density,
        )
    }
    val emphasizedShape = RoundedCornerShape(5.dp)

    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            for (placement in placements) {
                key(placement.feature.id) {
                    val emphasized = placement.selected || placement.searchMatch
                    val emphasisColor = if (placement.selected) {
                        palette.selection.copy(alpha = 0.64f)
                    } else {
                        palette.search.copy(alpha = 0.58f)
                    }
                    Text(
                        text = placement.feature.name,
                        color = palette.labelInk,
                        fontSize = placement.fontSizeSp.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        style = TextStyle(
                            shadow = if (emphasized) null else Shadow(
                                color = palette.labelHalo,
                                offset = Offset.Zero,
                                blurRadius = 3f,
                            ),
                        ),
                        modifier = Modifier
                            .zIndex(
                                when {
                                    placement.selected -> 2f
                                    placement.searchMatch -> 1f
                                    else -> 0f
                                },
                            )
                            .graphicsLayer {
                                val zoom = currentZoom().coerceIn(MIN_ZOOM, MAX_ZOOM)
                                val compensatedZoom = 1f +
                                    (zoom - 1f) * LABEL_SCALE_COMPENSATION_STRENGTH
                                val labelScale = 1f / compensatedZoom
                                scaleX = labelScale
                                scaleY = labelScale
                                alpha = if (zoom >= placement.minimumZoom) 1f else 0f
                                transformOrigin = TransformOrigin.Center
                            }
                            .then(
                                if (emphasized) {
                                    Modifier
                                        .background(
                                            color = if (placement.selected) {
                                                palette.selectedLabel
                                            } else {
                                                palette.searchLabel
                                            },
                                            shape = emphasizedShape,
                                        )
                                        .border(
                                            width = 0.7.dp,
                                            color = emphasisColor,
                                            shape = emphasizedShape,
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.5.dp)
                                } else {
                                    Modifier.padding(horizontal = 1.dp, vertical = 0.5.dp)
                                },
                            ),
                    )
                }
            }
        },
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val anchor = placements[index].anchor
                placeable.placeRelative(
                    x = (anchor.x - placeable.width / 2f).roundToInt(),
                    y = (anchor.y - placeable.height / 2f).roundToInt(),
                )
            }
        }
    }
}

private fun buildCampusLabelPlacements(
    scene: CampusCanvasScene,
    viewportSize: IntSize,
    interactiveBuildingIds: Set<String>,
    selectedBuildingId: String?,
    filterActive: Boolean,
    searchActive: Boolean,
    density: Density,
): List<CampusLabelPlacement> {
    val viewport = Size(viewportSize.width.toFloat(), viewportSize.height.toFloat())
    val camera = CampusCanvasCamera(
        viewport = viewport,
        worldBounds = scene.cameraBounds,
        zoom = 1f,
        pan = Offset.Zero,
    )
    val labelPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    val occupiedBoundsByZoom = LABEL_ZOOM_LEVELS.map { mutableListOf<Rect>() }
    val placements = ArrayList<CampusLabelPlacement>(scene.labelFeatures.size)
    val viewportCenter = Offset(viewport.width / 2f, viewport.height / 2f)

    fun addPlacement(
        feature: ProjectedCampusFeature,
        selected: Boolean,
        searchMatch: Boolean,
    ) {
        val fontSizeSp = labelFontSizeSp(feature, selected, searchMatch)
        labelPaint.textSize = with(density) { fontSizeSp.sp.toPx() }
        val horizontalPadding = with(density) {
            (if (selected || searchMatch) 5.dp else 2.dp).toPx()
        }
        val verticalPadding = with(density) {
            (if (selected || searchMatch) 2.dp else 1.dp).toPx()
        }
        val labelWidth = labelPaint.measureText(feature.name) + horizontalPadding * 2f
        val metrics = labelPaint.fontMetrics
        val labelHeight = metrics.descent - metrics.ascent + verticalPadding * 2f
        val baseAnchor = camera.worldToScreen(feature.center)

        fun boundsAtZoom(zoom: Float): Rect {
            val anchor = viewportCenter + (baseAnchor - viewportCenter) * zoom
            return Rect(
                left = anchor.x - labelWidth / 2f,
                top = anchor.y - labelHeight / 2f,
                right = anchor.x + labelWidth / 2f,
                bottom = anchor.y + labelHeight / 2f,
            )
        }

        val emphasized = selected || searchMatch
        val firstVisibleZoomIndex = if (emphasized) {
            0
        } else {
            LABEL_ZOOM_LEVELS.indices.firstOrNull { zoomIndex ->
                val candidateBounds = boundsAtZoom(LABEL_ZOOM_LEVELS[zoomIndex])
                occupiedBoundsByZoom[zoomIndex].none { it.overlaps(candidateBounds) }
            } ?: LABEL_ZOOM_LEVELS.lastIndex
        }
        for (zoomIndex in firstVisibleZoomIndex..LABEL_ZOOM_LEVELS.lastIndex) {
            occupiedBoundsByZoom[zoomIndex] += boundsAtZoom(LABEL_ZOOM_LEVELS[zoomIndex])
        }
        placements += CampusLabelPlacement(
            feature = feature,
            anchor = baseAnchor,
            minimumZoom = LABEL_ZOOM_LEVELS[firstVisibleZoomIndex],
            fontSizeSp = fontSizeSp,
            selected = selected,
            searchMatch = searchMatch,
        )
    }

    scene.labelFeatures.firstOrNull { feature ->
        feature.id == selectedBuildingId && feature.id in interactiveBuildingIds
    }?.let { selected ->
        addPlacement(selected, selected = true, searchMatch = false)
    }
    if (searchActive) {
        for (feature in scene.labelFeatures) {
            if (feature.id == selectedBuildingId || feature.id !in interactiveBuildingIds) continue
            addPlacement(feature, selected = false, searchMatch = true)
        }
    }
    for (feature in scene.labelFeatures) {
        val interactive = feature.id in interactiveBuildingIds
        if (feature.id == selectedBuildingId || searchActive && interactive) continue
        if (filterActive && !interactive) continue
        addPlacement(feature, selected = false, searchMatch = false)
    }
    return placements
}

private fun labelFontSizeSp(
    feature: ProjectedCampusFeature,
    selected: Boolean,
    searchMatch: Boolean,
): Float = when {
    selected -> 10.5f
    searchMatch -> 9.5f
    labelPriority(feature) >= 3 -> 8.5f
    labelPriority(feature) >= 2 -> 8f
    else -> 7.5f
}

private data class CampusLabelPlacement(
    val feature: ProjectedCampusFeature,
    val anchor: Offset,
    val minimumZoom: Float,
    val fontSizeSp: Float,
    val selected: Boolean,
    val searchMatch: Boolean,
)

private fun labelPriority(feature: ProjectedCampusFeature): Int = when {
    feature.category == "图书馆" -> 3
    feature.category == "体育" -> 3
    feature.name.contains("教学") -> 2
    feature.name.contains("食堂") -> 2
    else -> 1
}

private data class CampusCanvasScene(
    val key: String,
    val boundaries: List<ProjectedCampusFeature>,
    val features: List<ProjectedCampusFeature>,
    val labelFeatures: List<ProjectedCampusFeature>,
    val roads: List<ProjectedCampusRoad>,
    val projection: CampusGeographicProjection,
    val bounds: Rect,
    val cameraBounds: Rect,
    val paperGrain: List<Offset>,
)

private data class ProjectedCampusRoad(
    val id: String,
    val kind: CampusRoadKind,
    val width: Float,
    val path: Path,
    val bounds: Rect,
)

private enum class CampusFeatureKind(val rawValue: String) {
    Boundary("boundary"),
    Water("water"),
    Green("green"),
    Sport("sport"),
    Building("building");

    companion object {
        fun fromRawValue(rawValue: String?): CampusFeatureKind? =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

private enum class CampusRoadKind(val rawValue: String) {
    Main("main"),
    Service("service"),
    Footway("footway");

    companion object {
        fun fromRawValue(rawValue: String): CampusRoadKind =
            entries.firstOrNull { it.rawValue == rawValue } ?: Footway
    }
}

private enum class CampusBuildingStyle {
    Teaching,
    Library,
    EastDormitory,
    WestDormitory,
    SouthDormitory,
    ZhichengDormitory,
    HongyiDormitory,
    XingjianDormitory,
    MinxingDormitory,
    Dormitory,
    Canteen,
    Administration,
    Leisure,
    Default,
}

private data class ProjectedCampusFeature(
    val id: String,
    val name: String,
    val category: String,
    val kind: CampusFeatureKind,
    val rings: List<List<Offset>>,
    val path: Path,
    val bounds: Rect,
    val center: Offset,
    val fillBrush: Brush,
    val baseColor: Color,
    val sideColor: Color,
    val outlineColor: Color,
    val athleticsTrackGeometry: AthleticsTrackGeometry?,
) {
    fun contains(point: Offset): Boolean {
        if (!point.isSpecified || !bounds.contains(point)) return false
        val outerRing = rings.firstOrNull() ?: return false
        if (!pointInRing(point, outerRing)) return false
        for (ringIndex in 1 until rings.size) {
            if (pointInRing(point, rings[ringIndex])) return false
        }
        return true
    }
}

private data class AthleticsTrackGeometry(
    val center: Offset,
    val angleDegrees: Float,
    val length: Float,
    val width: Float,
)

private data class CampusGeographicProjection(
    val originLatitude: Double,
    val originLongitude: Double,
    val longitudeMetersPerDegree: Double,
) {
    fun project(latitude: Double, longitude: Double): Offset = Offset(
        x = ((longitude - originLongitude) * longitudeMetersPerDegree).toFloat(),
        y = ((originLatitude - latitude) * METERS_PER_LATITUDE_DEGREE).toFloat(),
    )
}

private data class CampusCanvasCamera(
    val viewport: Size,
    val worldBounds: Rect,
    val zoom: Float,
    val pan: Offset,
) {
    private val baseScale = canvasBaseScale(viewport, worldBounds)
    val scale: Float = baseScale * zoom
    val origin: Offset = Offset(
        x = viewport.width / 2f + pan.x - worldBounds.center.x * scale,
        y = viewport.height / 2f + pan.y - worldBounds.center.y * scale,
    )

    fun worldToScreen(point: Offset): Offset = origin + point * scale

    fun screenToWorld(point: Offset): Offset = Offset(
        x = (point.x - origin.x) / scale,
        y = (point.y - origin.y) / scale,
    )

    fun visibleWorldBounds(extraScreenPadding: Float): Rect = Rect(
        left = (-origin.x - extraScreenPadding) / scale,
        top = (-origin.y - extraScreenPadding) / scale,
        right = (viewport.width - origin.x + extraScreenPadding) / scale,
        bottom = (viewport.height - origin.y + extraScreenPadding) / scale,
    )
}

private fun buildCampusCanvasScene(
    features: List<CampusMapFeature>,
    roads: List<CampusMapRoad>,
    palette: CampusMapPalette,
): CampusCanvasScene {
    var originLatitude = Double.NEGATIVE_INFINITY
    var originLongitude = Double.POSITIVE_INFINITY
    var latitudeSum = 0.0
    var coordinateCount = 0
    for (feature in features) {
        for (ring in feature.geometry.coordinates) {
            for (coordinate in ring) {
                val longitude = coordinate.getOrNull(0) ?: continue
                val latitude = coordinate.getOrNull(1) ?: continue
                originLatitude = max(originLatitude, latitude)
                originLongitude = min(originLongitude, longitude)
                latitudeSum += latitude
                coordinateCount++
            }
        }
    }
    if (coordinateCount == 0) return emptyCampusCanvasScene()

    val averageLatitudeRadians = Math.toRadians(latitudeSum / coordinateCount)
    val projection = CampusGeographicProjection(
        originLatitude = originLatitude,
        originLongitude = originLongitude,
        longitudeMetersPerDegree = METERS_PER_LATITUDE_DEGREE * cos(averageLatitudeRadians),
    )

    val projectedFeatures = buildList {
        for (feature in features) {
            val rings = buildList {
                for (ring in feature.geometry.coordinates) {
                    val projectedRing = buildList {
                        for (coordinate in ring) {
                            val longitude = coordinate.getOrNull(0) ?: continue
                            val latitude = coordinate.getOrNull(1) ?: continue
                            add(projection.project(latitude, longitude))
                        }
                    }
                    if (projectedRing.size >= 3) add(projectedRing)
                }
            }
            if (rings.isEmpty()) continue

            val kind = renderKind(feature)
            val path = if (kind == CampusFeatureKind.Building) {
                rings.toRoundedEvenOddPath(radius = BUILDING_CORNER_RADIUS_METERS)
            } else {
                rings.toEvenOddPath()
            }
            val bounds = rings.ringsBounds()
            val athleticsTrack = kind == CampusFeatureKind.Sport && isAthleticsTrack(feature.properties.name)
            val buildingPalette = palette.forBuildingStyle(buildingStyle(feature))
            val baseColor = when (kind) {
                CampusFeatureKind.Water -> palette.water
                CampusFeatureKind.Green -> palette.green
                CampusFeatureKind.Sport -> if (athleticsTrack) {
                    palette.trackSurface
                } else {
                    palette.sportSurface
                }
                CampusFeatureKind.Boundary -> palette.campusPaper
                CampusFeatureKind.Building -> buildingPalette.roof
            }
            val fillBrush = when (kind) {
                CampusFeatureKind.Boundary -> Brush.linearGradient(
                    colors = listOf(palette.campusPaperLight, palette.campusPaper),
                    start = bounds.topLeft,
                    end = bounds.bottomRight,
                )
                CampusFeatureKind.Water -> Brush.linearGradient(
                    colors = palette.waterGradientColors(1f),
                    start = bounds.topLeft,
                    end = bounds.bottomRight,
                )
                CampusFeatureKind.Green -> SolidColor(palette.green)
                CampusFeatureKind.Sport -> SolidColor(
                    if (athleticsTrack) palette.trackSurface else palette.sportSurface,
                )
                CampusFeatureKind.Building -> Brush.linearGradient(
                    colors = listOf(
                        buildingPalette.roofLight,
                        buildingPalette.roof,
                        buildingPalette.roofShade,
                    ),
                    start = Offset(
                        x = bounds.left + bounds.width * 0.12f,
                        y = bounds.top,
                    ),
                    end = bounds.bottomRight,
                )
            }
            add(
                ProjectedCampusFeature(
                    id = feature.stableId,
                    name = feature.properties.name,
                    category = feature.properties.category,
                    kind = kind,
                    rings = rings,
                    path = path,
                    bounds = bounds,
                    center = polygonCenter(rings.first()),
                    fillBrush = fillBrush,
                    baseColor = baseColor,
                    sideColor = buildingPalette.side,
                    outlineColor = when (kind) {
                        CampusFeatureKind.Water -> palette.waterOutline
                        CampusFeatureKind.Green -> palette.greenOutline
                        CampusFeatureKind.Sport -> if (athleticsTrack) {
                            palette.trackOutline
                        } else {
                            palette.sportOutline
                        }
                        CampusFeatureKind.Boundary -> palette.campusBorder
                        CampusFeatureKind.Building -> buildingPalette.outline
                    },
                    athleticsTrackGeometry = if (athleticsTrack) {
                        buildAthleticsTrackGeometry(rings.first())
                    } else {
                        null
                    },
                ),
            )
        }
    }
    if (projectedFeatures.isEmpty()) return emptyCampusCanvasScene()

    val boundaryFeatures = projectedFeatures.filterTo(mutableListOf()) {
        it.kind == CampusFeatureKind.Boundary
    }
    val mapFeatures = projectedFeatures.filterTo(mutableListOf()) {
        it.kind != CampusFeatureKind.Boundary
    }
    val drawFeatures = mapFeatures.sortedWith(
        compareBy<ProjectedCampusFeature> { feature ->
            when (feature.kind) {
                CampusFeatureKind.Green -> 0
                CampusFeatureKind.Water -> 1
                CampusFeatureKind.Sport -> 2
                CampusFeatureKind.Building -> 3
                CampusFeatureKind.Boundary -> 4
            }
        }.thenBy { feature -> if (feature.name.isBlank()) 0 else 1 },
    )
    val projectedRoads = buildList {
        for (road in roads) {
            val points = buildList {
                for (coordinate in road.geometry.coordinates) {
                    val longitude = coordinate.getOrNull(0) ?: continue
                    val latitude = coordinate.getOrNull(1) ?: continue
                    add(projection.project(latitude, longitude))
                }
            }
            if (points.size < 2) continue
            add(
                ProjectedCampusRoad(
                    id = road.properties.id,
                    kind = CampusRoadKind.fromRawValue(road.properties.kind),
                    width = road.properties.width.coerceIn(1f, 12f),
                    path = points.toOpenPath(),
                    bounds = points.bounds(),
                ),
            )
        }
    }

    val boundsSource = if (boundaryFeatures.isNotEmpty()) boundaryFeatures else drawFeatures
    var bounds = boundsSource.featureBounds().ensureMinimumSize()
    if (boundaryFeatures.isEmpty()) {
        for (road in projectedRoads) bounds = bounds.union(road.bounds)
    }
    var cameraBounds = drawFeatures.featureBounds().ensureMinimumSize()
    for (road in projectedRoads) cameraBounds = cameraBounds.union(road.bounds)
    cameraBounds = cameraBounds.inflate(min(cameraBounds.width, cameraBounds.height) * 0.025f)
    val random = Random(drawFeatures.fold(17) { acc, feature -> 31 * acc + feature.id.hashCode() })
    val grain = buildList {
        var attempts = 0
        while (size < 150 && attempts < 2_000) {
            val candidate = Offset(
            x = bounds.left + random.nextFloat() * bounds.width,
            y = bounds.top + random.nextFloat() * bounds.height,
            )
            if (boundaryFeatures.isEmpty() || boundaryFeatures.any { it.contains(candidate) }) {
                add(candidate)
            }
            attempts++
        }
    }
    val labelFeatures = drawFeatures.filterTo(mutableListOf()) { it.name.isNotBlank() }
    labelFeatures.sortWith(
        compareByDescending<ProjectedCampusFeature>(::labelPriority)
            .thenByDescending { it.bounds.width * it.bounds.height },
    )
    return CampusCanvasScene(
        key = (31 * projectedFeatures.hashCode() + projectedRoads.hashCode()).toString(),
        boundaries = boundaryFeatures,
        features = drawFeatures,
        labelFeatures = labelFeatures,
        roads = projectedRoads,
        projection = projection,
        bounds = bounds,
        cameraBounds = cameraBounds,
        paperGrain = grain,
    )
}

private fun renderKind(feature: CampusMapFeature): CampusFeatureKind {
    CampusFeatureKind.fromRawValue(feature.properties.kind)?.let { return it }
    val name = feature.properties.name
    return when {
        feature.properties.category == "体育" &&
            listOf("场", "田径", "球场").any(name::contains) -> CampusFeatureKind.Sport
        listOf("湖", "水塘", "水域").any(name::contains) -> CampusFeatureKind.Water
        listOf("草坪", "花园", "公园").any(name::contains) -> CampusFeatureKind.Green
        else -> CampusFeatureKind.Building
    }
}

private fun buildingStyle(feature: CampusMapFeature): CampusBuildingStyle {
    val category = feature.properties.category
    val name = feature.properties.name
    return when {
        category == "教学楼" -> CampusBuildingStyle.Teaching
        category == "图书馆" -> CampusBuildingStyle.Library
        category == "食堂" -> CampusBuildingStyle.Canteen
        category == "行政办公" -> CampusBuildingStyle.Administration
        category == "生活休闲" -> CampusBuildingStyle.Leisure
        category.contains("宿舍") || name.contains("宿舍") || name.contains("轩") -> when {
            name.contains("至诚") -> CampusBuildingStyle.ZhichengDormitory
            name.contains("弘毅") -> CampusBuildingStyle.HongyiDormitory
            name.contains("行健") -> CampusBuildingStyle.XingjianDormitory
            name.contains("敏行") -> CampusBuildingStyle.MinxingDormitory
            category.contains("东苑") || name.contains("东苑") -> CampusBuildingStyle.EastDormitory
            category.contains("西苑") || name.contains("西苑") -> CampusBuildingStyle.WestDormitory
            category.contains("南苑") || name.contains("南苑") -> CampusBuildingStyle.SouthDormitory
            else -> CampusBuildingStyle.Dormitory
        }
        else -> CampusBuildingStyle.Default
    }
}

private fun emptyCampusCanvasScene(): CampusCanvasScene = CampusCanvasScene(
    key = "empty",
    boundaries = emptyList(),
    features = emptyList(),
    labelFeatures = emptyList(),
    roads = emptyList(),
    projection = CampusGeographicProjection(0.0, 0.0, METERS_PER_LATITUDE_DEGREE),
    bounds = Rect(0f, 0f, 1f, 1f),
    cameraBounds = Rect(0f, 0f, 1f, 1f),
    paperGrain = emptyList(),
)

private fun List<List<Offset>>.toEvenOddPath(): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    for (ring in this@toEvenOddPath) {
        if (ring.isEmpty()) continue
        moveTo(ring.first().x, ring.first().y)
        for (index in 1 until ring.size) {
            lineTo(ring[index].x, ring[index].y)
        }
        close()
    }
}

private fun List<List<Offset>>.toRoundedEvenOddPath(radius: Float): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    for (ring in this@toRoundedEvenOddPath) {
        val pointCount = if (ring.size > 1 && ring.first() == ring.last()) {
            ring.size - 1
        } else {
            ring.size
        }
        if (pointCount < 3) continue

        for (index in 0 until pointCount) {
            val previous = ring[(index - 1 + pointCount) % pointCount]
            val current = ring[index]
            val next = ring[(index + 1) % pointCount]
            val incomingX = previous.x - current.x
            val incomingY = previous.y - current.y
            val outgoingX = next.x - current.x
            val outgoingY = next.y - current.y
            val incomingLength = sqrt(incomingX * incomingX + incomingY * incomingY)
            val outgoingLength = sqrt(outgoingX * outgoingX + outgoingY * outgoingY)
            if (incomingLength == 0f || outgoingLength == 0f) continue

            val cornerDistance = min(radius, min(incomingLength, outgoingLength) * 0.22f)
            val cornerStart = Offset(
                x = current.x + incomingX / incomingLength * cornerDistance,
                y = current.y + incomingY / incomingLength * cornerDistance,
            )
            val cornerEnd = Offset(
                x = current.x + outgoingX / outgoingLength * cornerDistance,
                y = current.y + outgoingY / outgoingLength * cornerDistance,
            )
            if (index == 0) {
                moveTo(cornerStart.x, cornerStart.y)
            } else {
                lineTo(cornerStart.x, cornerStart.y)
            }
            quadraticTo(current.x, current.y, cornerEnd.x, cornerEnd.y)
        }
        close()
    }
}

private fun List<Offset>.toOpenPath(): Path = Path().apply {
    if (this@toOpenPath.isEmpty()) return@apply
    moveTo(first().x, first().y)
    for (index in 1 until this@toOpenPath.size) {
        lineTo(this@toOpenPath[index].x, this@toOpenPath[index].y)
    }
}

private fun List<Offset>.bounds(): Rect {
    if (isEmpty()) return Rect.Zero
    var left = first().x
    var top = first().y
    var right = first().x
    var bottom = first().y
    for (index in 1 until size) {
        val point = this[index]
        if (point.x < left) left = point.x
        if (point.x > right) right = point.x
        if (point.y < top) top = point.y
        if (point.y > bottom) bottom = point.y
    }
    return Rect(left, top, right, bottom)
}

private fun List<List<Offset>>.ringsBounds(): Rect {
    if (isEmpty()) return Rect.Zero
    var combinedBounds = first().bounds()
    for (index in 1 until size) {
        combinedBounds = combinedBounds.union(this[index].bounds())
    }
    return combinedBounds
}

private fun List<ProjectedCampusFeature>.featureBounds(): Rect {
    if (isEmpty()) return Rect.Zero
    var combinedBounds = first().bounds
    for (index in 1 until size) {
        combinedBounds = combinedBounds.union(this[index].bounds)
    }
    return combinedBounds
}

private fun Rect.ensureMinimumSize(): Rect {
    val minimumSize = 20f
    val extraWidth = (minimumSize - width).coerceAtLeast(0f)
    val extraHeight = (minimumSize - height).coerceAtLeast(0f)
    return Rect(
        left = left - extraWidth / 2f,
        top = top - extraHeight / 2f,
        right = right + extraWidth / 2f,
        bottom = bottom + extraHeight / 2f,
    )
}

private fun Rect.inflate(amount: Float): Rect = Rect(
    left = left - amount,
    top = top - amount,
    right = right + amount,
    bottom = bottom + amount,
)

private fun Rect.union(other: Rect): Rect = Rect(
    left = min(left, other.left),
    top = min(top, other.top),
    right = max(right, other.right),
    bottom = max(bottom, other.bottom),
)

private fun polygonCenter(points: List<Offset>): Offset {
    if (points.isEmpty()) return Offset.Zero
    var x = 0f
    var y = 0f
    for (point in points) {
        x += point.x
        y += point.y
    }
    return Offset(x / points.size, y / points.size)
}

private fun buildAthleticsTrackGeometry(points: List<Offset>): AthleticsTrackGeometry? {
    val pointCount = if (points.size > 1 && points.first() == points.last()) {
        points.size - 1
    } else {
        points.size
    }
    if (pointCount < 3) return null

    var meanX = 0f
    var meanY = 0f
    for (index in 0 until pointCount) {
        meanX += points[index].x
        meanY += points[index].y
    }
    val mean = Offset(meanX / pointCount, meanY / pointCount)

    var covarianceXx = 0f
    var covarianceYy = 0f
    var covarianceXy = 0f
    for (index in 0 until pointCount) {
        val dx = points[index].x - mean.x
        val dy = points[index].y - mean.y
        covarianceXx += dx * dx
        covarianceYy += dy * dy
        covarianceXy += dx * dy
    }

    var angleRadians = 0.5f * atan2(
        y = 2f * covarianceXy,
        x = covarianceXx - covarianceYy,
    )
    val axisX = cos(angleRadians.toDouble()).toFloat()
    val axisY = sin(angleRadians.toDouble()).toFloat()
    val normalX = -axisY
    val normalY = axisX

    var minimumAlong = Float.POSITIVE_INFINITY
    var maximumAlong = Float.NEGATIVE_INFINITY
    var minimumAcross = Float.POSITIVE_INFINITY
    var maximumAcross = Float.NEGATIVE_INFINITY
    for (index in 0 until pointCount) {
        val dx = points[index].x - mean.x
        val dy = points[index].y - mean.y
        val along = dx * axisX + dy * axisY
        val across = dx * normalX + dy * normalY
        minimumAlong = min(minimumAlong, along)
        maximumAlong = max(maximumAlong, along)
        minimumAcross = min(minimumAcross, across)
        maximumAcross = max(maximumAcross, across)
    }

    val middleAlong = (minimumAlong + maximumAlong) / 2f
    val middleAcross = (minimumAcross + maximumAcross) / 2f
    val orientedCenter = mean + Offset(
        x = axisX * middleAlong + normalX * middleAcross,
        y = axisY * middleAlong + normalY * middleAcross,
    )
    var length = maximumAlong - minimumAlong
    var width = maximumAcross - minimumAcross
    if (width > length) {
        val previousLength = length
        length = width
        width = previousLength
        angleRadians += (Math.PI / 2.0).toFloat()
    }

    return AthleticsTrackGeometry(
        center = orientedCenter,
        angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat(),
        length = length,
        width = width,
    )
}

private fun pointInRing(point: Offset, ring: List<Offset>): Boolean {
    if (ring.size < 3) return false
    var inside = false
    var previousIndex = ring.lastIndex
    for (currentIndex in ring.indices) {
        val current = ring[currentIndex]
        val previous = ring[previousIndex]
        val crossesRay = (current.y > point.y) != (previous.y > point.y)
        if (crossesRay) {
            val intersectionX = (previous.x - current.x) *
                (point.y - current.y) /
                (previous.y - current.y) +
                current.x
            if (point.x < intersectionX) inside = !inside
        }
        previousIndex = currentIndex
    }
    return inside
}

private fun Color.scaleRgb(factor: Float): Color = Color(
    red = (red * factor).coerceIn(0f, 1f),
    green = (green * factor).coerceIn(0f, 1f),
    blue = (blue * factor).coerceIn(0f, 1f),
    alpha = alpha,
)

private fun canvasBaseScale(viewport: Size, bounds: Rect): Float {
    if (viewport.width <= 0f || viewport.height <= 0f) return 1f
    return min(
        (viewport.width * 0.96f) / bounds.width,
        (viewport.height * 0.94f) / bounds.height,
    ).coerceAtLeast(0.0001f)
}

private fun clampCampusPan(
    proposedPan: Offset,
    zoom: Float,
    viewport: Size,
    worldBounds: Rect,
): Offset {
    val baseScale = canvasBaseScale(viewport, worldBounds)
    val scaledMapWidth = worldBounds.width * baseScale * zoom
    val scaledMapHeight = worldBounds.height * baseScale * zoom

    fun maximumPan(mapSize: Float, viewportSize: Float): Float {
        if (zoom <= 1f) return 24f
        val overflow = ((mapSize - viewportSize) / 2f).coerceAtLeast(0f)
        return overflow + viewportSize * 0.42f
    }

    val maximumPanX = maximumPan(scaledMapWidth, viewport.width)
    val maximumPanY = maximumPan(scaledMapHeight, viewport.height)
    return Offset(
        x = proposedPan.x.coerceIn(-maximumPanX, maximumPanX),
        y = proposedPan.y.coerceIn(-maximumPanY, maximumPanY),
    )
}

private data class BuildingPalette(
    val roofLight: Color,
    val roof: Color,
    val roofShade: Color,
    val side: Color,
    val outline: Color,
)

private class CampusMapPalette(private val isDarkTheme: Boolean) {
    val backgroundTopLeft = if (isDarkTheme) Color(0xFF0F171D) else Color.White
    val backgroundTopRight = if (isDarkTheme) Color(0xFF111C23) else Color(0xFFF8FCFE)
    val backgroundBottomLeft = if (isDarkTheme) Color(0xFF101A20) else Color(0xFFFAFDFD)
    val backgroundBottomRight = if (isDarkTheme) Color(0xFF142128) else Color(0xFFF3F9FC)
    val campusPaperLight = if (isDarkTheme) Color(0xFF1B2B34) else Color(0xFFFBFDFE)
    val campusPaper = if (isDarkTheme) Color(0xFF17252D) else Color(0xFFF2F7FA)
    val campusBorder = if (isDarkTheme) Color(0xFF2E4653) else Color(0xFFD0E0E8)
    val campusShadow = if (isDarkTheme) Color(0x66000000) else Color(0x183B7898)
    val grainLight = if (isDarkTheme) Color(0x18FFFFFF) else Color(0x44FFFFFF)
    val grainDark = if (isDarkTheme) Color(0x24000000) else Color(0x120F2919)
    val buildingShadow = if (isDarkTheme) Color(0xFF02080C) else Color(0xFF4B8CAD)
    val buildingRoofHighlight = if (isDarkTheme) Color(0x28D8EDF7) else Color(0x4DFFFFFF)
    val search = Color(0xFFE49A22)
    val searchGlow = Color(0xFFFFD45A)
    val selection = Color(0xFF2B91C4)
    val selectionGlow = Color(0xFF9DDFFF)
    val labelShadow = if (isDarkTheme) Color(0x40000000) else Color(0x160E2B3A)
    val labelHalo = if (isDarkTheme) Color(0xD914242C) else Color(0xEEFFFFFF)
    val selectedLabel = if (isDarkTheme) Color(0xFF355665) else Color(0xFFDDF4FF)
    val searchLabel = if (isDarkTheme) Color(0xFF574426) else Color(0xFFFFE3A0)
    val labelInk = if (isDarkTheme) Color(0xFFE3EAED) else Color(0xFF44515B)
    val mainRoad = if (isDarkTheme) Color(0xFF314650) else Color.White
    val serviceRoad = if (isDarkTheme) Color(0xFF2B3D47) else Color(0xFFF9FCFF)
    val footway = if (isDarkTheme) Color(0xFF263842) else Color(0xFFE8F0F4)
    val roadCasing = if (isDarkTheme) Color(0xFF203039) else Color(0xFFD8E5EA)
    val roadHighlight = if (isDarkTheme) Color(0x24FFFFFF) else Color(0xCCFFFFFF)
    val water = if (isDarkTheme) Color(0xFF477D7B) else Color(0xFF9BCBC9)
    val green = if (isDarkTheme) Color(0xFF385744) else Color(0xFFCFE5CB)
    val waterOutline = if (isDarkTheme) Color(0xFF6AA4A2) else Color(0xFF72AAA8)
    val greenOutline = if (isDarkTheme) Color(0xFF4A6655) else Color(0xFFC4DDBF)
    val sportSurface = if (isDarkTheme) Color(0xFF416B7B) else Color(0xFF9BBFC9)
    val sportOutline = if (isDarkTheme) Color(0xFF6F9BAC) else Color(0xFF7197A2)
    val trackSurface = if (isDarkTheme) Color(0xFF875349) else Color(0xFFC88977)
    val trackOutline = if (isDarkTheme) Color(0xFFB47A6D) else Color(0xFFA86D5E)
    val fieldSurface = if (isDarkTheme) Color(0xFF466B43) else Color(0xFFB7D49B)
    private val waterGradient = if (isDarkTheme) {
        listOf(Color(0xFF5C9290), Color(0xFF3E706E))
    } else {
        listOf(Color(0xFFC4E1DE), Color(0xFF9BCBC9))
    }

    fun waterGradientColors(alpha: Float): List<Color> =
        gradientColors(waterGradient, alpha)

    private fun gradientColors(colors: List<Color>, alpha: Float): List<Color> =
        if (alpha == 1f) colors else colors.map { it.copy(alpha = alpha) }

    private fun buildingPalette(
        roofLight: Color,
        roof: Color,
        side: Color,
        outline: Color,
    ): BuildingPalette = if (isDarkTheme) {
        val darkRoof = roof.scaleRgb(0.48f)
        BuildingPalette(
            roofLight = roofLight.scaleRgb(0.55f),
            roof = darkRoof,
            roofShade = darkRoof.scaleRgb(0.84f),
            side = side.scaleRgb(0.42f),
            outline = outline.scaleRgb(0.78f),
        )
    } else {
        BuildingPalette(
            roofLight = roofLight,
            roof = roof,
            roofShade = roof.scaleRgb(0.91f),
            side = side,
            outline = outline,
        )
    }

    private val teachingBuilding = buildingPalette(
        roofLight = Color(0xFFF8E8D6),
        roof = Color(0xFFF0D2B4),
        side = Color(0xFFD9B998),
        outline = Color(0xFFB79577),
    )
    private val libraryBuilding = buildingPalette(
        roofLight = Color(0xFFE7F0F7),
        roof = Color(0xFFC7DBEA),
        side = Color(0xFFA6C2D6),
        outline = Color(0xFF7899B0),
    )
    private val eastDormitory = buildingPalette(
        roofLight = Color(0xFFEDF5EA),
        roof = Color(0xFFD6E8D1),
        side = Color(0xFFB4CEB0),
        outline = Color(0xFF86A184),
    )
    private val westDormitory = buildingPalette(
        roofLight = Color(0xFFF7E7E5),
        roof = Color(0xFFEBCBC8),
        side = Color(0xFFCDA8A4),
        outline = Color(0xFFA77E7A),
    )
    private val southDormitory = buildingPalette(
        roofLight = Color(0xFFF1EBF3),
        roof = Color(0xFFDDD3E4),
        side = Color(0xFFBEAFC7),
        outline = Color(0xFF92839D),
    )
    private val minxingDormitory = buildingPalette(
        roofLight = Color(0xFFDCEAF7),
        roof = Color(0xFFB8D6EB),
        side = Color(0xFF8DAFC9),
        outline = Color(0xFF6387A3),
    )
    private val dormitory = buildingPalette(
        roofLight = Color(0xFFF6E1E8),
        roof = Color(0xFFEBC6D4),
        side = Color(0xFFC89AAA),
        outline = Color(0xFF996F7E),
    )
    private val canteen = buildingPalette(
        roofLight = Color(0xFFFAF0D8),
        roof = Color(0xFFF3E2BD),
        side = Color(0xFFD6BE8F),
        outline = Color(0xFFA98D5D),
    )
    private val administration = buildingPalette(
        roofLight = Color(0xFFDEE5EE),
        roof = Color(0xFFC7D2E1),
        side = Color(0xFF99AABE),
        outline = Color(0xFF6D7F95),
    )
    private val leisure = buildingPalette(
        roofLight = Color(0xFFDCF1ED),
        roof = Color(0xFFC1E4DD),
        side = Color(0xFF91BDB4),
        outline = Color(0xFF668E86),
    )
    private val defaultBuilding = buildingPalette(
        roofLight = Color(0xFFE2E9EC),
        roof = Color(0xFFCFDCE2),
        side = Color(0xFFA3B6C0),
        outline = Color(0xFF748995),
    )

    fun forBuildingStyle(style: CampusBuildingStyle): BuildingPalette = when (style) {
        CampusBuildingStyle.Teaching -> teachingBuilding
        CampusBuildingStyle.Library -> libraryBuilding
        CampusBuildingStyle.EastDormitory -> eastDormitory
        CampusBuildingStyle.WestDormitory -> westDormitory
        CampusBuildingStyle.SouthDormitory -> southDormitory
        CampusBuildingStyle.ZhichengDormitory -> eastDormitory
        CampusBuildingStyle.HongyiDormitory -> westDormitory
        CampusBuildingStyle.XingjianDormitory -> southDormitory
        CampusBuildingStyle.MinxingDormitory -> minxingDormitory
        CampusBuildingStyle.Dormitory -> dormitory
        CampusBuildingStyle.Canteen -> canteen
        CampusBuildingStyle.Administration -> administration
        CampusBuildingStyle.Leisure -> leisure
        CampusBuildingStyle.Default -> defaultBuilding
    }
}

private const val METERS_PER_LATITUDE_DEGREE = 111_320.0
private const val BUILDING_CORNER_RADIUS_METERS = 1.6f
internal const val CAMPUS_MAP_DEFAULT_ZOOM = 1.18f
private const val MIN_ZOOM = 0.72f
private const val MAX_ZOOM = 7f
private const val LABEL_SCALE_COMPENSATION_STRENGTH = 0.82f
private val LABEL_ZOOM_LEVELS = floatArrayOf(
    MIN_ZOOM,
    1f,
    1.4f,
    1.9f,
    2.7f,
    3.8f,
    5.2f,
    MAX_ZOOM,
)
