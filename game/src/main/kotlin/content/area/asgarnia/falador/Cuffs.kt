package content.area.asgarnia.falador

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Cuffs : Script {

    init {
        npcOperate("Talk-to", "cuffs") {
            player<Quiz>("Hello. Nice day for a walk, isn't it?")
            npc<Shifty>("A walk? Oh yes, that's what we're doing. We're just out here for a walk.")
            player<Shifty>("I'm glad you're just out here for a walk. A more suspicious person would think you were waiting here to attack weak-looking travellers.")
            npc<Shifty>("Nope, we'd never do anything like that. Just a band of innocent walkers, that's us.")
            player<Shifty>("Alright, have a nice walk.")
        }
    }
}
