package world.gregs.voidps.engine.entity.definition.data

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.capitalise

/**
 * @param skill skill type
 * @param level required skill level
 * @param xp experience gained in the process
 * @param requires items required
 * @param remove items to be consumed
 * @param add items created or by-products
 * @param ticks till the end of the process
 * @param animation to perform
 * @param graphic to perform
 * @param sound to play
 * @param message to send
 */
data class Making(
    val skill: Skill? = null,
    val level: Int = 1,
    val xp: Double = 0.0,
    val requires: List<Item> = emptyList(),
    val remove: List<Item> = emptyList(),
    val add: List<Item> = emptyList(),
    val ticks: Int = 0,
    val animation: String = "",
    val graphic: String = "",
    val sound: String = "",
    val message: String = ""
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Making(
            skill = if (map.containsKey("skill")) Skill.valueOf((map["skill"] as String).capitalise()) else null,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            level = map["level"] as? Int ?: EMPTY.level,
            requires = listOfItems(map["requires"] as? List<Any>) ?: EMPTY.requires,
            remove = listOfItems(map["remove"] as? List<Any>) ?: EMPTY.remove,
            add = listOfItems(map["add"] as? List<Any>) ?: EMPTY.add,
            ticks = map["ticks"] as? Int ?: EMPTY.ticks,
            animation = map["animation"] as? String ?: EMPTY.animation,
            graphic = map["graphic"] as? String ?: EMPTY.graphic,
            sound = map["sound"] as? String ?: EMPTY.sound,
            message = map["message"] as? String ?: EMPTY.message
        )

        private fun listOfItems(list: List<Any>?): List<Item>? {
            if (list == null) {
                return null
            }
            return list.mapNotNull {
                when (it) {
                    is String -> Item(it, 1)
                    is Map<*, *> -> Item(it["item"] as String, it["amount"] as? Int ?: 1)
                    else -> null
                }
            }
        }

        val EMPTY = Making(Skill.Attack)
    }
}