package content.area.wilderness.chaos_tunnels

import content.entity.combat.killer
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction

class Mummy : Script {
    init {
        npcDeath("mummy*") {
            transform("${id}_alight")
            World.queue("mummy_ash_breakup_$index", 1) {
                anim("mummy_death")
                killer?.sound("mummy_ash")
                hide = true
                val rotation = when (direction) {
                    Direction.WEST -> 1
                    Direction.NORTH -> 2
                    Direction.EAST -> 3
                    else -> 0
                }
                GameObjects.add("mummy_ash_breakup_${if (id.startsWith("mummy_female")) "female" else "male"}", tile, rotation = rotation, ticks = 4)
            }
        }
    }
}
