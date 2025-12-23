package world.gregs.voidps.cache.type.types

import world.gregs.voidps.cache.type.Type

data class NPCType(
    override val id: Int = -1,
    var name: String = "null",
    var size: Int = 1,
    var options: Array<String?> = arrayOf(null, null, null, null, null, "Examine"),
    var combat: Int = -1,
    var varbit: Int = -1,
    var varp: Int = -1,
    var transforms: IntArray? = null,
    var walkMode: Byte = 0,
    var renderEmote: Int = -1,
    var idleSound: Int = -1,
    var crawlSound: Int = -1,
    var walkSound: Int = -1,
    var runSound: Int = -1,
    var soundDistance: Int = 0,
    var stringId: String = "",
    var extras: Map<String, Any>? = null,
) : Type {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NPCType

        if (id != other.id) return false
        if (size != other.size) return false
        if (combat != other.combat) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (walkMode != other.walkMode) return false
        if (renderEmote != other.renderEmote) return false
        if (idleSound != other.idleSound) return false
        if (crawlSound != other.crawlSound) return false
        if (walkSound != other.walkSound) return false
        if (runSound != other.runSound) return false
        if (soundDistance != other.soundDistance) return false
        if (name != other.name) return false
        if (!options.contentEquals(other.options)) return false
        if (!transforms.contentEquals(other.transforms)) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + size
        result = 31 * result + combat
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + walkMode
        result = 31 * result + renderEmote
        result = 31 * result + idleSound
        result = 31 * result + crawlSound
        result = 31 * result + walkSound
        result = 31 * result + runSound
        result = 31 * result + soundDistance
        result = 31 * result + name.hashCode()
        result = 31 * result + options.contentHashCode()
        result = 31 * result + (transforms?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        val EMPTY = NPCType()
    }
}