package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume("wild_pie*") { player ->
    player.levels.boost(Skill.Slayer, 4)
    player.levels.boost(Skill.Ranged, 4)
}