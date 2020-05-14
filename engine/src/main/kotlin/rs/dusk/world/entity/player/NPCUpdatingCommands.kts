import kotlinx.coroutines.runBlocking
import rs.dusk.engine.entity.factory.NPCFactory
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.command.Command
import rs.dusk.engine.model.entity.index.update.visual.*
import rs.dusk.engine.model.entity.index.update.visual.npc.*
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.inject

val factory: NPCFactory by inject()
val npcs: NPCs by inject()

Command where { prefix == "npc" } then {
    println("Npc command")
    runBlocking {
        factory.spawn(1, player.tile.x, player.tile.y + 1, player.tile.plane, Direction.NORTH)
    }
}

Command where { prefix == "npcname" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.name = "Bob"
}

Command where { prefix == "npctfm" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.transform = 50
}

Command where { prefix == "npcturn" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    val parts = content.split(" ")
    npc.turn(parts[0].toInt(), parts[1].toInt())
}

Command where { prefix == "npcmodel" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.setModelChange(intArrayOf(100), intArrayOf(1))
}

Command where { prefix == "npclvl" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.combatLevel = 100
}

Command where { prefix == "npckill" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npcs.remove(npc.tile, npc)
    npcs.remove(npc.tile.chunk, npc)
    npcs.removeAtIndex(npc.index)
}

Command where { prefix == "npcanim" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.setAnimation(content.toInt())// 863
}

Command where { prefix == "npcoverlay" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.setColourOverlay(-2108002746, 10, 100)
}

Command where { prefix == "npcchat" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.forceChat = "Testing"
}

Command where { prefix == "npcgfx" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    val id = content.toInt()
    npc.setGraphic(id)// 93
}

Command where { prefix == "npchit" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.addHit(Hit(10, Hit.Mark.Healed, 255))
}

Command where { prefix == "npctime" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.setTimeBar(true, 0, 60, 1)
}

Command where { prefix == "npcwatch" } then {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.watch(player)
}


Command where { prefix == "npcwalk" } then {
//    GlobalScope.launch {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
//        npc.movementType = MovementType.WALK
//        npc.temporaryMoveType = MovementType.WALK
    val direction = Direction.NORTH
    npc.movement.direction = direction.inverse().value
    npc.movement.delta = Tile(direction.deltaX, direction.deltaY, 0)
    move(npc, player.tile.add(x = direction.deltaX, y = direction.deltaY))
//        delay(600)
//        npc.movementType = MovementType.NONE
//    }
}

Command where { prefix == "npcrun" } then {
//    GlobalScope.launch {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!
    npc.movement.run = true
//        npc.movementType = MovementType.RUN
//        npc.temporaryMoveType = RUN
    val walk = Direction.NORTH
    val run = Direction.NORTH_EAST
    val deltaX = walk.deltaX + run.deltaX
    val deltaY = walk.deltaY + run.deltaY
    npc.movement.direction = 0//getPlayerRunningDirection(deltaX, deltaY)
    npc.movement.delta = Tile(deltaX, deltaY, 0)
    move(npc, npc.tile.add(npc.movement.delta))
//    }
}

Command where { prefix == "npctele" } then {
//    GlobalScope.launch {
    val npc = npcs[player.tile.add(y = 1)]!!.first()!!

    val deltaX = 0
    val deltaY = 3
    npc.movement.delta = Tile(deltaX, deltaY, 0)
    move(npc, npc.tile.add(deltaX, deltaY, 0))
//    }
}

fun move(npc: NPC, tile: Tile) {
    npc.movement.lastTile = npc.tile
    npcs.remove(npc.tile, npc)
    npcs.remove(npc.tile.chunk, npc)
    npc.tile = tile
    npcs[npc.tile] = npc
    npcs[npc.tile.chunk] = npc
}
