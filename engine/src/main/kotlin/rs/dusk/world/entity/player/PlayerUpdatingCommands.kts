import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.player.Players
import rs.dusk.engine.model.entity.character.player.command.Command
import rs.dusk.engine.model.entity.character.update.visual.*
import rs.dusk.engine.model.entity.character.update.visual.player.*
import rs.dusk.engine.model.world.DynamicMaps
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.LargeTraversal
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.get
import rs.dusk.utility.inject
import rs.dusk.world.entity.player.login.LoginQueue

val players: Players by inject()
val login: LoginQueue by inject()
val bus: EventBus by inject()

Command where { prefix == "kill" } then {
    players.indexed.forEachIndexed { index, bot ->
        if (bot != null && bot.name.startsWith("Bot")) {
            players.remove(bot.tile, bot)
            players.remove(bot.tile.chunk, bot)
        }
    }
    GlobalScope.launch {
        delay(600)
        players.indexed.forEachIndexed { index, bot ->
            if (bot != null && bot.name.startsWith("Bot")) {
                players.indexed[index] = null
            }
        }
    }
}

Command where { prefix == "players" } then {
    println("Players: ${players.indexed.filterNotNull().size}")
}

Command where { prefix == "under" } then {
    players[player.tile]?.filterNotNull()?.forEach {
        println("$it - ${player.viewport.players.current.contains(it)}")
    }
}

Command where { prefix == "anim" } then {
    player.setAnimation(content.toInt())// 863
}

Command where { prefix == "gfx" } then {
    val id = content.toInt()
    player.setGraphic(id)// 93
}

Command where { prefix == "tfm" || prefix == "transform" } then {
    val id = content.toInt()
    player.transform = id
    if (id != -1) {
        val decoder = get<NPCDecoder>()
        val definition = decoder.getSafe(id)
        player.emote = definition.renderEmote
        player.size = Size(definition.size, definition.size)
        val collisions: Collisions = get()
        player.movement.traversal = when (definition.size) {
            1 -> SmallTraversal(TraversalType.Land, false, collisions)
            2 -> MediumTraversal(TraversalType.Land, false, collisions)
            else -> LargeTraversal(TraversalType.Land, false, player.size, collisions)
        }
        player.setTransformSounds(
            definition.idleSound,
            definition.crawlSound,
            definition.walkSound,
            definition.runSound,
            definition.soundDistance
        )
    } else {
        player.emote = 1426
        player.size = Size.TILE
        player.movement.traversal = get<SmallTraversal>()
        player.setTransformSounds()
    }
}

Command where { prefix == "overlay" } then {
    player.setColourOverlay(-2108002746, 10, 100)
}

Command where { prefix == "chat" } then {
    player.forceChat = "Testing"
}

Command where { prefix == "move" } then {
    player.setForceMovement(Tile(0, -2), 120, startDelay = 60, direction = Direction.SOUTH)
}

Command where { prefix == "hit" } then {
    player.addHit(
        Hit(
            10,
            Hit.Mark.Healed,
            255
        )
    )
}

Command where { prefix == "time" } then {
    player.setTimeBar(true, 0, 60, 1)
}

Command where { prefix == "watch" } then {
    val bot = players.indexed.firstOrNull { it != null && it.name.startsWith("Bot") }
    if (bot != null) {
        player.watch(bot)
    }
}

Command where { prefix == "mate" } then {
    val bot = players.indexed.firstOrNull { it != null && it.name.startsWith("Bot") }
    bot?.clanmate = true
}

Command where { prefix == "face" } then {
    val parts = content.split(" ")
    player.face(parts[0].toInt(), parts[1].toInt())
}

Command where { prefix == "hide" } then {
    player.minimapHighlight = !player.minimapHighlight
}

Command where { prefix == "run" } then {
    player.movement.running = !player.movement.running
}

Command where { prefix == "test" } then {
    val maps: DynamicMaps = get()
    maps.set(player.tile.chunk, player.tile.chunk, rotation = 2)
}

Command where { prefix == "test2" } then {
    val maps: DynamicMaps = get()
    maps.remove(player.tile.chunk)
}