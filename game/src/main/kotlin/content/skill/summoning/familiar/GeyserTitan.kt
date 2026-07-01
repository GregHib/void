package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class GeyserTitan : Script {
    init {
        npcOperate("Interact", "geyser_titan_familiar") {
            npc<Neutral>("Did you know a snail can sleep up to three years?")
            player<Happy>("I wish I could do that. Ah...sleep.")
        }
    }
}
