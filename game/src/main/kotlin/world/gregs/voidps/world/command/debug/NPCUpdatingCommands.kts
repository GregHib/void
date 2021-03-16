import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.effect.Colour
import world.gregs.voidps.engine.entity.character.effect.Transform
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.engine.entity.character.update.visual.*
import world.gregs.voidps.engine.entity.character.update.visual.npc.turn
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.utility.inject

val npcs: NPCs by inject()
val pf: PathFinder by inject()

Command where { prefix == "npckill" } then {
    npcs.indexed.forEachIndexed { index, npc ->
        npcs.indexed[index] = null
        if (npc != null) {
            npcs.remove(npc.tile, npc)
            npcs.remove(npc.tile.chunk, npc)
        }
    }
}

Command where { prefix == "npcs" } then {
    println("NPCs: ${npcs.indexed.filterNotNull().size}")
}

Command where { prefix == "npctfm" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.effects.add(Transform(50))
}

Command where { prefix == "npcturn" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    val parts = content.split(" ")
    npc.turn(parts[0].toInt(), parts[1].toInt())
}

Command where { prefix == "npcanim" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.setAnimation(content.toInt())// 863
}

Command where { prefix == "npcoverlay" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.effects.add(Colour(-2108002746, 10, 100))
}

Command where { prefix == "npcchat" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.forceChat = "Testing"
}

Command where { prefix == "npcgfx" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    val id = content.toInt()
    npc.setGraphic(id)// 93
}

Command where { prefix == "npchit" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.addHit(Hit(10, Hit.Mark.Healed, 255))
}

Command where { prefix == "npctime" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.setTimeBar(true, 0, 60, 1)
}

Command where { prefix == "npcwatch" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.watch(player)
}

Command where { prefix == "npccrawl" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.crawling = true
    npc.movement.steps.add(Direction.NORTH)
}

Command where { prefix == "npcrun" } then {
    val npc = npcs[player.tile.addY(1)]!!.first()!!
    npc.movement.running = true
    npc.movement.steps.add(Direction.NORTH)
    npc.movement.steps.add(Direction.NORTH)
}