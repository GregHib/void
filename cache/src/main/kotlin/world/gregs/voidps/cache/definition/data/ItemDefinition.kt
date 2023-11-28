package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class ItemDefinition(
    override var id: Int = -1,
    var name: String = "null",
    var stackable: Int = 0,
    var cost: Int = 1,
    var members: Boolean = false,
    var primaryMaleModel: Int = -1,
    var primaryFemaleModel: Int = -1,
    var floorOptions: Array<String?> = arrayOf(null, null, "Take", null, null, "Examine"),
    var options: Array<String?> = arrayOf(null, null, null, null, "Drop"),
    var dummyItem: Int = 0,
    var noteId: Int = -1,
    var notedTemplateId: Int = -1,
    var lendId: Int = -1,
    var lendTemplateId: Int = -1,
    var singleNoteId: Int = -1,
    var singleNoteTemplateId: Int = -1,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null
) : Definition, Extra {

    val noted: Boolean
        get() = notedTemplateId != -1

    val lent: Boolean
        get() = lendTemplateId != -1

    val singleNote: Boolean
        get() = singleNoteTemplateId != -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemDefinition

        if (id != other.id) return false
        if (name != other.name) return false
        if (stackable != other.stackable) return false
        if (cost != other.cost) return false
        if (members != other.members) return false
        if (primaryMaleModel != other.primaryMaleModel) return false
        if (primaryFemaleModel != other.primaryFemaleModel) return false
        if (!floorOptions.contentEquals(other.floorOptions)) return false
        if (!options.contentEquals(other.options)) return false
        if (dummyItem != other.dummyItem) return false
        if (noteId != other.noteId) return false
        if (notedTemplateId != other.notedTemplateId) return false
        if (lendId != other.lendId) return false
        if (lendTemplateId != other.lendTemplateId) return false
        if (singleNoteId != other.singleNoteId) return false
        if (singleNoteTemplateId != other.singleNoteTemplateId) return false
        if (stringId != other.stringId) return false
        return extras == other.extras
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + stackable
        result = 31 * result + cost
        result = 31 * result + members.hashCode()
        result = 31 * result + primaryMaleModel
        result = 31 * result + primaryFemaleModel
        result = 31 * result + floorOptions.contentHashCode()
        result = 31 * result + options.contentHashCode()
        result = 31 * result + dummyItem
        result = 31 * result + noteId
        result = 31 * result + notedTemplateId
        result = 31 * result + lendId
        result = 31 * result + lendTemplateId
        result = 31 * result + singleNoteId
        result = 31 * result + singleNoteTemplateId
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    fun toLend(item: ItemDefinition?, template: ItemDefinition?) {
        if (item == null || template == null) {
            return
        }
        extras = item.extras
        members = item.members
        floorOptions = item.floorOptions
        primaryFemaleModel = item.primaryFemaleModel
        options = arrayOfNulls(5)
        cost = 0
        name = item.name
        primaryMaleModel = item.primaryMaleModel
        System.arraycopy(item.options, 0, options, 0, 4)
        options[4] = "Discard"
    }

    fun toNote(template: ItemDefinition?, item: ItemDefinition?) {
        if (item == null || template == null) {
            return
        }
        cost = item.cost
        name = item.name
        stackable = 1
        members = item.members
    }

    fun toSingleNote(template: ItemDefinition?, item: ItemDefinition?) {
        if (item == null || template == null) {
            return
        }
        cost = 0
        stackable = item.stackable
        members = item.members
        options = arrayOfNulls(5)
        floorOptions = item.floorOptions
        name = item.name
        extras = item.extras
        primaryFemaleModel = item.primaryFemaleModel
        primaryMaleModel = item.primaryMaleModel
        System.arraycopy(item.options, 0, options, 0, 4)
        options[4] = "Discard"
    }
    companion object {
        val EMPTY = ItemDefinition()
    }
}