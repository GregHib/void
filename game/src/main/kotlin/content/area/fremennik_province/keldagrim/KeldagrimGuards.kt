package content.area.fremennik_province.keldagrim

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Script

@Script
class KeldagrimGuards {
    init {
        huntPlayer("black_guard_keldagrim_market*", "guarding") { npc ->
            if (target.hasClock("thieving")) {
                npc.say("Hey, what do you think you are doing!")
                npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
            }
        }
    }
}
