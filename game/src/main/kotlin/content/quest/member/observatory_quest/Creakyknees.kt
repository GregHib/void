package content.quest.member.observatory_quest

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Creakyknees : Script {

    init {
        npcSpawn("creakyknees_observatory") {
            softTimers.start("creakyknees_overhead")
        }

        npcTimerStart("creakyknees_overhead") {
            TimeUnit.SECONDS.toTicks(random.nextInt(30, 45))
        }

        npcTimerTick("creakyknees_overhead") {
            when (random.nextInt(2)) {
                0 -> say("Was that a spark?")
                else -> say("Come on! Please light!")
            }
            Timer.CONTINUE
        }

        npcOperate("Talk-to", "creakyknees_observatory") {
            player<Neutral>("Where did you get that lens?")
            npc<Neutral>("From that strange metal thing up on the hill.")
            player<Angry>("You should give that back!")
            npc<Quiz>("Even if it's cracked?")
            player<Neutral>("Ah, well, I suppose it's of no use. But, still.")
        }
    }
}
