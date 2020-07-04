package rs.dusk.world.entity

import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.index.npc.NPCOption
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.walkTo
import rs.dusk.engine.model.entity.item.FloorItemOption
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.obj.ObjectOption
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.path.PathFinder
import rs.dusk.engine.path.PathResult
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
val bus: EventBus by inject()

fun Player.approach(target: Entity, action: (PathResult) -> Unit) = walkTo(target) { result ->
    // TODO improve, what about bankers?
    if(result is PathResult.Failure || !pf.getStrategy(target).reached(tile, size)) {
        println("You can't reach that.")
        return@walkTo
    }

    action(result)
}

ObjectOptionMessage verify { player ->
    val tile = player.tile.copy(x = x, y = y)
    val loc = objects[tile, objectId] ?: return@verify
    val definition = loc.def
    val options = definition.options
    val index = option - 1
    if (options == null || index !in options.indices) {
        //Invalid option
        return@verify
    }
    val option = options[index]
    player.approach(loc) { result ->
        val partial = result is PathResult.Success.Partial
        bus.emit(ObjectOption(player, loc, option, partial))
    }
}

PlayerOptionMessage verify { player ->
    val target = players.getAtIndex(index) ?: return@verify
    // TODO check target has option
    player.approach(target) { result ->
        println("Result $result")
    }
}

NPCOptionMessage verify { player ->
    val npc = npcs.getAtIndex(npcIndex) ?: return@verify
    val options = npc.def.options
    val index = option - 1
    if (index !in options.indices) {
        //Invalid option
        return@verify
    }
    val option = options[index]//TODO is null a valid action? What to do about "*"'s?
    player.approach(npc) { result ->
        val partial = result is PathResult.Success.Partial
        bus.emit(NPCOption(player, npc, option, partial))
    }
}

FloorItemOptionMessage verify { player ->
    val items = items[player.tile] ?: return@verify
    val item = items.firstOrNull { it.id == id } ?: return@verify
    val options = item.def.floorOptions
    val index = option - 1
    if (index !in options.indices) {
        //Invalid option
        return@verify
    }
    val option = options[index]//TODO is null a valid action? What to do about "*"'s?
    player.approach(item) { result ->
        val partial = result is PathResult.Success.Partial
        bus.emit(FloorItemOption(player, item, option, partial))
    }
}