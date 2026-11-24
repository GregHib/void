package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class NedLadyLumbridge : Script {

    init {
        // https://runescape.wiki/w/Transcript:Dragon_Slayer (Post-quest, Ned on Lady Lumbridge)
        npcOperate("Talk-to", "ned_port_sarim_2") {
            npc<Neutral>("Ah, if you're wanting to go to Crandor again, can you find some other way? I think that voyage is cursed!")
            player<Neutral>("Now you mention it, I found a secret door that led into a cave inside Karamja volcano.")
            npc<Happy>("That's a relief. You can go and use that instead.")
        }
    }
}
