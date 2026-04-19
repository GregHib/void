package content.bot.behaviour.condition

import content.entity.effect.frozen
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnPlayerInteract
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

data class BotTargetFrozen(val hpMin: Double? = null, val hpMax: Double? = null) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()

    override fun check(player: Player): Boolean {
        val target = (player.mode as? PlayerOnPlayerInteract)?.target ?: return false
        if (!target.frozen) return false
        if (hpMin == null && hpMax == null) return true
        val maxHp = target.levels.getMax(Skill.Constitution)
        if (maxHp <= 0) return false
        val fraction = target.levels.get(Skill.Constitution).toDouble() / maxHp
        if (hpMin != null && fraction < hpMin) return false
        if (hpMax != null && fraction > hpMax) return false
        return true
    }
}
