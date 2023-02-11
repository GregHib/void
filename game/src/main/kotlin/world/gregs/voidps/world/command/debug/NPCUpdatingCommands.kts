import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.Hit
import world.gregs.voidps.world.interact.entity.effect.transform

val npcs: NPCs by inject()

on<Command>({ prefix == "npckill" }) { player: Player ->
    npcs.forEach { npc ->
        npcs.remove(npc)
    }
}

on<Command>({ prefix == "npcs" }) { player: Player ->
    player.message("NPCs: ${npcs.size}")
}

on<Command>({ prefix == "npctfm" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.transform(content)
}

on<Command>({ prefix == "npcturn" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    val parts = content.split(" ")
    npc.turn(parts[0].toInt(), parts[1].toInt())
}

on<Command>({ prefix == "npcanim" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.setAnimation(content)// 863
}

on<Command>({ prefix == "npcoverlay" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.colourOverlay(-2108002746, 10, 100)
}

on<Command>({ prefix == "npcchat" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.forceChat = "Testing"
}

on<Command>({ prefix == "npcgfx" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.setGraphic(content)// 93
}

on<Command>({ prefix == "npchit" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.hit(player, 10, Hit.Mark.Healed)
}

on<Command>({ prefix == "npctime" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.setTimeBar(true, 0, 60, 1)
}

on<Command>({ prefix == "npcwatch" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.watch(player)
}

on<Command>({ prefix == "npccrawl" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
//    npc.def["crawl"] = true
//    npc.walkTo(npc.tile)
//    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 1))
}

on<Command>({ prefix == "npcrun" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.running = true
//    npc.walkTo(npc.tile)
//    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 2))
}