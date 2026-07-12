package content.skill.summoning.familiar

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class AbyssalTitan : Script {
    init {
        npcOperate("Interact", "abyssal_titan_familiar") {
            npc<Neutral>("Scruunt, scraaan.....")
        }
    }
}
