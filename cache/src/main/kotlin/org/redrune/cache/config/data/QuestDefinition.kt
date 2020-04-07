package org.redrune.cache.config.data

import org.redrune.cache.Definition
import org.redrune.cache.definition.Parameterized

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class QuestDefinition(
    override var id: Int = -1,
    var aString2211: String? = null,
    var aString2202: String? = null,
    var anIntArrayArray2208: Array<IntArray>? = null,
    var anIntArrayArray2193: Array<IntArray>? = null,
    var anIntArray2209: IntArray? = null,
    var anIntArray2207: IntArray? = null,
    var anIntArrayArray2210: Array<IntArray>? = null,
    var anInt2188: Int = -1,
    var anIntArray2200: IntArray? = null,
    var anIntArray2191: IntArray? = null,
    var anIntArray2199: IntArray? = null,
    var aStringArray2201: Array<String?>? = null,
    var anIntArray2204: IntArray? = null,
    var anIntArray2195: IntArray? = null,
    var anIntArray2190: IntArray? = null,
    var aStringArray2198: Array<String?>? = null,
    override var params: HashMap<Long, Any>? = null
) : Definition, Parameterized