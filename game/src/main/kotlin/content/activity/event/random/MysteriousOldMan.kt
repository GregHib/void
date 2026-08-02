package content.activity.event.random

import content.activity.event.random.pinball.Pinball
import world.gregs.voidps.engine.Script

class MysteriousOldMan : Script {
    init {
        npcOperate("Talk-to", "mysterious_old_man") { (target) ->
            when (get("random_event", "")) {
                "pinball" -> Pinball.explainPinball(this, target)
                else -> return@npcOperate
            }
        }
    }
}
