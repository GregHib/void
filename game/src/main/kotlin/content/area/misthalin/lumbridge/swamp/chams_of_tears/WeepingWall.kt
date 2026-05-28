package content.area.misthalin.lumbridge.swamp.chams_of_tears

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random

class WeepingWall : Script {
    val active = mutableListOf<Tile>()
    var nextToMove = 0

    init {
        objectOperate("Collect-from", "weeping_wall") { (target) ->
            val next = when (target.rotation) {
                0 -> Direction.WEST
                1 -> Direction.NORTH
                else -> Direction.SOUTH
            }
            clearAnim()
            walkToDelay(target.tile.add(next))
            arriveDelay()
            delay(1)
            face(target)
            anim("tears_lean_forward_bowl")
            walkTrigger {
                clear("face_entity")
                anim("tears_lean_back_bowl")
                start("movement_delay", 1)
            }
            var i = 0
            while (i++ < 200) {
                val tears = GameObjects.getLayer(target.tile, ObjectLayer.WALL_DECORATION) ?: continue
                if (tears.id.startsWith("blue_tears")) {
                    inc("tears_of_guthix_points")
                    sound("collect_good_tear")
                } else if (tears.id.startsWith("green_tears")) {
                    val amount = dec("tears_of_guthix_points")
                    if (amount != 0) {
                        sound("collect_bad_tear")
                    }
                }
                pause(1)
            }
        }

        worldSpawn {
            setupMinigame()
        }

        settingsReload {
            if (Settings["events.tearsOfGuthix.active", false] && active.isEmpty()) {
                setupMinigame()
            } else if (!Settings["events.tearsOfGuthix.active", false] && active.isNotEmpty()) {
                active.clear()
                nextToMove = 0
            }
        }

        npcSpawn("juna") {
            softTimers.start("tears_of_guthix")
        }

        adminCommand("tog_restart", desc = "Restart tears of guthix") {
            GameObjects.reset(Zone(407, 1189))
            setupMinigame()
        }

        npcTimerStart("tears_of_guthix") { 1 }

        npcTimerTick("tears_of_guthix") {
            if (active.isEmpty()) {
                return@npcTimerTick Timer.CANCEL
            }
            // Select new spot to move it to
            val nextTile = findWall(noTears = false).randomOrNull(random) ?: return@npcTimerTick Timer.CANCEL
            // Remove current tears
            val tile = active[nextToMove]
            val current = GameObjects.getLayer(tile, ObjectLayer.WALL_DECORATION) ?: return@npcTimerTick Timer.CANCEL
            val next = GameObjects.getLayer(nextTile, ObjectLayer.WALL_DECORATION) ?: return@npcTimerTick Timer.CANCEL
            // Swap the objects
            current.replace("${next.id.substringBefore("_tears")}_tears${current.id.substringAfter("_tears")}")
            next.replace("${current.id.substringBefore("_tears")}_tears${next.id.substringAfter("_tears")}")
            active[nextToMove] = nextTile
            nextToMove = (nextToMove + 1).rem(active.size)
            if (nextToMove == 0) 10 else 1
        }
    }

    private fun setupMinigame() {
        if (!Settings["events.tearsOfGuthix.active", false]) {
            return
        }
        active.clear()
        active.addAll(findWall(noTears = true))
        active.shuffle(random)
        val juna = NPCs.findOrNull(Tile(3252, 9517, 1), "juna") ?: return
        if (!juna.softTimers.contains("tears_of_guthix")) {
            juna.softTimers.start("tears_of_guthix")
        }
    }

    private fun findWall(noTears: Boolean): MutableList<Tile> {
        val list = mutableListOf<Tile>()
        val tiles = Tables.tileList("tears_of_guthix.spawns.tiles")
        for (i in tiles.indices) {
            val tile = tiles[i]
            val tears = GameObjects.getLayer(tile, ObjectLayer.WALL_DECORATION) ?: continue
            if (tears.id.startsWith("no_tears") == noTears) {
                continue
            }
            list.add(tile)
        }
        return list
    }
}
