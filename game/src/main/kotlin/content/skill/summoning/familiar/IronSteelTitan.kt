package content.skill.summoning.familiar

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class IronSteelTitan : Script {
    init {
        npcOperate("Interact", "iron_titan_familiar,steel_titan_familiar") {
            npc<Neutral>("Brrrrrr...")
        }
    }
}
