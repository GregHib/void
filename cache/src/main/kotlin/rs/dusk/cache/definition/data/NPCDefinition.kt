package rs.dusk.cache.definition.data

import rs.dusk.cache.Definition
import rs.dusk.cache.definition.Parameterized
import rs.dusk.cache.definition.Recolourable
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class NPCDefinition(
    override var id: Int = -1,
    var modelIds: IntArray? = null,
    var name: String = "null",
    var size: Int = 1,
    var options: Array<String?> = arrayOf(null, null, null, null, null, "Examine"),
    override var originalColours: ShortArray? = null,
    override var modifiedColours: ShortArray? = null,
    override var originalTextureColours: ShortArray? = null,
    override var modifiedTextureColours: ShortArray? = null,
    var recolourPalette: ByteArray? = null,
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
    var varbit: Int = -1,
    var varp: Int = -1,
    var morphs: IntArray? = null,
    var clickable: Boolean = true,
    var slowWalk: Boolean = true,
    var animateIdle: Boolean = true,
    var primaryShadowColour: Short = 0,
    var secondaryShadowColour: Short = 0,
    var primaryShadowModifier: Byte = -96,
    var secondaryShadowModifier: Byte = -16,
    var walkMask: Byte = 0,
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
    var aBoolean2883: Boolean = false,
    var anInt2803: Int = -1,
    var anInt2844: Int = 256,
    var anInt2852: Int = 256,
    var anInt2831: Int = 0,
    var anInt2862: Int = 0,
    override var params: HashMap<Long, Any>? = null
) : Definition, Recolourable, Parameterized