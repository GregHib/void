package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

data class BotTargetHpPercent(val min: Double? = null, val max: Double? = null) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()

    override fun check(player: Player): Boolean {
        val target = (player.mode as? PlayerOnPlayerInteract)?.target ?: return false
        val maxHp = target.levels.getMax(Skill.Constitution)
        if (maxHp <= 0) return false
        val fraction = target.levels.get(Skill.Constitution).toDouble() / maxHp
        if (min != null && fraction < min) return false
        if (max != null && fraction > max) return false
        return true
    }
}
