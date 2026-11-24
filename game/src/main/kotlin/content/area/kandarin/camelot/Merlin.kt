package content.area.kandarin.camelot

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Merlin : Script {

    init {
        // https://runescape.wiki/w/Transcript:Merlin%27s_Crystal (Post-quest dialogue)
        npcOperate("Talk-to", "merlin_normal") {
            npc<Happy>("Hello. Now that you've freed me from that ghastly crystal, I'm working on a new spell. It'll turn people into hedgehogs!")
            player<Confused>("O... k...")
        }
    }
}
