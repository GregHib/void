package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class ZombieProtester : Script {
    init {

        huntPlayer("zombie_protester*", "spotted") {
            if (!tile.within(it.tile, 4)) {
                return@huntPlayer
            }
            softTimers.start("protesting")
            mode = Follow(this, it)
        }

        npcOperate("Talk-to", "zombie_protester*") {
            if (!questCompleted("rum_deal")) {
                return@npcOperate
            }
            npc<Drunk>("Arrrr! Tis yerself! Have a drink!")
            player<Shifty>("Errr...Arrr! I will in a sec, I've just go to, err, plunder some landlubbers...")
            npc<Drunk>("Good huntin'!")
        }

        npcTimerStart("protesting") { TimeUnit.SECONDS.toTicks(30) }

        npcTimerTick("protesting") {
            say(
                when (random.nextInt(8)) {
                    0 -> "Whadda we want? Rum!"
                    1 -> "Give us rum or give us death!"
                    2 -> "Give us yer rum, ye scurvy dog!"
                    3 -> "Yer rum or yer brains!"
                    4 -> "When do we want it? Now!"
                    5 -> "Where d'ye think yer goin?"
                    6 -> "Ye'll never beat us all!"
                    7 -> "United we stagger!"
                    else -> "Rum, rum, we want rum!"
                },
            )
            if (mode is Follow) {
                Timer.CONTINUE
            } else {
                Timer.CANCEL
            }
        }
    }
}
