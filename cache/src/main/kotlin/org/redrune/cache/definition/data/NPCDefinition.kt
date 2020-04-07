package org.redrune.cache.definition.data

import org.redrune.cache.Definition
import org.redrune.cache.definition.Parameterized
import org.redrune.cache.definition.Recolourable
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
    var aShort2863: Short = 0,
    var aShort2871: Short = 0,
    var aByte2877: Byte = -96,
    var aByte2868: Byte = -16,
    var walkMask: Byte = 0,
    var translations: Array<IntArray?>? = null,
    var anInt2878: Int = -1,
    var height: Int = -1,
    var respawnDirection: Byte = 4,
    var renderEmote: Int = -1,
    var anInt2812: Int = -1,
    var anInt2833: Int = -1,
    var anInt2809: Int = -1,
    var anInt2810: Int = -1,
    var anInt2864: Int = 0,
    var anInt2815: Int = -1,
    var anInt2859: Int = -1,
    var anInt2856: Int = -1,
    var anInt2886: Int = -1,
    var attackCursor: Int = -1,
    var armyIcon: Int = -1,
    var spriteId: Int = -1,
    var anInt2828: Int = 255,
    var aBoolean2843: Boolean = false,
    var mapFunction: Int = -1,
    var aBoolean2825: Boolean = false,
    var aByte2836: Byte = 0,
    var aByte2853: Byte = 0,
    var aByte2857: Byte = 0,
    var aByte2839: Byte = 0,
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