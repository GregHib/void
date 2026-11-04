package content.area.misthalin.ham_hideout

import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class JimmyTheChisel : Script {

    init {
        npcOperate("Talk-to", "jimmy_the_chisel_ham_cave") {
            npc<Disheartened>("Hello mate!")
        }
    }
}
