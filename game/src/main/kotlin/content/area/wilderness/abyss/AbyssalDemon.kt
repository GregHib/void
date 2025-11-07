package content.area.wilderness.abyss

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.type.random

class AbyssalDemon : Script {
    init {
        npcCombatDamage("abyssal_demon") { (source) ->
            if (random.nextInt(6) == 0) {
                val tile = source.tile.toCuboid(1).random(this) ?: return@npcCombatDamage
                tele(tile, clearMode = false)
                anim("abyssal_demon_teleport")
                gfx("abyssal_demon_teleport")
                sound("abyssal_demon_teleport")
            } else if (random.nextInt(3) == 0) {
                val tile = tile.toCuboid(1).random(this) ?: return@npcCombatDamage
                source.tele(tile)
                source.gfx("abyssal_demon_teleport")
            }
        }
    }
}
