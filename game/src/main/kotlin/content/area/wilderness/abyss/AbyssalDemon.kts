package content.area.wilderness.abyss

import content.entity.combat.hit.npcCombatDamage
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.random

val areas: AreaDefinitions by inject()

npcCombatDamage("abyssal_demon") { npc ->
    val area = areas.getTagged("abyssal").firstOrNull { it.area.contains(npc.tile) } ?: return@npcCombatDamage
    // TODO anims, gfx, sounds
    if (random.nextInt(6) == 0) {
        val tile = area.area.random(npc) ?: return@npcCombatDamage
        npc.tele(tile)
    } else if (random.nextInt(3) == 0) {
        val tile = area.area.random(source.collision, size) ?: return@npcCombatDamage
        source.tele(tile)
    }
}