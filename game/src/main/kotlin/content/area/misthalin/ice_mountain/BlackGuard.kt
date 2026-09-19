package content.area.misthalin.ice_mountain

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class BlackGuard : Script {

    init {
        npcOperate("Talk-to", "black_guard*") {
            when (random.nextInt(5)) {
                0 -> npc<Neutral>("Obey the law!")
                1 -> npc<Neutral>("Stay out of trouble!")
                2 -> npc<Neutral>("Out of the way, human!")
                3 -> npc<Neutral>("I'm keeping an eye on you!")
                4 -> statement("The guard ignores you.")
            }
        }
    }
}
