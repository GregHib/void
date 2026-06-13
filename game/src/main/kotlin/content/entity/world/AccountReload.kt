package content.entity.world

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.AccountDefinitionsReloader
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Periodically reloads offline account definitions from storage so website
 * password resets and imported accounts take effect without a server restart.
 */
class AccountReload(val reloader: AccountDefinitionsReloader) : Script {

    init {
        worldSpawn {
            if (minutes() > 0) {
                World.timers.start("account_reload")
            }
        }

        worldTimerStart("account_reload") { TimeUnit.MINUTES.toTicks(minutes()) }
        worldTimerTick("account_reload") {
            val minutes = minutes()
            if (minutes <= 0) {
                return@worldTimerTick Timer.CANCEL
            }
            reloader.reload()
            TimeUnit.MINUTES.toTicks(minutes)
        }

        settingsReload {
            if (minutes() > 0) {
                World.timers.start("account_reload", restart = true)
            } else {
                World.timers.stop("account_reload")
            }
        }
    }

    private fun minutes() = Settings["storage.accounts.reloadMinutes", -1]
}
