package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotVariableIn(
    val id: String,
    val default: Int,
    val min: Int? = null,
    val max: Int? = null,
) : Condition(1) {
    override fun keys() = setOf("var:$id")
    override fun events() = setOf("var:$id")
    override fun check(player: Player): Boolean {
        val value = player.variables.get(id, default)
        return inRange(value, min, max)
    }
}
