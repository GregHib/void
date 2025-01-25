package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import content.entity.player.effect.energy.runEnergy

consume("summer_pie*") { player ->
    player.runEnergy += (player.runEnergy / 100) * 10
    player.levels.boost(Skill.Agility, 5)
}