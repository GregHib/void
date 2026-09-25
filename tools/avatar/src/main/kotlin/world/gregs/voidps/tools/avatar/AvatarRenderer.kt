package world.gregs.voidps.tools.avatar

import world.gregs.voidps.tools.render.Mesh
import world.gregs.voidps.tools.render.TextureSource
import world.gregs.voidps.tools.render.Toolkit
import java.awt.Canvas
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Renders player [Mesh]es with the client's software toolkit, lit and posed the way interface
 * model components are drawn (Class348_Sub40_Sub7.method3064 + Class358.method3489).
 *
 * The image is rendered at [SUPERSAMPLE]x the requested size, cut out to a transparent background,
 * tightly cropped and scaled down.
 */
class AvatarRenderer(private val textureSource: TextureSource?) {

    private var toolkit: Toolkit? = null
    private var dimension = 0

    /**
     * @param ambient/contrast the client uses 64/850 for the full body and 64/768 for chatheads.
     * @param pitch/yaw in degrees; positive pitch looks down on the model.
     */
    fun render(mesh: Mesh, size: Int, ambient: Int, contrast: Int, pitch: Double = 0.0, yaw: Double = 0.0): BufferedImage {
        val raw = draw(mesh, size * SUPERSAMPLE, ambient, contrast, pitch, yaw)
        return scaleTo(cropTight(raw), size)
    }

    /** Fallback chathead for cases with no dedicated chathead mesh: renders the body and keeps the top [topFraction]. */
    fun renderHeadCrop(mesh: Mesh, size: Int, ambient: Int, contrast: Int, pitch: Double = 0.0, yaw: Double = 0.0, topFraction: Double = 0.26): BufferedImage {
        val raw = draw(mesh, size * SUPERSAMPLE, ambient, contrast, pitch, yaw)
        return scaleTo(cropTop(raw, topFraction), size)
    }

    private fun toolkit(dimension: Int): Toolkit {
        var toolkit = toolkit
        if (toolkit == null || this.dimension != dimension) {
            toolkit?.method3635()
            val canvas = Canvas()
            canvas.setSize(dimension, dimension)
            toolkit = Toolkit.method3692(dimension, dimension, textureSource, canvas)
            this.toolkit = toolkit
            this.dimension = dimension
        }
        return toolkit
    }

    private fun draw(mesh: Mesh, dimension: Int, ambient: Int, contrast: Int, pitchDegrees: Double, yawDegrees: Double): BufferedImage {
        val radius = centre(mesh)
        val toolkit = toolkit(dimension)
        val model = toolkit.createModel(mesh, FUNCTION_MASK, FEATURE_MASK, ambient, contrast)!!
        model.loadedTextures()

        // Keep the whole bounding sphere on screen with a mild perspective
        val distance = radius * 5
        val scale = (dimension.toLong() * distance / (radius * 2.8)).toInt()
        val pitch = angle(pitchDegrees)
        val yaw = angle(yawDegrees)

        toolkit.DA(dimension / 2, dimension / 2, scale, scale)
        val camera = toolkit.method3654()!!
        camera.makeIdentity()
        toolkit.setCamera(camera)
        // Class358.method3489 with the default brightness setting
        toolkit.xa(1.1523438f)
        toolkit.ZA(0xFFFFFF, 0.69921875f, 1.2f, -200.0f, -240.0f, -200.0f)
        val matrix = toolkit.method3705()!!
        matrix.makeRotationZ(0)
        matrix.makeAxisY(yaw)
        matrix.translate(0, distance * Mesh.anIntArray1207[pitch] shr 14, distance * Mesh.anIntArray1204[pitch] shr 14)
        matrix.rotateAxisX(pitch)
        val near = toolkit.i()
        val far = toolkit.XA()
        toolkit.f(50, Int.MAX_VALUE)
        toolkit.ya()
        toolkit.la()
        toolkit.aa(0, 0, dimension, dimension, 0, 0)
        model.render(matrix, 1)
        toolkit.f(near, far)
        val pixels = toolkit.na(0, 0, dimension, dimension)!!
        for (i in pixels.indices) {
            pixels[i] = if (pixels[i] and 0xffffff != 0) pixels[i] or -0x1000000 else 0
        }
        val image = BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(0, 0, dimension, dimension, pixels, 0, dimension)
        return image
    }

    /** Moves [mesh]'s bounding box centre to the origin so it rotates in place, returning its bounding radius. */
    private fun centre(mesh: Mesh): Int {
        val count = mesh.vertexCount
        val xs = mesh.vertexX!!
        val ys = mesh.vertexY!!
        val zs = mesh.vertexZ!!
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE
        for (i in 0 until count) {
            minX = minOf(minX, xs[i])
            maxX = maxOf(maxX, xs[i])
            minY = minOf(minY, ys[i])
            maxY = maxOf(maxY, ys[i])
            minZ = minOf(minZ, zs[i])
            maxZ = maxOf(maxZ, zs[i])
        }
        mesh.translate(-(minX + maxX) / 2, -(minY + maxY) / 2, -(minZ + maxZ) / 2)
        var radius = 1.0
        for (i in 0 until count) {
            radius = max(radius, sqrt(xs[i].toDouble() * xs[i] + ys[i].toDouble() * ys[i] + zs[i].toDouble() * zs[i]))
        }
        return radius.toInt() + 1
    }

    /** Degrees to the client's 14-bit angle. */
    private fun angle(degrees: Double): Int = Math.floorMod((degrees * 16384.0 / 360.0).toInt(), 16384)

    /** Crops to the populated alpha bounding box and centres it in a square canvas. */
    private fun cropTight(image: BufferedImage): BufferedImage {
        val w = image.width
        val h = image.height
        val pixels = image.getRGB(0, 0, w, h, null, 0, w)
        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (pixels[y * w + x] ushr 24 != 0) {
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                }
            }
        }
        if (maxX < minX || maxY < minY) return image
        return square(pixels, w, minX, minY, maxX, maxY)
    }

    /** Keeps the top [topFraction] of the populated height, then tight-crops that strip to a square. */
    private fun cropTop(image: BufferedImage, topFraction: Double): BufferedImage {
        val w = image.width
        val h = image.height
        val pixels = image.getRGB(0, 0, w, h, null, 0, w)
        var minY = h
        var maxY = -1
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (pixels[y * w + x] ushr 24 != 0) {
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                    break
                }
            }
        }
        if (maxY < minY) return image
        val cutBottom = minY + max(1, ((maxY - minY + 1) * topFraction).toInt()) - 1
        var minX = w
        var maxX = -1
        var top = h
        var bottom = -1
        for (y in minY..cutBottom) {
            for (x in 0 until w) {
                if (pixels[y * w + x] ushr 24 != 0) {
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                    top = minOf(top, y)
                    bottom = maxOf(bottom, y)
                }
            }
        }
        if (maxX < minX || bottom < top) return image
        return square(pixels, w, minX, top, maxX, bottom)
    }

    private fun square(pixels: IntArray, width: Int, minX: Int, minY: Int, maxX: Int, maxY: Int): BufferedImage {
        val cropW = maxX - minX + 1
        val cropH = maxY - minY + 1
        val side = max(cropW, cropH)
        val square = BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB)
        val offsetX = (side - cropW) / 2
        val offsetY = (side - cropH) / 2
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val argb = pixels[y * width + x]
                if (argb ushr 24 != 0) square.setRGB(offsetX + x - minX, offsetY + y - minY, argb)
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

    companion object {
        private const val SUPERSAMPLE = 3

        // Interface model components render players with function mask 2048
        private const val FUNCTION_MASK = 2048

        // Class69.method720 with high detail textures (no 0x40 low detail flag)
        private const val FEATURE_MASK = 0x1 or 0x2 or 0x4 or 0x10 or 0x20
    }
}
