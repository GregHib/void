package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume("fish_pie*") { player ->
    player.levels.boost(Skill.Fishing, 3)
}