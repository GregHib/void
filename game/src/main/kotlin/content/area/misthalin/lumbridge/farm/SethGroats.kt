package content.area.misthalin.lumbridge.farm

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class SethGroats : Script{
    init {
        npcOperate("Talk-to", "seth_groats_lumbridge") {
            //both rs3 and osrs has this same dialogue.
            npc<Neutral>("M'arnin'....going to milk me cowsies!")
        }
    }
}