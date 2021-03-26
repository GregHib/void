import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.effect.Colour
import world.gregs.voidps.engine.entity.character.effect.Transform
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.inject

val npcs: NPCs by inject()
val pf: PathFinder by inject()

on<Command>({ prefix == "npckill" }) { player: Player ->
    npcs.indexed.forEachIndexed { index, npc ->
        npcs.indexed[index] = null
        if (npc != null) {
            npcs.remove(npc.tile, npc)
            npcs.remove(npc.tile.chunk, npc)
        }
    }
}

on<Command>({ prefix == "npcs" }) { player: Player ->
    println("NPCs: ${npcs.indexed.filterNotNull().size}")
}

on<Command>({ prefix == "npctfm" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.effects.add(Transform(50))
}

on<Command>({ prefix == "npcturn" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    val parts = content.split(" ")
    npc.turn(parts[0].toInt(), parts[1].toInt())
}

on<Command>({ prefix == "npcanim" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.setAnimation(content.toInt())// 863
}

on<Command>({ prefix == "npcoverlay" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.effects.add(Colour(-2108002746, 10, 100))
}

on<Command>({ prefix == "npcchat" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.forceChat = "Testing"
}

on<Command>({ prefix == "npcgfx" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    val id = content.toInt()
    npc.setGraphic(id)// 93
}

on<Command>({ prefix == "npchit" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.addHit(Hit(10, Hit.Mark.Healed, 255))
}

on<Command>({ prefix == "npctime" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.setTimeBar(true, 0, 60, 1)
}

on<Command>({ prefix == "npcwatch" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.watch(player)
}

on<Command>({ prefix == "npccrawl" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.crawling = true
    npc.movement.steps.add(Direction.NORTH)
}

on<Command>({ prefix == "npcrun" }) { player: Player ->
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.movement.running = true
    npc.movement.steps.add(Direction.NORTH)
    npc.movement.steps.add(Direction.NORTH)
}