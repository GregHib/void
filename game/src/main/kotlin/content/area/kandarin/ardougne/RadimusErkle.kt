package content.area.kandarin.ardougne

import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script

class RadimusErkle : Script {

    init {
        // https://oldschool.runescape.wiki/w/Transcript:Radimus_Erkle (Standard dialogue), matches 2009scape RadimusErkleDialogue
        npcOperate("Talk-to", "radimus_erkle_3") {
            statement("Radimus looks busy...")
            npc<Sad>("Excuse me a moment won't you. Do feel free to explore the rest of the building.")
        }
    }
}
