package content.quest.member.observatory_quest

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class Smellytoes : Script {

    init {
        npcSpawn("smellytoes_observatory") {
            softTimers.start("smellytoes_overhead")
        }

        npcTimerStart("smellytoes_overhead") {
            TimeUnit.SECONDS.toTicks(random.nextInt(15, 30))
        }

        npcTimerTick("smellytoes_overhead") {
            when (random.nextInt(2)) {
                0 -> say("Doh ray meeee laa doh faaa!")
                else -> {
                    anim("6830")
                    say("La la la! I'm feeling good!")
                }
            }
            Timer.CONTINUE
        }

        npcOperate("Talk-to", "smellytoes_observatory") {
            player<Neutral>("Hi there.")
            npc<Drunk>("Hey, ids me matesh!")
            player<Confused>("Sorry, have we met?")
            npc<Drunk>("Yeah! you wazsh wiv me in dat pub overy by hill!!")
            player<Confused>("I have no idea what you're going on about.")
            npc<Drunk>("Glad yeeash remembers.")
        }
    }
}
