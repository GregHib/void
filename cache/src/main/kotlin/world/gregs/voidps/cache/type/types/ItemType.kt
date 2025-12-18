package world.gregs.voidps.cache.type.types

import world.gregs.voidps.cache.type.Type


data class ItemType(
    override var id: Int = -1,
    val name: String = "null",
    val stackable: Int = 0,
    val cost: Int = 1,
    val members: Boolean = false,
    val floorOptions: Array<String?> = arrayOf(null, null, "Take", null, null, "Examine"),
    val options: Array<String?> = arrayOf(null, null, null, null, "Drop"),
    val exchangeable: Boolean = false,
    val dummyItem: Int = 0,
    val noteId: Int = -1,
    val notedTemplateId: Int = -1,
    val lendId: Int = -1,
    val lendTemplateId: Int = -1,
    val equipIndex: Int = -1,
    val stringId: String = "",
    val params: Map<Int, Any>? = null,
) : Type {

    val noted: Boolean
        get() = notedTemplateId != -1

    val lent: Boolean
        get() = lendTemplateId != -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemType

        if (id != other.id) return false
        if (name != other.name) return false
        if (stackable != other.stackable) return false
        if (cost != other.cost) return false
        if (members != other.members) return false
        if (!floorOptions.contentEquals(other.floorOptions)) return false
        if (!options.contentEquals(other.options)) return false
        if (exchangeable != other.exchangeable) return false
        if (dummyItem != other.dummyItem) return false
        if (noteId != other.noteId) return false
        if (notedTemplateId != other.notedTemplateId) return false
        if (lendId != other.lendId) return false
        if (lendTemplateId != other.lendTemplateId) return false
        if (equipIndex != other.equipIndex) return false
        if (stringId != other.stringId) return false
        return params == other.params
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + stackable
        result = 31 * result + cost
        result = 31 * result + members.hashCode()
        result = 31 * result + floorOptions.contentHashCode()
        result = 31 * result + options.contentHashCode()
        result = 31 * result + exchangeable.hashCode()
        result = 31 * result + dummyItem
        result = 31 * result + noteId
        result = 31 * result + notedTemplateId
        result = 31 * result + lendId
        result = 31 * result + lendTemplateId
        result = 31 * result + equipIndex
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (params?.hashCode() ?: 0)
        return result
    }

    fun toLend(item: ItemType?, template: ItemType?): ItemType? {
        if (item == null || template == null) {
            return null
        }
        return copy(
            params = item.params,
            members = item.members,
            floorOptions = item.floorOptions,
            cost = 0,
            name = item.name,
            equipIndex = item.equipIndex,
            options = Array(5) { if (it == 4) "Discard" else item.options[it] },
        )
    }

    fun toNote(template: ItemType?, item: ItemType?): ItemType? {
        if (item == null || template == null) {
            return null
        }
        return copy(
            cost = item.cost,
            name = item.name,
            stackable = 1,
            members = item.members
        )
    }

    companion object {
        val EMPTY = ItemType()
    }
}