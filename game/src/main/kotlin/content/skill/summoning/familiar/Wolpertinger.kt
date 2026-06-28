package content.skill.summoning.familiar

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Wolpertinger : Script {
    init {
        npcOperate("Interact", "wolpertinger_familiar") {
            npc<Neutral>("Rawr!")
        }
    }
}
