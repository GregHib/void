package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Recolourable

data class GraphicDefinition(
    override var id: Int = -1,
    var modelId: Int = 0,
    var animationId: Int = -1,
    var sizeXY: Int = 128,
    var sizeZ: Int = 128,
    var rotation: Int = 0,
    var ambience: Int = 0,
    var contrast: Int = 0,
    var aByte2381: Byte = 0,
    var anInt2385: Int = -1,
    var aBoolean2402: Boolean = false,
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Recolourable,
    Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphicDefinition

        if (id != other.id) return false
        if (modelId != other.modelId) return false
        if (animationId != other.animationId) return false
        if (sizeXY != other.sizeXY) return false
        if (sizeZ != other.sizeZ) return false
        if (rotation != other.rotation) return false
        if (ambience != other.ambience) return false
        if (contrast != other.contrast) return false
        if (aByte2381 != other.aByte2381) return false
        if (anInt2385 != other.anInt2385) return false
        if (aBoolean2402 != other.aBoolean2402) return false
        if (originalColours != null) {
            if (other.originalColours == null) return false
            if (!originalColours.contentEquals(other.originalColours)) return false
        } else if (other.originalColours != null) {
            return false
        }
        if (modifiedColours != null) {
            if (other.modifiedColours == null) return false
            if (!modifiedColours.contentEquals(other.modifiedColours)) return false
        } else if (other.modifiedColours != null) {
            return false
        }
        if (originalTextureColours != null) {
            if (other.originalTextureColours == null) return false
            if (!originalTextureColours.contentEquals(other.originalTextureColours)) return false
        } else if (other.originalTextureColours != null) {
            return false
        }
        if (modifiedTextureColours != null) {
            if (other.modifiedTextureColours == null) return false
            if (!modifiedTextureColours.contentEquals(other.modifiedTextureColours)) return false
        } else if (other.modifiedTextureColours != null) {
            return false
        }
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + modelId
        result = 31 * result + animationId
        result = 31 * result + sizeXY
        result = 31 * result + sizeZ
        result = 31 * result + rotation
        result = 31 * result + ambience
        result = 31 * result + contrast
        result = 31 * result + aByte2381
        result = 31 * result + anInt2385
        result = 31 * result + aBoolean2402.hashCode()
        result = 31 * result + (originalColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedColours?.contentHashCode() ?: 0)
        result = 31 * result + (originalTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + extras.hashCode()
        return result
    }
    companion object {
        val EMPTY = GraphicDefinition()
    }
}
