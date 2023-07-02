package world.gregs.voidps.bot.item

import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.client.variable.clear
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.remove
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import kotlin.coroutines.resume

val players: Players by inject()

on<Unregistered> { floorItem: FloorItem ->
    val hash = floorItem.hashCode()
    players.forEach { bot ->
        if (bot.isBot && bot.contains("floor_item_job") && bot["floor_item_hash", -1] == hash) {
            val job: CancellableContinuation<Unit> = bot.remove("floor_item_job") ?: return@on
            bot.clear("floor_item_hash")
            job.resume(Unit)
        }
    }
}