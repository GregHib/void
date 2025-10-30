package content.area.kandarin.ourania

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PatrolDefinitions
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class ZamorakCrafter : Script {

    val objects: GameObjects by inject()
    val patrols: PatrolDefinitions by inject()

    init {
        npcSpawn("zamorak_crafter*") {
            val patrol = patrols.get(if (id == "zamorak_crafter_start") "zamorak_crafter_to_altar" else "zamorak_crafter_to_bank")
            mode = Patrol(this, patrol.waypoints)
        }

        npcMoved("zamorak_crafter*", ::checkRoute)
    }

    fun checkRoute(npc: NPC, from: Tile) {
        if (npc.tile.equals(3314, 4811)) {
            npc.strongQueue("craft_runes") {
                val altar = objects[Tile(3315, 4810), "ourania_altar"]
                if (altar != null) {
                    npc.face(altar)
                }
                delay(4)
                npc.anim("bind_runes")
                npc.gfx("bind_runes")
                delay(4)
                val patrol = patrols.get("zamorak_crafter_to_bank")
                npc.mode = Patrol(npc, patrol.waypoints)
            }
        } else if (npc.tile.equals(3270, 4856)) {
            npc.strongQueue("return_home") {
                delay(5)
                val patrol = patrols.get("zamorak_crafter_to_altar")
                npc.mode = Patrol(npc, patrol.waypoints)
            }
        }
    }
}
