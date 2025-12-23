package world.gregs.voidps.cache.type.types

import world.gregs.voidps.cache.type.Type

data class ObjectType(
    override val id: Int = -1,
    var name: String = "null",
    var sizeX: Int = 1,
    var sizeY: Int = 1,
    var solid: Int = 2,
    var interactive: Int = -1,
    var options: Array<String?>? = null,
    var mirrored: Boolean = false,
    var blockFlag: Int = 0,
    var varbit: Int = -1,
    var varp: Int = -1,
    var transforms: IntArray? = null,
    var stringId: String = "",
    var extras: Map<String, Any>? = null,
    var block: Int = PROJECTILE or ROUTE
) : Type {


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

        other as ObjectType

        if (id != other.id) return false
        if (sizeX != other.sizeX) return false
        if (sizeY != other.sizeY) return false
        if (solid != other.solid) return false
        if (interactive != other.interactive) return false
        if (mirrored != other.mirrored) return false
        if (blockFlag != other.blockFlag) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (name != other.name) return false
        if (!options.contentEquals(other.options)) return false
        if (!transforms.contentEquals(other.transforms)) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + sizeX
        result = 31 * result + sizeY
        result = 31 * result + solid
        result = 31 * result + interactive
        result = 31 * result + mirrored.hashCode()
        result = 31 * result + blockFlag
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + name.hashCode()
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + (transforms?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    companion object {
        const val ROUTE = 0x10
        const val PROJECTILE = 0x8
        val EMPTY = ObjectType()
    }
}