package content.bot.behaviour.condition

import content.bot.bot
import content.bot.isBot
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

data class BotTargetArmorType(val equals: Set<String>) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        val target = currentTarget(player) ?: return "none" in equals
        val body = target.equipped(EquipSlot.Chest)
        val material = body.def.getOrNull<String>("material") ?: "none"
        return material in equals
    }

    private fun currentTarget(player: Player): Player? {
        val mode = player.mode
        if (mode is PlayerOnPlayerInteract) return mode.target
        if (!player.isBot) return null
        return player.bot.combatContext?.incomingAttacker
    }
}
