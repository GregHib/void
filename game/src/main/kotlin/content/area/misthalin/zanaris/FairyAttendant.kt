package content.area.misthalin.zanaris

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile

class FairyAttendant : Script {
    init {
        objectOperate("Use", "zanaris_al_kharid_fairy_ring") {
            val attendant = NPCs.find(tile.regionLevel, "fairy_attendant")
            talkWith(attendant)
            leave()
        }
        npcOperate("Talk-to", "fairy_attendant") {
            leave()
        }
    }

    private suspend fun Player.leave() {
        npc<Neutral>("This fairy ring will take you out of Zanaris. It leads to the place known as Al Kharid in your realm. Once passed you can not return this way.")
        npc<Neutral>("Before leaving make sure that you have fully sampled the delights of our marketplace.")
        choice {
            option<Neutral>("I think I'll stay down here a bit longer.") {
                npc<Neutral>("As you wish.")
            }
            option<Neutral>("Yes, I'm ready to leave.") {
                walkToDelay(Tile(2486, 4471))
                Teleport.teleport(this, Tile(3260, 3171), "fairy")
            }
        }
    }
}
