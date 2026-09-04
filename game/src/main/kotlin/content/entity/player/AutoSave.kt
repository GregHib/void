package content.entity.player

import content.social.trade.exchange.GrandExchange
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class AutoSave(
    val saveQueue: SaveQueue,
    val exchange: GrandExchange,
) : Script {

    init {
        worldSpawn {
            autoSave()
        }

        worldDespawn {
            runBlocking {
                saveQueue.awaitInFlight()
                saveQueue.direct().join()
                exchange.save()
            }
        }

        settingsReload {
            val minutes = Settings["storage.autoSave.minutes", 0]
            if (minutes <= 0) {
                World.clearQueue("auto_save")
                return@settingsReload
            }
            autoSave()
        }
    }

    fun autoSave() {
        val minutes = Settings["storage.autoSave.minutes", 0]
        if (minutes <= 0) {
            return
        }
        if (World.containsQueue("auto_save")) {
            return
        }
        World.queue("auto_save", TimeUnit.MINUTES.toTicks(minutes)) {
            for (player in Players) {
                saveQueue.save(player)
            }
            exchange.save()
            autoSave()
        }
    }
}
