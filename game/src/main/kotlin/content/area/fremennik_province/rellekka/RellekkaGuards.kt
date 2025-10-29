package content.area.fremennik_province.rellekka

import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.event.Script

@Script
class RellekkaGuards {
    init {
        huntPlayer("market_guard_rellekka", "guarding") { npc ->
            if (target.hasClock("thieving")) {
                npc.say("Hey, what do you think you are doing!")
                npc.interactPlayer(target, "Attack")
            }
        }
    }
}
