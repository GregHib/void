package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotAnyCondition(val children: List<Condition>) : Condition(children.minOfOrNull { it.priority } ?: 0) {
    override fun keys(): Set<String> = children.flatMap { it.keys() }.toSet()
    override fun events(): Set<String> = children.flatMap { it.events() }.toSet()
    override fun check(player: Player): Boolean = children.any { it.check(player) }
}
