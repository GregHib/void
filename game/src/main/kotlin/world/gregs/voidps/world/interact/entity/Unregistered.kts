import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.on

on<Unregistered>{ player: Player ->
    player.timers.clearAll()
    player.normalTimers.clearAll()
}

on<Unregistered>{ npc: NPC ->
    npc.timers.clearAll()
}

on<Unregistered>{ floorItem: FloorItem ->
    floorItem.timers.clearAll()
}