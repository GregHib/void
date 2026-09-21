package content.area.asgarnia.dwarven_mines

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class HammerspikeStoutbeard : Script {

    init {
        npcOperate("Talk-to", "hammerspike_stoutbeard") { (target) ->
            npc<Angry>("You looking at me? I don't see nobody else here!")
        }
    }
}
