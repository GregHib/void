package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotInterfaceClosed(val id: String) : Condition(1) {
    override fun keys() = setOf("iface:$id")
    override fun events() = setOf("iface:$id")
    override fun check(player: Player) = !player.interfaces.contains(id)
}
