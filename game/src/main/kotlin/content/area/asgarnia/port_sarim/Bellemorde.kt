package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.pet.hasCatspeakAmulet
import world.gregs.voidps.engine.Script

class Bellemorde : Script {

    init {
        npcOperate("Talk-to", "bellemorde_port_sarim") {
            player<Neutral>("Hello puss.")
            if (!hasCatspeakAmulet()) {
                npc<Angry>("Hiss!")
                return@npcOperate
            }
            npc<Neutral>("Hello human.")
            player<Neutral>("Would you like a fish?")
            npc<Neutral>("I don't want your fish. I hunt and eat what I need by myself.")
        }
    }
}
