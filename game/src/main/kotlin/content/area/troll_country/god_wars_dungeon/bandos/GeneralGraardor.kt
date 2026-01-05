package content.area.troll_country.god_wars_dungeon.bandos

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

class GeneralGraardor(
    val npcs: NPCs
) : Script {
    var strongstack: NPC? = null
    var steelwill: NPC? = null
    var grimspike: NPC? = null

    init {
        npcSpawn("general_graardor") {
            if (strongstack == null) {
                strongstack = npcs.add("sergeant_strongstack", Tile(2866, 5358, 2))
            }
            if (steelwill == null) {
                steelwill = npcs.add("sergeant_steelwill", Tile(2872, 5352, 2))
            }
            if (grimspike == null) {
                grimspike = npcs.add("sergeant_grimspike", Tile(2868, 5362, 2))
            }
        }

        npcDespawn("sergeant_*") {
            when (id) {
                "sergeant_strongstack" -> strongstack = null
                "sergeant_steelwill" -> steelwill = null
                "sergeant_grimspike" -> grimspike = null
            }
        }
    }
}
