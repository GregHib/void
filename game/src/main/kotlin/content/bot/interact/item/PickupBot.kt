package content.bot.interact.item

import content.bot.isBot
import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Players
import kotlin.coroutines.resume

class PickupBot : Script {

    init {
        floorItemDespawn {
            val hash = hashCode()
            for (bot in Players) {
                if (!bot.isBot || !bot.contains("floor_item_job") || bot["floor_item_hash", -1] != hash) {
                    continue
                }
                val job: CancellableContinuation<Unit> = bot.remove("floor_item_job") ?: return@floorItemDespawn
                bot.clear("floor_item_hash")
                job.resume(Unit)
            }
        }
    }
}
