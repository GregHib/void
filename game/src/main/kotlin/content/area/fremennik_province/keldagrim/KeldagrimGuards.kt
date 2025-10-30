package content.area.fremennik_province.keldagrim

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer

class KeldagrimGuards : Script {
    init {
        huntPlayer("black_guard_keldagrim_market*", "guarding") { npc ->
            if (target.hasClock("thieving")) {
                npc.say("Hey, what do you think you are doing!")
                npc.interactPlayer(target, "Attack")
            }
        }
    }
}
