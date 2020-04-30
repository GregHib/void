import kotlinx.coroutines.runBlocking
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.player.command.Command
import rs.dusk.utility.inject

val factory: PlayerFactory by inject()

Command where { prefix == "bot" } then {
    runBlocking {
        factory.spawn("Bot").await()
        println("Bot spawned.")
    }
}