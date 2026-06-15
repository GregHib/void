package world.gregs.voidps.tools.photobooth.render

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Software rasterizer for [RenderModel]s. Ported from Quill's `SoftwareModelRenderer.renderModelPreview`
 * (z-buffered, flat HSL shading, painter-ordered translucency) with two deliberate changes for avatars:
 *  - textures are never sampled (flat shaded faces only);
 *  - the inventory-sprite black outline + drop shadow post-process is dropped (it looks like a sticker
 *    border on a portrait). The output is finalized to a clean transparent cut-out, then cropped to a
 *    head-and-shoulders bust and scaled to the requested size.
 */
object AvatarRenderer {

    private const val INTERNAL_RENDER_SCALE = 3
    private const val NEAR_PLANE = 50.0

    private val LIGHT_X: Double
    private val LIGHT_Y: Double
    private val LIGHT_Z: Double

    init {
        val len = sqrt(0.45 * 0.45 + 0.7 * 0.7 + 0.55 * 0.55)
        LIGHT_X = -0.45 / len
        LIGHT_Y = 0.7 / len
        LIGHT_Z = -0.55 / len
    }

    /**
     * Renders [model] to a square [size]x[size] transparent ARGB image, tightly cropped to the
     * drawn pixels and centred. Works for both the full-body and chathead models.
     */
    fun render(
        model: RenderModel,
        size: Int,
        pitchDegrees: Double = 10.0,
        yawDegrees: Double = 0.0,
        zoom: Double = 1.3,
    ): BufferedImage {
        val raw = renderFaces(model, size * INTERNAL_RENDER_SCALE, pitchDegrees, yawDegrees, zoom)
        finalizeAlpha(raw)
        val cropped = cropTight(raw)
        return scaleTo(cropped, size)
    }

    /**
     * Fallback chathead for cases with no dedicated chathead mesh (e.g. a full helm with no dialogue
     * model): renders the full body and crops the top [topFraction] of the figure to isolate the head.
     */
    fun renderHeadCrop(
        model: RenderModel,
        size: Int,
        pitchDegrees: Double = 10.0,
        yawDegrees: Double = 0.0,
        zoom: Double = 1.3,
        topFraction: Double = 0.26,
    ): BufferedImage {
        val raw = renderFaces(model, size * INTERNAL_RENDER_SCALE, pitchDegrees, yawDegrees, zoom)
        finalizeAlpha(raw)
        val head = cropTop(raw, topFraction)
        return scaleTo(head, size)
    }

    private fun renderFaces(model: RenderModel, dimension: Int, pitchDegrees: Double, yawDegrees: Double, zoom: Double): BufferedImage {
        val raw = max(1, dimension)
        val centerModelX = (model.minX + model.maxX) * 0.5
        val centerModelY = (model.minY + model.maxY) * 0.5
        val centerModelZ = (model.minZ + model.maxZ) * 0.5
        val maxDimension = max(1.0, max(model.width, max(model.height, model.depth)).toDouble())
        val fitScale = raw * 0.7 / maxDimension
        val distance = raw * (1.8 / max(0.2, zoom))
        val centerX = raw * 0.5
        val centerY = raw * 0.55
        val focalLength = raw * 1.1
        val pitch = Math.toRadians(pitchDegrees)
        val yaw = Math.toRadians(yawDegrees)

        val count = model.vertexCount
        val viewX = DoubleArray(count)
        val viewY = DoubleArray(count)
        val viewZ = DoubleArray(count)
        val screenX = IntArray(count)
        val screenY = IntArray(count)
        for (i in 0 until count) {
            var px = (model.verticesX[i] - centerModelX) * fitScale
            var py = (model.verticesY[i] - centerModelY) * fitScale
            var pz = (model.verticesZ[i] - centerModelZ) * fitScale
            // yaw (around Y) then pitch (around X)
            val sinYaw = sin(yaw); val cosYaw = cos(yaw)
            val ny = px * cosYaw + pz * sinYaw
            val nz = -px * sinYaw + pz * cosYaw
            px = ny
            pz = nz
            val sinPitch = sin(pitch); val cosPitch = cos(pitch)
            val pyy = py * cosPitch - pz * sinPitch
            val pzz = py * sinPitch + pz * cosPitch
            py = pyy
            pz = pzz + distance
            viewX[i] = px; viewY[i] = py; viewZ[i] = pz
            var depth = pz
            if (Math.abs(depth) < NEAR_PLANE) depth = if (depth < 0.0) -NEAR_PLANE else NEAR_PLANE
            screenX[i] = round(centerX + px * focalLength / depth).toInt()
            screenY[i] = round(centerY + py * focalLength / depth).toInt()
        }

        // Smooth (Gouraud) shading: accumulate area-weighted face normals per vertex, then light each
        // vertex. Brightness is interpolated across each triangle, removing the flat-faceted look.
        val vnx = DoubleArray(count); val vny = DoubleArray(count); val vnz = DoubleArray(count)
        for (face in 0 until model.faceCount) {
            val rt = model.faceRenderTypes?.getOrNull(face) ?: 0
            if (rt >= 2) continue
            val a = model.faceA[face]; val b = model.faceB[face]; val c = model.faceC[face]
            val fnx = (viewY[b] - viewY[a]) * (viewZ[c] - viewZ[a]) - (viewZ[b] - viewZ[a]) * (viewY[c] - viewY[a])
            val fny = (viewZ[b] - viewZ[a]) * (viewX[c] - viewX[a]) - (viewX[b] - viewX[a]) * (viewZ[c] - viewZ[a])
            val fnz = (viewX[b] - viewX[a]) * (viewY[c] - viewY[a]) - (viewY[b] - viewY[a]) * (viewX[c] - viewX[a])
            vnx[a] += fnx; vny[a] += fny; vnz[a] += fnz
            vnx[b] += fnx; vny[b] += fny; vnz[b] += fnz
            vnx[c] += fnx; vny[c] += fny; vnz[c] += fnz
        }
        val vertexBrightness = DoubleArray(count)
        for (i in 0 until count) {
            val len = sqrt(vnx[i] * vnx[i] + vny[i] * vny[i] + vnz[i] * vnz[i])
            vertexBrightness[i] = if (len == 0.0) 0.55
            else clamp(0.55 + (vnx[i] / len) * LIGHT_X + (vny[i] / len) * LIGHT_Y + (vnz[i] / len) * LIGHT_Z, 0.28, 1.0)
        }

        val image = BufferedImage(raw, raw, BufferedImage.TYPE_INT_ARGB)
        val depthBuffer = DoubleArray(raw * raw) { Double.POSITIVE_INFINITY }
        val translucent = ArrayList<PendingFace>()
        for (face in 0 until model.faceCount) {
            val rt = model.faceRenderTypes?.getOrNull(face) ?: 0
            if (rt >= 2) continue
            val a = model.faceA[face]; val b = model.faceB[face]; val c = model.faceC[face]
            if (viewZ[a] <= NEAR_PLANE || viewZ[b] <= NEAR_PLANE || viewZ[c] <= NEAR_PLANE) continue
            val cross = (screenX[b] - screenX[a]).toDouble() * (screenY[c] - screenY[a]) -
                (screenY[b] - screenY[a]).toDouble() * (screenX[c] - screenX[a])
            if (cross >= 0.0) continue
            val alpha = faceAlpha(model.faceAlphas, face)
            if (alpha == 0) continue
            val packed = if (face < model.faceColors.size) model.faceColors[face].toInt() and 0xFFFF else 0
            val rgb = CacheColor.toRgb(packed)
            // Flat faces (renderType 1) take a single face-normal brightness; the rest are smooth.
            val c1: Int; val c2: Int; val c3: Int
            if (rt == 1) {
                var nx = (viewY[b] - viewY[a]) * (viewZ[c] - viewZ[a]) - (viewZ[b] - viewZ[a]) * (viewY[c] - viewY[a])
                var nyv = (viewZ[b] - viewZ[a]) * (viewX[c] - viewX[a]) - (viewX[b] - viewX[a]) * (viewZ[c] - viewZ[a])
                var nzv = (viewX[b] - viewX[a]) * (viewY[c] - viewY[a]) - (viewY[b] - viewY[a]) * (viewX[c] - viewX[a])
                val nl = sqrt(nx * nx + nyv * nyv + nzv * nzv)
                val fb = if (nl == 0.0) 0.55 else clamp(0.55 + (nx / nl) * LIGHT_X + (nyv / nl) * LIGHT_Y + (nzv / nl) * LIGHT_Z, 0.28, 1.0)
                val flat = shade(rgb, alpha, fb)
                c1 = flat; c2 = flat; c3 = flat
            } else {
                c1 = shade(rgb, alpha, vertexBrightness[a])
                c2 = shade(rgb, alpha, vertexBrightness[b])
                c3 = shade(rgb, alpha, vertexBrightness[c])
            }
            if (alpha < 255) {
                translucent.add(PendingFace(screenX[a], screenY[a], viewZ[a], c1, screenX[b], screenY[b], viewZ[b], c2,
                    screenX[c], screenY[c], viewZ[c], c3))
                continue
            }
            rasterize(image, depthBuffer, screenX[a], screenY[a], viewZ[a], c1, screenX[b], screenY[b], viewZ[b], c2,
                screenX[c], screenY[c], viewZ[c], c3, true)
        }
        translucent.sortByDescending { it.averageDepth() }
        for (f in translucent) {
            rasterize(image, depthBuffer, f.x1, f.y1, f.z1, f.c1, f.x2, f.y2, f.z2, f.c2, f.x3, f.y3, f.z3, f.c3, false)
        }
        return image
    }

    /** Gouraud triangle: per-pixel barycentric interpolation of the three corner colours. */
    private fun rasterize(
        image: BufferedImage, depthBuffer: DoubleArray,
        x1: Int, y1: Int, z1: Double, c1: Int, x2: Int, y2: Int, z2: Double, c2: Int, x3: Int, y3: Int, z3: Double, c3: Int,
        writeDepth: Boolean,
    ) {
        val w = image.width; val h = image.height
        val minX = max(0, min(x1, min(x2, x3)))
        val maxX = min(w - 1, max(x1, max(x2, x3)))
        val minY = max(0, min(y1, min(y2, y3)))
        val maxY = min(h - 1, max(y1, max(y2, y3)))
        if (minX > maxX || minY > maxY) return
        val area = edge(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), x3.toDouble(), y3.toDouble())
        if (area == 0.0) return
        val a1 = (c1 ushr 24) and 0xFF; val r1 = (c1 shr 16) and 0xFF; val g1 = (c1 shr 8) and 0xFF; val b1c = c1 and 0xFF
        val a2 = (c2 ushr 24) and 0xFF; val r2 = (c2 shr 16) and 0xFF; val g2 = (c2 shr 8) and 0xFF; val b2c = c2 and 0xFF
        val a3 = (c3 ushr 24) and 0xFF; val r3 = (c3 shr 16) and 0xFF; val g3 = (c3 shr 8) and 0xFF; val b3c = c3 and 0xFF
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val sx = x + 0.5; val sy = y + 0.5
                val w1 = edge(x2.toDouble(), y2.toDouble(), x3.toDouble(), y3.toDouble(), sx, sy) / area
                val w2 = edge(x3.toDouble(), y3.toDouble(), x1.toDouble(), y1.toDouble(), sx, sy) / area
                val w3 = edge(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble(), sx, sy) / area
                if (w1 < 0.0 || w2 < 0.0 || w3 < 0.0) continue
                val depth = z1 * w1 + z2 * w2 + z3 * w3
                val index = y * w + x
                if (depth >= depthBuffer[index]) continue
                val a = (a1 * w1 + a2 * w2 + a3 * w3).toInt()
                val r = (r1 * w1 + r2 * w2 + r3 * w3).toInt()
                val g = (g1 * w1 + g2 * w2 + g3 * w3).toInt()
                val bch = (b1c * w1 + b2c * w2 + b3c * w3).toInt()
                val argb = (a shl 24) or (r shl 16) or (g shl 8) or bch
                if (writeDepth) {
                    depthBuffer[index] = depth
                    image.setRGB(x, y, argb)
                } else {
                    image.setRGB(x, y, blend(argb, image.getRGB(x, y)))
                }
            }
        }
    }

    private fun edge(x1: Double, y1: Double, x2: Double, y2: Double, px: Double, py: Double): Double =
        (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1)

    private fun shade(rgb: Int, alpha: Int, brightness: Double): Int {
        val red = applyBrightness((rgb shr 16) and 0xFF, brightness)
        val green = applyBrightness((rgb shr 8) and 0xFF, brightness)
        val blue = applyBrightness(rgb and 0xFF, brightness)
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    private fun faceAlpha(faceAlphas: IntArray?, index: Int): Int =
        if (faceAlphas != null && index < faceAlphas.size) 255 - (faceAlphas[index] and 0xFF) else 255

    private fun applyBrightness(channel: Int, brightness: Double): Int = clamp(24.0 + channel * brightness, 0.0, 255.0).toInt()

    private fun blend(source: Int, destination: Int): Int {
        val sa = (source ushr 24) and 0xFF
        if (sa <= 0) return destination
        if (sa >= 255) return source
        val da = (destination ushr 24) and 0xFF
        val outA = sa + da * (255 - sa) / 255
        if (outA == 0) return 0
        val sr = (source shr 16) and 0xFF; val sg = (source shr 8) and 0xFF; val sb = source and 0xFF
        val dr = (destination shr 16) and 0xFF; val dg = (destination shr 8) and 0xFF; val db = destination and 0xFF
        val r = (sr * sa + dr * da * (255 - sa) / 255) / outA
        val g = (sg * sa + dg * da * (255 - sa) / 255) / outA
        val b = (sb * sa + db * da * (255 - sa) / 255) / outA
        return (outA shl 24) or (r shl 16) or (g shl 8) or b
    }

    /** Forces every rendered pixel fully opaque and clears the rest, producing a clean cut-out. */
    private fun finalizeAlpha(image: BufferedImage) {
        val w = image.width; val h = image.height
        val pixels = image.getRGB(0, 0, w, h, null, 0, w)
        for (i in pixels.indices) {
            pixels[i] = if ((pixels[i] and 0x00FFFFFF) != 0) pixels[i] or 0xFF000000.toInt() else 0
        }
        image.setRGB(0, 0, w, h, pixels, 0, w)
    }

    /** Crops to the populated alpha bounding box and centres it in a square canvas. */
    private fun cropTight(image: BufferedImage): BufferedImage {
        val w = image.width; val h = image.height
        val pixels = image.getRGB(0, 0, w, h, null, 0, w)
        var minX = w; var minY = h; var maxX = -1; var maxY = -1
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                if ((pixels[row + x] ushr 24) != 0) {
                    if (x < minX) minX = x; if (x > maxX) maxX = x
                    if (y < minY) minY = y; if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return image // nothing drawn
        val cropW = maxX - minX + 1
        val cropH = maxY - minY + 1
        val side = max(cropW, cropH)
        val square = BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB)
        val offsetX = (side - cropW) / 2
        val offsetY = (side - cropH) / 2
        for (y in minY..maxY) {
            val srcRow = y * w
            for (x in minX..maxX) {
                val argb = pixels[srcRow + x]
                if ((argb ushr 24) != 0) {
                    square.setRGB(offsetX + (x - minX), offsetY + (y - minY), argb)
                }
            }
        }
        return square
    }

    /** Keeps the top [topFraction] of the populated height, then tight-crops that strip to a square. */
    private fun cropTop(image: BufferedImage, topFraction: Double): BufferedImage {
        val w = image.width; val h = image.height
        val pixels = image.getRGB(0, 0, w, h, null, 0, w)
        var minY = h; var maxY = -1
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                if ((pixels[row + x] ushr 24) != 0) { if (y < minY) minY = y; if (y > maxY) maxY = y; break }
            }
        }
        if (maxY < minY) return image
        val cutBottom = minY + max(1, ((maxY - minY + 1) * topFraction).toInt()) - 1
        // tight horizontal+vertical crop within the head strip
        var minX = w; var maxX = -1; var top = h; var bottom = -1
        for (y in minY..cutBottom) {
            val row = y * w
            for (x in 0 until w) {
                if ((pixels[row + x] ushr 24) != 0) {
                    if (x < minX) minX = x; if (x > maxX) maxX = x
                    if (y < top) top = y; if (y > bottom) bottom = y
                }
            }
        }
        if (maxX < minX || bottom < top) return image
        val cropW = maxX - minX + 1; val cropH = bottom - top + 1
        val side = max(cropW, cropH)
        val square = BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB)
        val offX = (side - cropW) / 2; val offY = (side - cropH) / 2
        for (y in top..bottom) {
            val srcRow = y * w
            for (x in minX..maxX) {
                val argb = pixels[srcRow + x]
                if ((argb ushr 24) != 0) square.setRGB(offX + (x - minX), offY + (y - top), argb)
            }
        }
        return square
    }

    private fun scaleTo(image: BufferedImage, size: Int): BufferedImage {
        if (image.width == size && image.height == size) return image
        val scaled = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = scaled.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.drawImage(image, 0, 0, size, size, null)
        g.dispose()
        return scaled
    }

    private fun clamp(value: Double, minValue: Double, maxValue: Double): Double = max(minValue, min(maxValue, value))

    private class PendingFace(
        val x1: Int, val y1: Int, val z1: Double, val c1: Int,
        val x2: Int, val y2: Int, val z2: Double, val c2: Int,
        val x3: Int, val y3: Int, val z3: Double, val c3: Int,
    ) {
        fun averageDepth(): Double = (z1 + z2 + z3) / 3.0
    }
}
