package rs.dusk.world.entity.player

import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.path.PathFinder
import rs.dusk.network.rs.codec.game.decode.message.FloorItemOptionMessage
import rs.dusk.network.rs.codec.game.decode.message.NPCOptionMessage
import rs.dusk.network.rs.codec.game.decode.message.ObjectOptionMessage
import rs.dusk.network.rs.codec.game.decode.message.PlayerOptionMessage
import rs.dusk.utility.inject

val pf: PathFinder by inject()
val objects: Objects by inject()
val players: Players by inject()
val npcs: NPCs by inject()
val items: FloorItems by inject()

ObjectOptionMessage verify { player ->
    val objects = objects[x, y, player.tile.plane] ?: return@verify
    val loc = objects.firstOrNull { it != null && it.id == objectId } ?: return@verify
    val definition = loc.def
    val options = definition.options
    val option = option - 1
    if (options == null || option !in options.indices) {
        //Invalid option
        return@verify
    }
    val action = options[option]
    println("Action $action")
    val result = pf.find(player, loc)
    println("Result $result")
}

PlayerOptionMessage verify { player ->
    val target = players.getAtIndex(index) ?: return@verify
    // TODO check target has option
    val result = pf.find(player, target)
    println("Result $result")
}

NPCOptionMessage verify { player ->
    val npc = npcs.getAtIndex(npcIndex) ?: return@verify
    val options = npc.def.options
    val option = option - 1
    if (option !in options.indices) {
        //Invalid option
        return@verify
    }
    val action = options[option]//TODO is null a valid action? What to do about "*"'s?
    println("Action $action")
    val result = pf.find(player, npc)
    println("Result $result")
}

FloorItemOptionMessage verify { player ->
    val items = items[x, y, player.tile.plane] ?: return@verify
    val item = items.firstOrNull { it != null && it.id == id } ?: return@verify
    val options = item.def.floorOptions
    val option = option - 1
    if (option !in options.indices) {
        //Invalid option
        return@verify
    }
    val action = options[option]//TODO is null a valid action? What to do about "*"'s?
    println("Action $action")
    val result = pf.find(player, item)
    println("Result $result")
}