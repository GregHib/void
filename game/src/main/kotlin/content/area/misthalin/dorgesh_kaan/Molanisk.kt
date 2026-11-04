package content.area.misthalin.dorgesh_kaan

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Molanisks cling to the cave walls of the Dorgesh-Kaan south dungeon until a Slayer bell is rung nearby.
 */
class Molanisk : Script {

    init {
        itemOption("Ring", "slayer_bell") {
            anim("ring_slayer_bell")
            // The bell can be rung by anyone, but only lures molanisks for those who can slay them
            if (!has(Skill.Slayer, 39)) {
                return@itemOption
            }
            val wall = tile.toCuboid(3).flatMap { GameObjects.at(it) }.firstOrNull { it.id == "molanisk_wall" } ?: return@itemOption
            message("The bell re-sounds loudly throughout the cavern.")
            GameObjects.replace(wall, "molanisk_wall_empty", ticks = TimeUnit.SECONDS.toTicks(30))
            val tile = wall.tile.toCuboid(1).random(this) ?: tile
            val molanisk = NPCs.add("molanisk", tile, ticks = TimeUnit.MINUTES.toTicks(5), owner = this)
            molanisk.interactPlayer(this, "Attack")
        }
    }
}
