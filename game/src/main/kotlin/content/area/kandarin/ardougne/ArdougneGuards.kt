package content.area.kandarin.ardougne

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Script

@Script
class ArdougneGuards {
    init {
        huntPlayer("market_guard_draynor", "guarding") { npc ->
            if (target.hasClock("thieving")) {
                npc.say("Hey, what do you think you are doing!")
                npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
            }
        }
    }
}