package world.gregs.voidps.world.command.debug

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.command
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.Hitsplat
import world.gregs.voidps.world.interact.entity.effect.transform

val npcs: NPCs by inject()

command({ prefix == "npckill" }) { player: Player ->
    npcs.forEach { npc ->
        npcs.remove(npc)
    }
}

command({ prefix == "npcs" }) { player: Player ->
    player.message("NPCs: ${npcs.size}")
}

command({ prefix == "npctfm" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.transform = content
}

command({ prefix == "npcturn" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    val parts = content.split(" ")
    npc.turn(parts[0].toInt(), parts[1].toInt())
}

command({ prefix == "npcanim" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.setAnimation(content)// 863
}

command({ prefix == "npcoverlay" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.colourOverlay(-2108002746, 10, 100)
}

command({ prefix == "npcchat" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.forceChat = "Testing"
}

command({ prefix == "npcgfx" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.setGraphic(content)// 93
}

command({ prefix == "npchit" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.hit(player, 10, Hitsplat.Mark.Healed)
}

command({ prefix == "npctime" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.setTimeBar(true, 0, 60, 1)
}

command({ prefix == "npcwatch" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.watch(player)
}

command({ prefix == "npccrawl" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
//    npc.def["crawl"] = true
//    npc.walkTo(npc.tile)
//    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 1))
}

command({ prefix == "npcrun" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)].first()
    npc.running = true
//    npc.walkTo(npc.tile)
//    npc.movement.steps.add(Tile(npc.tile.x, npc.tile.y + 2))
}