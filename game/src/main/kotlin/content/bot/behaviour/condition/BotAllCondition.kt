package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

/** Every child holds. */
data class BotAllCondition(val children: List<Condition>) : Condition(children.minOfOrNull { it.priority } ?: 0) {
    override fun keys(): Set<String> = children.flatMap { it.keys() }.toSet()
    override fun events(): Set<String> = children.flatMap { it.events() }.toSet()
    override fun check(player: Player): Boolean = children.all { it.check(player) }
}
