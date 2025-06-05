package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class WorldMapInfoDefinition(
    override var id: Int = -1,
    var spriteId: Int = -1,
    var highlightSpriteId: Int = -1,
    var name: String? = null,
    var anInt1058: Int = 0,
    var anInt1054: Int = -1,
    var fontSize: Int = 0,
    var aBoolean1047: Boolean = true,
    var hiddenOnWorldMap: Boolean = false,
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
    var clientScript: Int = -1,
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
    var anInt1068: Int = 2147483647,
    var anInt1089: Int = -2147483648,
    var anInt1051: Int = 2147483647,
    var anInt1060: Int = -2147483648,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorldMapInfoDefinition

        if (id != other.id) return false
        if (spriteId != other.spriteId) return false
        if (highlightSpriteId != other.highlightSpriteId) return false
        if (name != other.name) return false
        if (anInt1058 != other.anInt1058) return false
        if (anInt1054 != other.anInt1054) return false
        if (fontSize != other.fontSize) return false
        if (aBoolean1047 != other.aBoolean1047) return false
        if (hiddenOnWorldMap != other.hiddenOnWorldMap) return false
        if (aBoolean1063 != other.aBoolean1063) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (anInt1087 != other.anInt1087) return false
        if (anInt1042 != other.anInt1042) return false
        if (!aStringArray1065.contentEquals(other.aStringArray1065)) return false
        if (anIntArray1049 != null) {
            if (other.anIntArray1049 == null) return false
            if (!anIntArray1049.contentEquals(other.anIntArray1049)) return false
        } else if (other.anIntArray1049 != null) return false
        if (anInt1084 != other.anInt1084) return false
        if (anIntArray1066 != null) {
            if (other.anIntArray1066 == null) return false
            if (!anIntArray1066.contentEquals(other.anIntArray1066)) return false
        } else if (other.anIntArray1066 != null) return false
        if (aByteArray1057 != null) {
            if (other.aByteArray1057 == null) return false
            if (!aByteArray1057.contentEquals(other.aByteArray1057)) return false
        } else if (other.aByteArray1057 != null) return false
        if (aBoolean1064 != other.aBoolean1064) return false
        if (aString1045 != other.aString1045) return false
        if (anInt1093 != other.anInt1093) return false
        if (clientScript != other.clientScript) return false
        if (anInt1048 != other.anInt1048) return false
        if (anInt1044 != other.anInt1044) return false
        if (anInt1078 != other.anInt1078) return false
        if (anInt1072 != other.anInt1072) return false
        if (anInt1081 != other.anInt1081) return false
        if (anInt1077 != other.anInt1077) return false
        if (anInt1074 != other.anInt1074) return false
        if (anInt1050 != other.anInt1050) return false
        if (anInt1080 != other.anInt1080) return false
        if (anInt1071 != other.anInt1071) return false
        if (anInt1092 != other.anInt1092) return false
        if (anInt1068 != other.anInt1068) return false
        if (anInt1089 != other.anInt1089) return false
        if (anInt1051 != other.anInt1051) return false
        if (anInt1060 != other.anInt1060) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + spriteId
        result = 31 * result + highlightSpriteId
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + anInt1058
        result = 31 * result + anInt1054
        result = 31 * result + fontSize
        result = 31 * result + aBoolean1047.hashCode()
        result = 31 * result + hiddenOnWorldMap.hashCode()
        result = 31 * result + aBoolean1063.hashCode()
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + anInt1087
        result = 31 * result + anInt1042
        result = 31 * result + aStringArray1065.contentHashCode()
        result = 31 * result + (anIntArray1049?.contentHashCode() ?: 0)
        result = 31 * result + anInt1084
        result = 31 * result + (anIntArray1066?.contentHashCode() ?: 0)
        result = 31 * result + (aByteArray1057?.contentHashCode() ?: 0)
        result = 31 * result + aBoolean1064.hashCode()
        result = 31 * result + (aString1045?.hashCode() ?: 0)
        result = 31 * result + anInt1093
        result = 31 * result + clientScript
        result = 31 * result + anInt1048
        result = 31 * result + anInt1044
        result = 31 * result + anInt1078
        result = 31 * result + anInt1072
        result = 31 * result + anInt1081
        result = 31 * result + anInt1077
        result = 31 * result + anInt1074
        result = 31 * result + anInt1050
        result = 31 * result + anInt1080
        result = 31 * result + anInt1071
        result = 31 * result + anInt1092
        result = 31 * result + anInt1068
        result = 31 * result + anInt1089
        result = 31 * result + anInt1051
        result = 31 * result + anInt1060
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }
}