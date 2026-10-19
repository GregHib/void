package content.quest.member.observatory_quest

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Greasycheeks : Script {

    init {
        npcSpawn("greasycheeks_observatory") {
            softTimers.start("greasycheeks_overhead")
        }

        npcTimerStart("greasycheeks_overhead") {
            TimeUnit.SECONDS.toTicks(random.nextInt(15, 30))
        }

        npcTimerTick("greasycheeks_overhead") {
            when (random.nextInt(3)) {
                0 -> say("Cook, cook, cook!")
                1 -> {
                    anim("6832")
                    say("I'm soo hungry!")
                }
                else -> say("This is gonna taste sooo good")
            }
            Timer.CONTINUE
        }

        npcOperate("Talk-to", "greasycheeks_observatory") {
            player<Neutral>("Hello.")
            npc<Neutral>("Shush! I'm concentrating.")
            player<Neutral>("Oh, sorry.")
        }
    }
}
