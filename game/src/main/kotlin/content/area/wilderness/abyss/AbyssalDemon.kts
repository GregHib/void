package content.area.wilderness.abyss

import content.entity.combat.hit.npcCombatDamage
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.random

npcCombatDamage("abyssal_demon") { npc ->
    if (random.nextInt(6) == 0) {
        val tile = source.tile.toCuboid(1).random(npc) ?: return@npcCombatDamage
        npc.tele(tile, clearMode = false)
        npc.anim("abyssal_demon_teleport")
        npc.gfx("abyssal_demon_teleport")
        npc.sound("abyssal_demon_teleport")
    } else if (random.nextInt(3) == 0) {
        val tile = npc.tile.toCuboid(1).random(npc) ?: return@npcCombatDamage
        source.tele(tile)
        source.gfx("abyssal_demon_teleport")
    }
}