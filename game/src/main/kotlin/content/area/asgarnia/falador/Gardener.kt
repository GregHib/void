package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.player.Player

class Gardener : Script {
    init {
        val dialogue: suspend Player.(PlayerOnNPCInteract) -> Unit = {
            player<Confused>("Hello.")
            npc<Angry>("Oi'm busy. If tha' wants owt, tha' can go find Wyson. He's ta boss 'round here. And KEEP YER TRAMPIN' FEET ORFF MA' FLOWERS!")
            player<Shock>("Right...")
        }
        npcOperate("Talk-to", "gardener_level_3", dialogue)
        npcOperate("Talk-to", "gardener_level_4", dialogue)
    }
}
