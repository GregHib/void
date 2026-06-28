package content.skill.summoning.familiar

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class PackYak : Script {
    init {
        npcOperate("Interact", "pack_yak_familiar") {
            npc<Neutral>("Barroobaroooo baaaaaaaaarooo!")
        }
    }
}
