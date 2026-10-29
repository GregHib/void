package content.area.kharidian_desert.al_kharid.tournament_arena

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Spectators : Script {

    init {
        npcOperate("Talk-to", "spectator_*") {
            npc<Idle>(
                "You know, if you want more information on<br>tournaments you should speak to Khalil. He's the bloke<br>next to the top of the staircase. He runs the<br>tournaments and knows everything about them.",
            )
        }
    }
}
