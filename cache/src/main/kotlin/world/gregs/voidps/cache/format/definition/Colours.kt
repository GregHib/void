package world.gregs.voidps.cache.format.definition

//import world.gregs.voidps.cache.format.definition.internal.ColourSerializer
//import world.gregs.voidps.cache.format.definition.internal.MediumSerializer

//@Serializable(with = ColourSerializer::class)
data class Colours(var original: ShortArray, var modified: ShortArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Colours

        if (!original.contentEquals(other.original)) return false
        if (!modified.contentEquals(other.modified)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = original.contentHashCode()
        result = 31 * result + modified.contentHashCode()
        return result
    }
}
