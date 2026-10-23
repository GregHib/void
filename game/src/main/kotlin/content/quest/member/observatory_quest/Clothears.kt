package content.quest.member.observatory_quest

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Clothears : Script {

    init {
        npcSpawn("clothears_observatory") {
            softTimers.start("clothears_idle")
        }

        npcTimerStart("clothears_idle") {
            TimeUnit.SECONDS.toTicks(10)
        }

        npcTimerTick("clothears_idle") {
            if (random.nextInt(4) == 0) {
                queue("clothears_wake") {
                    anim("6837")
                    delay(1)
                    say("Huh, What? Where?")
                }
            } else {
                say("Zzzzz...")
            }
            Timer.CONTINUE
        }

        npcOperate("Talk-to", "clothears_observatory") {
            statement("The little goblin appears to be fast asleep, and yet still fishing...")
            player<Neutral>("I think I'd rather not wake him.")
        }
    }
}
