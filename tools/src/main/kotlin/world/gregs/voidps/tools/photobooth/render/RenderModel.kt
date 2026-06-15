package world.gregs.voidps.tools.photobooth.render

/**
 * Flattened, render-ready geometry produced from a decoded [io.blurite.cache.model.Model].
 *
 * Texture fields from the original Quill `RenderModel` are intentionally omitted: the photo booth
 * renderer renders flat-shaded faces only (see [AvatarRenderer]), so per-face texture data is never
 * consulted.
 */
class RenderModel(
    val id: Int,
    val vertexCount: Int,
    val faceCount: Int,
    val verticesX: IntArray,
    val verticesY: IntArray,
    val verticesZ: IntArray,
    val faceA: IntArray,
    val faceB: IntArray,
    val faceC: IntArray,
    val faceColors: ShortArray,
    val faceAlphas: IntArray?,
    val faceRenderTypes: IntArray?,
    val minX: Int,
    val maxX: Int,
    val minY: Int,
    val maxY: Int,
    val minZ: Int,
    val maxZ: Int,
) {
    val width: Int get() = maxX - minX
    val height: Int get() = maxY - minY
    val depth: Int get() = maxZ - minZ
}
