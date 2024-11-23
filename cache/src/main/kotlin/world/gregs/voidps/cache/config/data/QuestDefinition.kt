package world.gregs.voidps.cache.config.data

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra

data class QuestDefinition(
    override var id: Int = -1,
    var name: String? = null,
    var listName: String? = null,
    var varps: Array<IntArray>? = null,
    var varbits: Array<IntArray>? = null,
    var subQuest: Int = -1,
    var difficulty: Int = -1,
    var members: Boolean = false,
    var questPoints: Int = -1,
    var pathStart: IntArray? = null,
    var otherPathStart: Int = -1,
    var questRequirements: IntArray? = null,
    var skillRequirements: Array<IntArray>? = null,
    var questPointRequirement: Int = 0,
    var itemSprite: Int = -1,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,
) : Definition, Extra {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestDefinition

        if (id != other.id) return false
        if (name != other.name) return false
        if (listName != other.listName) return false
        if (varps != null) {
            if (other.varps == null) return false
            if (!varps.contentDeepEquals(other.varps)) return false
        } else if (other.varps != null) return false
        if (varbits != null) {
            if (other.varbits == null) return false
            if (!varbits.contentDeepEquals(other.varbits)) return false
        } else if (other.varbits != null) return false
        if (pathStart != null) {
            if (other.pathStart == null) return false
            if (!pathStart.contentEquals(other.pathStart)) return false
        } else if (other.pathStart != null) return false
        if (questRequirements != null) {
            if (other.questRequirements == null) return false
            if (!questRequirements.contentEquals(other.questRequirements)) return false
        } else if (other.questRequirements != null) return false
        if (skillRequirements != null) {
            if (other.skillRequirements == null) return false
            if (!skillRequirements.contentDeepEquals(other.skillRequirements)) return false
        } else if (other.skillRequirements != null) return false
        if (itemSprite != other.itemSprite) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (listName?.hashCode() ?: 0)
        result = 31 * result + (varps?.contentDeepHashCode() ?: 0)
        result = 31 * result + (varbits?.contentDeepHashCode() ?: 0)
        result = 31 * result + (pathStart?.contentHashCode() ?: 0)
        result = 31 * result + (questRequirements?.contentHashCode() ?: 0)
        result = 31 * result + (skillRequirements?.contentDeepHashCode() ?: 0)
        result = 31 * result + itemSprite
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "QuestDefinition(id=$id, name=$name, listName=$listName, varps=${varps?.contentDeepToString()}, varbits=${varbits?.contentDeepToString()}, subQuest=$subQuest, difficulty=$difficulty, members=$members, questPoints=$questPoints, pathStart=${pathStart?.contentToString()}, otherPathStart=$otherPathStart, questRequirements=${questRequirements?.contentToString()}, skillRequirements=${skillRequirements?.contentDeepToString()}, questPointRequirement=$questPointRequirement, itemSprite=$itemSprite, stringId='$stringId', extras=$extras)"
    }


    companion object {
        val EMPTY = QuestDefinition()
    }
}