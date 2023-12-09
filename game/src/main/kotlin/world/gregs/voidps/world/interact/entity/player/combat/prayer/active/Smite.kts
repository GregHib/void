package world.gregs.voidps.world.interact.entity.player.combat.prayer.active

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying

on<CombatAttack>({ player -> damage > 40 && player.praying("smite") }) { _: Player ->
    target.levels.drain(Skill.Prayer, damage / 40)
}