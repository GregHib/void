package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.*

data class ObjectDefinitionFull(
    override var id: Int = -1,
    var modelIds: Array<IntArray>? = null,
    var modelTypes: ByteArray? = null,
    var name: String = "null",
    var sizeX: Int = 1,
    var sizeY: Int = 1,
    var blocksSky: Boolean = true,
    var solid: Int = 2,
    var interactive: Int = -1,
    var contouredGround: Byte = 0,
    var delayShading: Boolean = false,
    var offsetMultiplier: Int = 64,
    var brightness: Int = 0,
    var options: Array<String?>? = null,
    var contrast: Int = 0,
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    override var recolourPalette: ByteArray? = null,
    var mirrored: Boolean = false,
    var castsShadow: Boolean = true,
    var modelSizeX: Int = 128,
    var modelSizeZ: Int = 128,
    var modelSizeY: Int = 128,
    var blockFlag: Int = 0,
    var offsetX: Int = 0,
    var offsetZ: Int = 0,
    var offsetY: Int = 0,
    var blocksLand: Boolean = false,
    var ignoreOnRoute: Boolean = false,
    var supportItems: Int = -1,
    override var varbit: Int = -1,
    override var varp: Int = -1,
    override var transforms: IntArray? = null,
    var anInt3015: Int = -1,
    var anInt3012: Int = 0,
    var anInt2989: Int = 0,
    var anInt2971: Int = 0,
    var anIntArray3036: IntArray? = null,
    var anInt3023: Int = -1,
    var hideMinimap: Boolean = false,
    var aBoolean2972: Boolean = true,
    var animateImmediately: Boolean = true,
    var isMembers: Boolean = false,
    var aBoolean3056: Boolean = false,
    var aBoolean2998: Boolean = false,
    var anInt2987: Int = -1,
    var anInt3008: Int = -1,
    var anInt3038: Int = -1,
    var anInt3013: Int = -1,
    var anInt2958: Int = 0,
    var mapscene: Int = -1,
    var culling: Int = -1,
    var anInt3024: Int = 255,
    var invertMapScene: Boolean = false,
    var animations: IntArray? = null,
    var percents: IntArray? = null,
    var mapDefinitionId: Int = -1,
    var anIntArray2981: IntArray? = null,
    var aByte2974: Byte = 0,
    var aByte3045: Byte = 0,
    var aByte3052: Byte = 0,
    var aByte2960: Byte = 0,
    var anInt2964: Int = 0,
    var anInt2963: Int = 0,
    var anInt3018: Int = 0,
    var anInt2983: Int = 0,
    var aBoolean2961: Boolean = false,
    var aBoolean2993: Boolean = false,
    var anInt3032: Int = 960,
    var anInt2962: Int = 0,
    var anInt3050: Int = 256,
    var anInt3020: Int = 256,
    var aBoolean2992: Boolean = false,
    var anInt2975: Int = 0,
    override var params: Map<Int, Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Transforms,
    Recolourable,
    ColourPalette,
    Parameterized,
    Extra {

    var block: Int = PROJECTILE or ROUTE

    fun optionsIndex(option: String): Int = if (options != null) {
        options!!.indexOf(option)
    } else {
        -1
    }

    fun containsOption(option: String): Boolean = if (options != null) {
        options!!.contains(option)
    } else {
        false
    }

    fun containsOption(index: Int, option: String): Boolean = if (options != null) {
        options!![index] == option
    } else {
        false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectDefinitionFull

        if (id != other.id) return false
        if (modelIds != null) {
            if (other.modelIds == null) return false
            if (!modelIds.contentDeepEquals(other.modelIds)) return false
        } else if (other.modelIds != null) {
            return false
        }
        if (modelTypes != null) {
            if (other.modelTypes == null) return false
            if (!modelTypes.contentEquals(other.modelTypes)) return false
        } else if (other.modelTypes != null) {
            return false
        }
        if (name != other.name) return false
        if (sizeX != other.sizeX) return false
        if (sizeY != other.sizeY) return false
        if (blocksSky != other.blocksSky) return false
        if (solid != other.solid) return false
        if (interactive != other.interactive) return false
        if (contouredGround != other.contouredGround) return false
        if (delayShading != other.delayShading) return false
        if (offsetMultiplier != other.offsetMultiplier) return false
        if (brightness != other.brightness) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) {
            return false
        }
        if (contrast != other.contrast) return false
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
        if (recolourPalette != null) {
            if (other.recolourPalette == null) return false
            if (!recolourPalette.contentEquals(other.recolourPalette)) return false
        } else if (other.recolourPalette != null) {
            return false
        }
        if (mirrored != other.mirrored) return false
        if (castsShadow != other.castsShadow) return false
        if (modelSizeX != other.modelSizeX) return false
        if (modelSizeZ != other.modelSizeZ) return false
        if (modelSizeY != other.modelSizeY) return false
        if (blockFlag != other.blockFlag) return false
        if (offsetX != other.offsetX) return false
        if (offsetZ != other.offsetZ) return false
        if (offsetY != other.offsetY) return false
        if (blocksLand != other.blocksLand) return false
        if (ignoreOnRoute != other.ignoreOnRoute) return false
        if (supportItems != other.supportItems) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (transforms != null) {
            if (other.transforms == null) return false
            if (!transforms.contentEquals(other.transforms)) return false
        } else if (other.transforms != null) {
            return false
        }
        if (anInt3015 != other.anInt3015) return false
        if (anInt3012 != other.anInt3012) return false
        if (anInt2989 != other.anInt2989) return false
        if (anInt2971 != other.anInt2971) return false
        if (anIntArray3036 != null) {
            if (other.anIntArray3036 == null) return false
            if (!anIntArray3036.contentEquals(other.anIntArray3036)) return false
        } else if (other.anIntArray3036 != null) {
            return false
        }
        if (anInt3023 != other.anInt3023) return false
        if (hideMinimap != other.hideMinimap) return false
        if (aBoolean2972 != other.aBoolean2972) return false
        if (animateImmediately != other.animateImmediately) return false
        if (isMembers != other.isMembers) return false
        if (aBoolean3056 != other.aBoolean3056) return false
        if (aBoolean2998 != other.aBoolean2998) return false
        if (anInt2987 != other.anInt2987) return false
        if (anInt3008 != other.anInt3008) return false
        if (anInt3038 != other.anInt3038) return false
        if (anInt3013 != other.anInt3013) return false
        if (anInt2958 != other.anInt2958) return false
        if (mapscene != other.mapscene) return false
        if (culling != other.culling) return false
        if (anInt3024 != other.anInt3024) return false
        if (invertMapScene != other.invertMapScene) return false
        if (animations != null) {
            if (other.animations == null) return false
            if (!animations.contentEquals(other.animations)) return false
        } else if (other.animations != null) {
            return false
        }
        if (percents != null) {
            if (other.percents == null) return false
            if (!percents.contentEquals(other.percents)) return false
        } else if (other.percents != null) {
            return false
        }
        if (mapDefinitionId != other.mapDefinitionId) return false
        if (anIntArray2981 != null) {
            if (other.anIntArray2981 == null) return false
            if (!anIntArray2981.contentEquals(other.anIntArray2981)) return false
        } else if (other.anIntArray2981 != null) {
            return false
        }
        if (aByte2974 != other.aByte2974) return false
        if (aByte3045 != other.aByte3045) return false
        if (aByte3052 != other.aByte3052) return false
        if (aByte2960 != other.aByte2960) return false
        if (anInt2964 != other.anInt2964) return false
        if (anInt2963 != other.anInt2963) return false
        if (anInt3018 != other.anInt3018) return false
        if (anInt2983 != other.anInt2983) return false
        if (aBoolean2961 != other.aBoolean2961) return false
        if (aBoolean2993 != other.aBoolean2993) return false
        if (anInt3032 != other.anInt3032) return false
        if (anInt2962 != other.anInt2962) return false
        if (anInt3050 != other.anInt3050) return false
        if (anInt3020 != other.anInt3020) return false
        if (aBoolean2992 != other.aBoolean2992) return false
        if (anInt2975 != other.anInt2975) return false
        if (params != other.params) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (modelIds?.contentDeepHashCode() ?: 0)
        result = 31 * result + (modelTypes?.contentHashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + sizeX
        result = 31 * result + sizeY
        result = 31 * result + blocksSky.hashCode()
        result = 31 * result + solid
        result = 31 * result + interactive
        result = 31 * result + contouredGround
        result = 31 * result + delayShading.hashCode()
        result = 31 * result + offsetMultiplier
        result = 31 * result + brightness
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + contrast
        result = 31 * result + (originalColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedColours?.contentHashCode() ?: 0)
        result = 31 * result + (originalTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (recolourPalette?.contentHashCode() ?: 0)
        result = 31 * result + mirrored.hashCode()
        result = 31 * result + castsShadow.hashCode()
        result = 31 * result + modelSizeX
        result = 31 * result + modelSizeZ
        result = 31 * result + modelSizeY
        result = 31 * result + blockFlag
        result = 31 * result + offsetX
        result = 31 * result + offsetZ
        result = 31 * result + offsetY
        result = 31 * result + blocksLand.hashCode()
        result = 31 * result + ignoreOnRoute.hashCode()
        result = 31 * result + supportItems
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + (transforms?.contentHashCode() ?: 0)
        result = 31 * result + anInt3015
        result = 31 * result + anInt3012
        result = 31 * result + anInt2989
        result = 31 * result + anInt2971
        result = 31 * result + (anIntArray3036?.contentHashCode() ?: 0)
        result = 31 * result + anInt3023
        result = 31 * result + hideMinimap.hashCode()
        result = 31 * result + aBoolean2972.hashCode()
        result = 31 * result + animateImmediately.hashCode()
        result = 31 * result + isMembers.hashCode()
        result = 31 * result + aBoolean3056.hashCode()
        result = 31 * result + aBoolean2998.hashCode()
        result = 31 * result + anInt2987
        result = 31 * result + anInt3008
        result = 31 * result + anInt3038
        result = 31 * result + anInt3013
        result = 31 * result + anInt2958
        result = 31 * result + mapscene
        result = 31 * result + culling
        result = 31 * result + anInt3024
        result = 31 * result + invertMapScene.hashCode()
        result = 31 * result + (animations?.contentHashCode() ?: 0)
        result = 31 * result + (percents?.contentHashCode() ?: 0)
        result = 31 * result + mapDefinitionId
        result = 31 * result + (anIntArray2981?.contentHashCode() ?: 0)
        result = 31 * result + aByte2974
        result = 31 * result + aByte3045
        result = 31 * result + aByte3052
        result = 31 * result + aByte2960
        result = 31 * result + anInt2964
        result = 31 * result + anInt2963
        result = 31 * result + anInt3018
        result = 31 * result + anInt2983
        result = 31 * result + aBoolean2961.hashCode()
        result = 31 * result + aBoolean2993.hashCode()
        result = 31 * result + anInt3032
        result = 31 * result + anInt2962
        result = 31 * result + anInt3050
        result = 31 * result + anInt3020
        result = 31 * result + aBoolean2992.hashCode()
        result = 31 * result + anInt2975
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val ROUTE = 0x10
        const val PROJECTILE = 0x8
        val EMPTY = ObjectDefinitionFull()
    }
}
