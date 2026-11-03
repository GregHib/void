package content.area.kharidian_desert

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Eblis : Script {

    init {
        // https://runescape.wiki/w/Transcript:Eblis
        npcOperate("Talk-to", "eblis_kharidian_desert_2") {
            player<Neutral>("Hello again.")
            npc<Neutral>("Greetings. I await the return of my Lord Azzanadra and of our god. I do not know why, but I feel this spot has some significance...")
        }
    }
}
