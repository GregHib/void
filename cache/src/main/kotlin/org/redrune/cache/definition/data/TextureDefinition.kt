package org.redrune.cache.definition.data

import org.redrune.cache.Definition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
data class TextureDefinition(
    override var id: Int = -1,
    var useTextureColour: Boolean = false,
    var aBoolean1204: Boolean = false,
    var aBoolean1205: Boolean = false,
    var aByte1217: Byte = 0,
    var aByte1225: Byte = 0,
    var type: Byte = 0,
    var aByte1213: Byte = 0,
    var colour: Int = 0,
    var aByte1211: Byte = 0,
    var aByte1203: Byte = 0,
    var aBoolean1222: Boolean = false,
    var aBoolean1216: Boolean = false,
    var aByte1207: Byte = 0,
    var aBoolean1212: Boolean = false,
    var aBoolean1210: Boolean = false,
    var aBoolean1215: Boolean = false,
    var anInt1202: Int = 0,
    var anInt1206: Int = 0,
    var anInt1226: Int = 0
) : Definition