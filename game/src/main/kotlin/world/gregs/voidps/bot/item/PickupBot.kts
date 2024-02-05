package world.gregs.voidps.bot.item

import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.floorItemDespawn
import world.gregs.voidps.engine.inject
import kotlin.coroutines.resume

val players: Players by inject()

floorItemDespawn { floorItem ->
    val hash = floorItem.hashCode()
    players.forEach { bot ->
        if (bot.isBot && bot.contains("floor_item_job") && bot["floor_item_hash", -1] == hash) {
            val job: CancellableContinuation<Unit> = bot.remove("floor_item_job") ?: return@floorItemDespawn
            bot.clear("floor_item_hash")
            job.resume(Unit)
        }
    }
}