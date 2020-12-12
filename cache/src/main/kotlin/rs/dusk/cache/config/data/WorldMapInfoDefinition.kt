package rs.dusk.cache.config.data

import rs.dusk.cache.Definition
import rs.dusk.cache.definition.Parameterized

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
@Suppress("ArrayInDataClass")
data class WorldMapInfoDefinition(
    override var id: Int = -1,
    var spriteId: Int = -1,
    var highlightSpriteId: Int = -1,
    var name: String? = null,
    var anInt1058: Int = 0,
    var anInt1054: Int = -1,
    var fontSize: Int = 0,
    var aBoolean1047: Boolean = true,
    var aBoolean1079: Boolean = false,
    var aBoolean1063: Boolean = true,
    var varbit: Int = -1,
    var varp: Int = -1,
    var anInt1087: Int = 0,
    var anInt1042: Int = 0,
    var aStringArray1065: Array<String?> = arrayOfNulls(5),
    var anIntArray1049: IntArray? = null,
    var anInt1084: Int = 0,
    var anIntArray1066: IntArray? = null,
    var aByteArray1057: ByteArray? = null,
    var aBoolean1064: Boolean = true,
    var aString1045: String? = null,
    var anInt1093: Int = -1,
    var anInt1067: Int = -1,
    var anInt1048: Int = -1,
    var anInt1044: Int = -1,
    var anInt1078: Int = 0,
    var anInt1072: Int = 0,
    var anInt1081: Int = 0,
    var anInt1077: Int = 0,
    var anInt1074: Int = -1,
    var anInt1050: Int = -1,
    var anInt1080: Int = -1,
    var anInt1071: Int = 0,
    var anInt1092: Int = 0,
    override var params: HashMap<Long, Any>? = null,
    var anInt1068: Int = 2147483647,
    var anInt1089: Int = -2147483648,
    var anInt1051: Int = 2147483647,
    var anInt1060: Int = -2147483648
) : Definition, Parameterized