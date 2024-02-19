package world.gregs.voidps.world.interact.entity

import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.entity.npcDespawn
import world.gregs.voidps.engine.entity.playerDespawn
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject

val accounts: PlayerAccounts by inject()

playerDespawn(priority = Priority.LOWEST) { player ->
    player.queue.logout()
    player.softTimers.stopAll()
    player.timers.stopAll()
    accounts.queueSave(player)
}

npcDespawn(priority = Priority.LOWEST) { npc ->
    npc.softTimers.stopAll()
}