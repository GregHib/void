package world.gregs.voidps.world.interact.entity

import world.gregs.voidps.engine.data.PlayerAccounts
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject

val accounts: PlayerAccounts by inject()

on<Unregistered>(priority = Priority.LOWEST) { player: Player ->
    player.queue.logout()
    player.softTimers.clearAll()
    player.timers.clearAll()
    accounts.queueSave(player)
}

on<Unregistered>(priority = Priority.LOWEST) { npc: NPC ->
    npc.softTimers.clearAll()
}