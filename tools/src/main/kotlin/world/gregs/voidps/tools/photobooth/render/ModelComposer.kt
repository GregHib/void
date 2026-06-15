package world.gregs.voidps.tools.photobooth.render

import io.blurite.cache.model.MeshDecodingOption
import io.blurite.cache.model.Model
import io.netty.buffer.Unpooled

/**
 * Decodes, merges and recolours cache models into a flat [RenderModel].
 *
 * Ported and trimmed from Quill's `ModelDecoderAdapter` — only the composite/recolour path is kept
 * (NPC/object/animation/texture paths dropped). Model bytes come from an injected [ModelDataSource]
 * rather than Quill's static CacheManager, so a single void cache handle is used throughout.
 */
class ModelComposer(private val source: ModelDataSource) {

    private val options = arrayOf(MeshDecodingOption.PreserveOriginalData)

    private fun decode(modelId: Int): Model? {
        if (modelId < 0) return null
        val data = source.modelData(modelId) ?: return null
        if (data.isEmpty()) return null
        return Model.decode(modelId, Unpooled.wrappedBuffer(data), *options)
    }

    /**
     * Decodes each of [modelIds], upscales legacy (version <= 12) meshes to a common scale, merges
     * them, then applies the original->modified HSL colour swap. Returns null if no model decodes.
     */
    fun compose(id: Int, modelIds: IntArray, originalColours: IntArray?, modifiedColours: IntArray?): RenderModel? {
        if (modelIds.isEmpty()) return null
        val models = ArrayList<Model>(modelIds.size)
        for (modelId in modelIds) {
            val model = decode(modelId) ?: continue
            if (model.version <= 12) {
                model.resize(512, 512, 512)
            }
            models.add(model)
        }
        if (models.isEmpty()) return null
        val merged = if (models.size == 1) models[0] else Model.merge(id, models)
        val render = fromDecodedModel(id, merged)
        return applyColours(render, originalColours, modifiedColours)
    }

    private fun fromDecodedModel(id: Int, model: Model): RenderModel {
        val count = model.vertexCount
        val faces = model.triangleCount
        val x = cloneOrEmpty(model.vertexPositionsX, count)
        val y = cloneOrEmpty(model.vertexPositionsY, count)
        val z = cloneOrEmpty(model.vertexPositionsZ, count)
        val a = cloneOrEmpty(model.triangleVertex1, faces)
        val b = cloneOrEmpty(model.triangleVertex2, faces)
        val c = cloneOrEmpty(model.triangleVertex3, faces)
        val colours = model.triangleColors?.copyOf() ?: ShortArray(faces)
        val alphas = model.triangleAlphas?.copyOf()
        val renderTypes = model.triangleRenderTypes?.copyOf()
        var minX = 0; var maxX = 0; var minY = 0; var maxY = 0; var minZ = 0; var maxZ = 0
        if (count > 0) {
            minX = x[0]; maxX = x[0]; minY = y[0]; maxY = y[0]; minZ = z[0]; maxZ = z[0]
            for (i in 1 until count) {
                if (x[i] < minX) minX = x[i]; if (x[i] > maxX) maxX = x[i]
                if (y[i] < minY) minY = y[i]; if (y[i] > maxY) maxY = y[i]
                if (z[i] < minZ) minZ = z[i]; if (z[i] > maxZ) maxZ = z[i]
            }
        }
        return RenderModel(id, count, faces, x, y, z, a, b, c, colours, alphas, renderTypes,
            minX, maxX, minY, maxY, minZ, maxZ)
    }

    private fun applyColours(base: RenderModel, originalColours: IntArray?, modifiedColours: IntArray?): RenderModel {
        if (originalColours == null || modifiedColours == null || originalColours.isEmpty() || modifiedColours.isEmpty()) {
            return base
        }
        val recoloured = base.faceColors.copyOf()
        val pairs = minOf(originalColours.size, modifiedColours.size)
        for (i in 0 until pairs) {
            val from = originalColours[i] and 0xFFFF
            val to = (modifiedColours[i] and 0xFFFF).toShort()
            for (face in recoloured.indices) {
                if ((recoloured[face].toInt() and 0xFFFF) == from) {
                    recoloured[face] = to
                }
            }
        }
        return RenderModel(base.id, base.vertexCount, base.faceCount, base.verticesX, base.verticesY, base.verticesZ,
            base.faceA, base.faceB, base.faceC, recoloured, base.faceAlphas, base.faceRenderTypes,
            base.minX, base.maxX, base.minY, base.maxY, base.minZ, base.maxZ)
    }

    private fun cloneOrEmpty(source: IntArray?, length: Int): IntArray = source?.copyOf() ?: IntArray(length)

    companion object {
        /**
         * Concatenates already-flattened [RenderModel]s (each at a common scale) into one, offsetting
         * face vertex indices. Used to combine separately-recoloured body parts and equipment.
         */
        fun merge(id: Int, parts: List<RenderModel>): RenderModel? {
            val nonEmpty = parts.filter { it.vertexCount > 0 && it.faceCount > 0 }
            if (nonEmpty.isEmpty()) return null
            if (nonEmpty.size == 1) return nonEmpty[0]
            val vertexTotal = nonEmpty.sumOf { it.vertexCount }
            val faceTotal = nonEmpty.sumOf { it.faceCount }
            val vx = IntArray(vertexTotal); val vy = IntArray(vertexTotal); val vz = IntArray(vertexTotal)
            val fa = IntArray(faceTotal); val fb = IntArray(faceTotal); val fc = IntArray(faceTotal)
            val colours = ShortArray(faceTotal)
            val alphas = IntArray(faceTotal)
            val renderTypes = IntArray(faceTotal)
            var vOffset = 0; var fOffset = 0
            for (part in nonEmpty) {
                System.arraycopy(part.verticesX, 0, vx, vOffset, part.vertexCount)
                System.arraycopy(part.verticesY, 0, vy, vOffset, part.vertexCount)
                System.arraycopy(part.verticesZ, 0, vz, vOffset, part.vertexCount)
                for (f in 0 until part.faceCount) {
                    fa[fOffset + f] = part.faceA[f] + vOffset
                    fb[fOffset + f] = part.faceB[f] + vOffset
                    fc[fOffset + f] = part.faceC[f] + vOffset
                    colours[fOffset + f] = part.faceColors[f]
                    alphas[fOffset + f] = part.faceAlphas?.getOrNull(f) ?: 0
                    renderTypes[fOffset + f] = part.faceRenderTypes?.getOrNull(f) ?: 0
                }
                vOffset += part.vertexCount
                fOffset += part.faceCount
            }
            var minX = vx[0]; var maxX = vx[0]; var minY = vy[0]; var maxY = vy[0]; var minZ = vz[0]; var maxZ = vz[0]
            for (i in 1 until vertexTotal) {
                if (vx[i] < minX) minX = vx[i]; if (vx[i] > maxX) maxX = vx[i]
                if (vy[i] < minY) minY = vy[i]; if (vy[i] > maxY) maxY = vy[i]
                if (vz[i] < minZ) minZ = vz[i]; if (vz[i] > maxZ) maxZ = vz[i]
            }
            return RenderModel(id, vertexTotal, faceTotal, vx, vy, vz, fa, fb, fc, colours, alphas, renderTypes,
                minX, maxX, minY, maxY, minZ, maxZ)
        }
    }
}
