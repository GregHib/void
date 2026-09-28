package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Gardener : Script {
    init {
        npcOperate("Talk-to", "gardener_level_3,gardener_level_4") {
            player<Confused>("Hello.")
            npc<Angry>("Oi'm busy. If tha' wants owt, tha' can go find Wyson. He's ta boss 'round here. And KEEP YER TRAMPIN' FEET ORFF MA' FLOWERS!")
            player<Shock>("Right...")
        }
    }
}
