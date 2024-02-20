package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.block

block(Priority.LOWEST) { _ ->
    if (target is Player) {
        target.setAnimation("player_block")
        blocked = true
    }
}

block(Priority.LOWEST) { _ ->
    if (target is NPC) {
        val animation = if (target.race.isNotEmpty()) "${target.race}_hit" else target.def.getOrNull("hit_anim") ?: return@block
        target.setAnimation(animation)
        blocked = true
    }
}