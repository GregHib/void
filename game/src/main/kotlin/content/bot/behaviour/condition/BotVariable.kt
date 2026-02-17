package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotVariable(val id: String, val equals: Any, val default: Any) : Condition(1) {
    override fun keys() = setOf("var:$id")
    override fun events() = setOf("var:$id")
    override fun check(player: Player) = player.variables.get(id, default) == equals
}