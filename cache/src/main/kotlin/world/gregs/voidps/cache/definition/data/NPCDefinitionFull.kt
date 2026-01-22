package world.gregs.voidps.cache.definition.data

import world.gregs.config.param.Param
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.*

data class NPCDefinitionFull(
    override var id: Int = -1,
    var modelIds: IntArray? = null,
    var name: String = "null",
    var size: Int = 1,
    var options: Array<String?> = arrayOf(null, null, null, null, null, "Examine"),
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    override var recolourPalette: ByteArray? = null,
    var dialogueModels: IntArray? = null,
    var drawMinimapDot: Boolean = true,
    var combat: Int = -1,
    var scaleXY: Int = 128,
    var scaleZ: Int = 128,
    var priorityRender: Boolean = false,
    var lightModifier: Int = 0,
    var shadowModifier: Int = 0,
    var headIcon: Int = -1,
    var rotation: Int = 32,
    override var varbit: Int = -1,
    override var varp: Int = -1,
    override var transforms: IntArray? = null,
    var clickable: Boolean = true,
    var slowWalk: Boolean = true,
    var animateIdle: Boolean = true,
    var primaryShadowColour: Short = 0,
    var secondaryShadowColour: Short = 0,
    var primaryShadowModifier: Byte = -96,
    var secondaryShadowModifier: Byte = -16,
    var walkMode: Byte = 0,
    var translations: Array<IntArray?>? = null,
    var hitbarSprite: Int = -1,
    var height: Int = -1,
    var respawnDirection: Byte = 4,
    var renderEmote: Int = -1,
    var idleSound: Int = -1,
    var crawlSound: Int = -1,
    var walkSound: Int = -1,
    var runSound: Int = -1,
    var soundDistance: Int = 0,
    var primaryCursorOp: Int = -1,
    var primaryCursor: Int = -1,
    var secondaryCursorOp: Int = -1,
    var secondaryCursor: Int = -1,
    var attackCursor: Int = -1,
    var armyIcon: Int = -1,
    var spriteId: Int = -1,
    var ambientSoundVolume: Int = 255,
    var visiblePriority: Boolean = false,
    var mapFunction: Int = -1,
    var invisiblePriority: Boolean = false,
    var hue: Byte = 0,
    var saturation: Byte = 0,
    var lightness: Byte = 0,
    var opacity: Byte = 0,
    var mainOptionIndex: Byte = -1,
    var campaigns: IntArray? = null,
    var vorbis: Boolean = false,
    var slayerType: Int = -1,
    var soundRateMin: Int = 256,
    var soundRateMax: Int = 256,
    var pickSizeShift: Int = 0,
    var soundRangeMin: Int = 0,
    override var params: Map<Int, Any>? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition,
    Transforms,
    Recolourable,
    ColourPalette,
    Parameterized,
    Extra, Param {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NPCDefinitionFull

        if (id != other.id) return false
        if (modelIds != null) {
            if (other.modelIds == null) return false
            if (!modelIds.contentEquals(other.modelIds)) return false
        } else if (other.modelIds != null) {
            return false
        }
        if (name != other.name) return false
        if (size != other.size) return false
        if (!options.contentEquals(other.options)) return false
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
        if (dialogueModels != null) {
            if (other.dialogueModels == null) return false
            if (!dialogueModels.contentEquals(other.dialogueModels)) return false
        } else if (other.dialogueModels != null) {
            return false
        }
        if (drawMinimapDot != other.drawMinimapDot) return false
        if (combat != other.combat) return false
        if (scaleXY != other.scaleXY) return false
        if (scaleZ != other.scaleZ) return false
        if (priorityRender != other.priorityRender) return false
        if (lightModifier != other.lightModifier) return false
        if (shadowModifier != other.shadowModifier) return false
        if (headIcon != other.headIcon) return false
        if (rotation != other.rotation) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (transforms != null) {
            if (other.transforms == null) return false
            if (!transforms.contentEquals(other.transforms)) return false
        } else if (other.transforms != null) {
            return false
        }
        if (clickable != other.clickable) return false
        if (slowWalk != other.slowWalk) return false
        if (animateIdle != other.animateIdle) return false
        if (primaryShadowColour != other.primaryShadowColour) return false
        if (secondaryShadowColour != other.secondaryShadowColour) return false
        if (primaryShadowModifier != other.primaryShadowModifier) return false
        if (secondaryShadowModifier != other.secondaryShadowModifier) return false
        if (walkMode != other.walkMode) return false
        if (translations != null) {
            if (other.translations == null) return false
            if (!translations.contentDeepEquals(other.translations)) return false
        } else if (other.translations != null) {
            return false
        }
        if (hitbarSprite != other.hitbarSprite) return false
        if (height != other.height) return false
        if (respawnDirection != other.respawnDirection) return false
        if (renderEmote != other.renderEmote) return false
        if (idleSound != other.idleSound) return false
        if (crawlSound != other.crawlSound) return false
        if (walkSound != other.walkSound) return false
        if (runSound != other.runSound) return false
        if (soundDistance != other.soundDistance) return false
        if (primaryCursorOp != other.primaryCursorOp) return false
        if (primaryCursor != other.primaryCursor) return false
        if (secondaryCursorOp != other.secondaryCursorOp) return false
        if (secondaryCursor != other.secondaryCursor) return false
        if (attackCursor != other.attackCursor) return false
        if (armyIcon != other.armyIcon) return false
        if (spriteId != other.spriteId) return false
        if (ambientSoundVolume != other.ambientSoundVolume) return false
        if (visiblePriority != other.visiblePriority) return false
        if (mapFunction != other.mapFunction) return false
        if (invisiblePriority != other.invisiblePriority) return false
        if (hue != other.hue) return false
        if (saturation != other.saturation) return false
        if (lightness != other.lightness) return false
        if (opacity != other.opacity) return false
        if (mainOptionIndex != other.mainOptionIndex) return false
        if (campaigns != null) {
            if (other.campaigns == null) return false
            if (!campaigns.contentEquals(other.campaigns)) return false
        } else if (other.campaigns != null) {
            return false
        }
        if (vorbis != other.vorbis) return false
        if (slayerType != other.slayerType) return false
        if (soundRateMin != other.soundRateMin) return false
        if (soundRateMax != other.soundRateMax) return false
        if (pickSizeShift != other.pickSizeShift) return false
        if (soundRangeMin != other.soundRangeMin) return false
        if (params != other.params) return false
        if (stringId != other.stringId) return false
        return extras == other.extras
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (modelIds?.contentHashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + size
        result = 31 * result + options.contentHashCode()
        result = 31 * result + (originalColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedColours?.contentHashCode() ?: 0)
        result = 31 * result + (originalTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (modifiedTextureColours?.contentHashCode() ?: 0)
        result = 31 * result + (recolourPalette?.contentHashCode() ?: 0)
        result = 31 * result + (dialogueModels?.contentHashCode() ?: 0)
        result = 31 * result + drawMinimapDot.hashCode()
        result = 31 * result + combat
        result = 31 * result + scaleXY
        result = 31 * result + scaleZ
        result = 31 * result + priorityRender.hashCode()
        result = 31 * result + lightModifier
        result = 31 * result + shadowModifier
        result = 31 * result + headIcon
        result = 31 * result + rotation
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + (transforms?.contentHashCode() ?: 0)
        result = 31 * result + clickable.hashCode()
        result = 31 * result + slowWalk.hashCode()
        result = 31 * result + animateIdle.hashCode()
        result = 31 * result + primaryShadowColour
        result = 31 * result + secondaryShadowColour
        result = 31 * result + primaryShadowModifier
        result = 31 * result + secondaryShadowModifier
        result = 31 * result + walkMode
        result = 31 * result + (translations?.contentDeepHashCode() ?: 0)
        result = 31 * result + hitbarSprite
        result = 31 * result + height
        result = 31 * result + respawnDirection
        result = 31 * result + renderEmote
        result = 31 * result + idleSound
        result = 31 * result + crawlSound
        result = 31 * result + walkSound
        result = 31 * result + runSound
        result = 31 * result + soundDistance
        result = 31 * result + primaryCursorOp
        result = 31 * result + primaryCursor
        result = 31 * result + secondaryCursorOp
        result = 31 * result + secondaryCursor
        result = 31 * result + attackCursor
        result = 31 * result + armyIcon
        result = 31 * result + spriteId
        result = 31 * result + ambientSoundVolume
        result = 31 * result + visiblePriority.hashCode()
        result = 31 * result + mapFunction
        result = 31 * result + invisiblePriority.hashCode()
        result = 31 * result + hue
        result = 31 * result + saturation
        result = 31 * result + lightness
        result = 31 * result + opacity
        result = 31 * result + mainOptionIndex
        result = 31 * result + (campaigns?.contentHashCode() ?: 0)
        result = 31 * result + vorbis.hashCode()
        result = 31 * result + slayerType
        result = 31 * result + soundRateMin
        result = 31 * result + soundRateMax
        result = 31 * result + pickSizeShift
        result = 31 * result + soundRangeMin
        result = 31 * result + (params?.hashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = NPCDefinitionFull()
    }
}
