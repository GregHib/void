package content.entity

import content.area.misthalin.Border
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Distance.nearestTo
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Rectangle

class Movement : Script {

    val borders = mutableMapOf<Zone, Rectangle>()

    init {
        playerSpawn {
            if (Players.add(this) && Settings["world.players.collision", false]) {
                add(this)
            }
        }

        npcSpawn {
            if (Settings["world.npcs.collision", false]) {
                add(this)
            }
        }

        npcMoved(handler = NPCs::update)
        moved(handler = Players::update)

        worldSpawn {
            for (border in AreaTypes.tagged("border")) {
                val passage = border.area as Rectangle
                for (zone in passage.toZones()) {
                    borders[zone] = passage
                }
            }
        }

        instruction<Walk> { player ->
            if (player.contains("delay")) {
                return@instruction
            }
            player.closeInterfaces()
            player.clearWatch()
            player.queue.clearWeak()
            player.suspension = null
            if (minimap && !player["a_world_in_microcosm_task", false]) {
                player["a_world_in_microcosm_task"] = true
            }

            val target = player.tile.copy(x, y)
            val border = borders[target.zone]
            if (border != null && (target in border || player.tile in border)) {
                val tile = border.nearestTo(player.tile)
                val endSide = Border.getOppositeSide(border, tile)
                player.walkTo(endSide, noCollision = true, forceWalk = true)
            } else {
                if (player.tile == target && player.mode != EmptyMode && player.mode != PauseMode) {
                    player.mode = EmptyMode
                }
                player.walkTo(target)
            }
        }

        playerDespawn {
            if (Settings["world.players.collision", false]) {
                remove(this)
            }
        }

        npcDeath {
            remove(this)
        }

        npcDespawn {
            if (Settings["world.npcs.collision", false]) {
                remove(this)
            }
        }
    }

    fun add(char: Character) {
        val mask = char.collisionFlag
        val size = char.size
        for (x in char.tile.x until char.tile.x + size) {
            for (y in char.tile.y until char.tile.y + size) {
                Collisions.add(x, y, char.tile.level, mask)
            }
        }
    }

    fun remove(char: Character) {
        val mask = char.collisionFlag
        val size = char.size
        for (x in 0 until size) {
            for (y in 0 until size) {
                Collisions.remove(char.tile.x + x, char.tile.y + y, char.tile.level, mask)
            }
        }
    }
}
