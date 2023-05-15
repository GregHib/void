package world.gregs.voidps.engine.data.definition.config

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item

/**
 * @param skill skill type
 * @param level required skill level
 * @param xp experience gained in the process
 * @param requires items required
 * @param one one of these items needs to be consumed
 * @param remove items to be consumed
 * @param add items created or by-products
 * @param fail by-products when unsuccessful
 * @param delay ticks before the process starts
 * @param ticks till the end of the process
 * @param type the dialogue type
 * @param chance success rate
 * @param animation to perform
 * @param graphic to perform
 * @param sound to play
 * @param message to send
 * @param failure message
 */
data class ItemOnItemDefinition(
    val skill: Skill? = null,
    val level: Int = 1,
    val xp: Double = 0.0,
    val requires: List<Item> = emptyList(),
    val one: List<Item> = emptyList(),
    val remove: List<Item> = emptyList(),
    val add: List<Item> = emptyList(),
    val fail: List<Item> = emptyList(),
    val delay: Int = -1,
    val ticks: Int = 0,
    val chance: IntRange = Level.SUCCESS,
    val type: String = "make",
    val animation: String = "",
    val graphic: String = "",
    val sound: String = "",
    val message: String = "",
    val failure: String = ""
) {

    companion object {

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = ItemOnItemDefinition(
            skill = if (map.containsKey("skill")) Skill.valueOf((map["skill"] as String).toSentenceCase()) else null,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            level = map["level"] as? Int ?: EMPTY.level,
            requires = listOfItems(map["requires"] as? List<Any>) ?: EMPTY.requires,
            one = listOfItems(map["one"] as? List<Any>) ?: EMPTY.one,
            remove = listOfItems(map["remove"] as? List<Any>) ?: EMPTY.remove,
            add = listOfItems(map["add"] as? List<Any>) ?: EMPTY.add,
            fail = listOfItems(map["fail"] as? List<Any>) ?: EMPTY.fail,
            chance = (map["chance"] as? String)?.toIntRange() ?: EMPTY.chance,
            delay = map["delay"] as? Int ?: EMPTY.delay,
            ticks = map["ticks"] as? Int ?: EMPTY.ticks,
            type = map["type"] as? String ?: EMPTY.type,
            animation = map["animation"] as? String ?: EMPTY.animation,
            graphic = map["graphic"] as? String ?: EMPTY.graphic,
            sound = map["sound"] as? String ?: EMPTY.sound,
            message = map["message"] as? String ?: EMPTY.message,
            failure = map["failure"] as? String ?: EMPTY.failure
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

        val EMPTY = ItemOnItemDefinition(Skill.Attack)
    }
}