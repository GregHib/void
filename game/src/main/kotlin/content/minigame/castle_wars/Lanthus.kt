package content.minigame.castle_wars

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class Lanthus : Script {
    init {
        npcSpawn("lanthus") {
            softTimers.start("castle_wars_count_down")
        }

        npcOperate("Trade-with", "lanthus") {
            open("castle_wars_shop")
        }

        interfaceOpened("castle_wars_shop") {
            open("castle_wars_shopside")
        }

        interfaceClosed("castle_wars_shop") {
            close("castle_wars_shopside")
        }

        npcTimerStart("castle_wars_count_down") { TimeUnit.SECONDS.toTicks(60) }

        npcTimerTick("castle_wars_count_down") {
            val timer = inc("castle_wars_timer")
            if (timer < 5) {
                say("The next game will start in ${5 - timer} ${"minute".plural(5 - timer)}")
            }
            Timer.CONTINUE
        }
    }
}