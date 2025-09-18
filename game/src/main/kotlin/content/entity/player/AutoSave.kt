package content.entity.player

import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.settingsReload
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.worldDespawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

@Script
class AutoSave : Api {

    val players: Players by inject()
    val saveQueue: SaveQueue by inject()
    val exchange: GrandExchange by inject()

    override fun worldSpawn() {
        autoSave()
    }

    init {
        worldDespawn {
            saveQueue.direct(players).join()
            exchange.save()
        }

        settingsReload {
            val minutes = Settings["storage.autoSave.minutes", 0]
            if (World.contains("auto_save") && minutes <= 0) {
                World.clearQueue("auto_save")
            } else if (!World.contains("auto_save") && minutes > 0) {
                autoSave()
            }
        }
    }

    fun autoSave() {
        val minutes = Settings["storage.autoSave.minutes", 0]
        if (minutes <= 0) {
            return
        }
        World.queue("auto_save", TimeUnit.MINUTES.toTicks(minutes)) {
            for (player in players) {
                saveQueue.save(player)
            }
            exchange.save()
            autoSave()
        }
    }
}
