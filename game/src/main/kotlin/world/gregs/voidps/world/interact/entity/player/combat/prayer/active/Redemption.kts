package world.gregs.voidps.world.interact.entity.player.combat.prayer.active

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying

levelChange({ skill == Skill.Constitution && to > 0 && to < it.levels.getMax(skill) / 10 && it.praying("redemption") }) { player: Player ->
    player.levels.set(Skill.Prayer, 0)
    val health = (player.levels.getMax(Skill.Prayer) * 2.5).toInt()
    player.levels.restore(Skill.Constitution, health)
    player.setGraphic("redemption")
}