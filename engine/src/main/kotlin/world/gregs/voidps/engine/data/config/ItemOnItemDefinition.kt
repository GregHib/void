package world.gregs.voidps.engine.data.config

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
 * @param question override for make-x question
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
    val delay: Int = 1,
    val ticks: Int = 0,
    val chance: IntRange = Level.SUCCESS,
    val type: String = "make",
    val animation: String = "",
    val graphic: String = "",
    val sound: String = "",
    val message: String = "",
    val failure: String = "",
    val question: String = "How many would you like to $type?"
) {

    companion object {

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = ItemOnItemDefinition(
            skill = map["skill"] as? Skill,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            level = map["level"] as? Int ?: EMPTY.level,
            requires = map["requires"] as? List<Item> ?: EMPTY.requires,
            one = map["one"] as? List<Item> ?: EMPTY.one,
            remove = map["remove"] as? List<Item> ?: EMPTY.remove,
            add = map["add"] as? List<Item> ?: EMPTY.add,
            fail = map["fail"] as? List<Item> ?: EMPTY.fail,
            chance = map["chance"] as? IntRange ?: EMPTY.chance,
            delay = map["delay"] as? Int ?: EMPTY.delay,
            ticks = map["ticks"] as? Int ?: EMPTY.ticks,
            type = map["type"] as? String ?: EMPTY.type,
            animation = map["animation"] as? String ?: EMPTY.animation,
            graphic = map["graphic"] as? String ?: EMPTY.graphic,
            sound = map["sound"] as? String ?: EMPTY.sound,
            message = map["message"] as? String ?: EMPTY.message,
            failure = map["failure"] as? String ?: EMPTY.failure,
            question = map["question"] as? String ?: "How many would you like to ${map["type"] as? String ?: EMPTY.type}?"
        )

        val EMPTY = ItemOnItemDefinition(Skill.Attack)
    }
}