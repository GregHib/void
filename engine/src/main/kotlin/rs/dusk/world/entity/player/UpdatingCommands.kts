import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rs.dusk.engine.client.LoginQueue
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Hit
import rs.dusk.engine.entity.model.visual.visuals.*
import rs.dusk.engine.entity.model.visual.visuals.player.*
import rs.dusk.engine.entity.model.visual.visuals.player.MovementType.Companion.TELEPORT
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.Direction
import rs.dusk.engine.model.Tile
import rs.dusk.engine.model.entity.Move
import rs.dusk.engine.model.entity.player.command.Command
import rs.dusk.utility.get
import rs.dusk.utility.inject
import java.util.concurrent.atomic.AtomicInteger

val factory: PlayerFactory by inject()
val players: Players by inject()
val login: LoginQueue by inject()

val botCounter = AtomicInteger(0)

Command where { prefix == "bot" } then {
    runBlocking {
        (0 until 1000).map {
            factory.spawn("Bot ${botCounter.getAndIncrement()}")
        }.forEach {
            it.await()
        }
    }
}

Command where { prefix == "kill" } then {
    val bot = players.indexed.firstOrNull { it != null && it.name.startsWith("Bot") }!!
    players.remove(bot.tile, bot)
    players.remove(bot.tile.chunk, bot)
    GlobalScope.launch {
        delay(600)
        players.removeAtIndex(bot.index)
    }
}

Command where { prefix == "anim" } then {
    player.setAnimation(content.toInt())// 863
}

Command where { prefix == "gfx" } then {
    player.setGraphic(content.toInt())// 93
}

Command where { prefix == "tfm" || prefix == "transform" } then {
    player.transform = content.toInt()
}

runBlocking {
    repeat(2) {
        factory.spawn("Bot ${botCounter.getAndIncrement()}").await()
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
    player.addHit(Hit(10, Hit.Mark.Healed, 255))
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
    player.face(0, -1)
}

Command where { prefix == "hide" } then {
    player.minimapHighlight = !player.minimapHighlight
}

Command where { prefix == "speed" } then {
    player.movementSpeed = !player.movementSpeed
}

Command where { prefix == "tele" || prefix == "tp" } then {
    if (content.contains(",")) {
        val params = content.split(",")
        val plane = params[0].toInt()
        val x = params[1].toInt() shl 6 or params[3].toInt()
        val y = params[2].toInt() shl 6 or params[4].toInt()
        player.movement.delta = Tile(x - player.tile.x, y - player.tile.y, plane - player.tile.plane)
        player.movementType = TELEPORT
        player.movement.lastTile = player.tile
        players.remove(player.tile, player)
        players.remove(player.tile.chunk, player)
        player.tile = Tile(x, y, plane)
        players[player.tile] = player
        players[player.tile.chunk] = player
        val bus: EventBus = get()
        bus.emit(Move(player, player.tile, player.movement.lastTile))
    }
}