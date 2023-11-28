package world.gregs.voidps.world.interact.entity.player.combat.prayer.active

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying

on<CombatHit>({ damage > 40 && source.praying("smite") }) { player: Player ->
    player.levels.drain(Skill.Prayer, damage / 40)
}