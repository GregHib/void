import kotlinx.coroutines.runBlocking
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.player.command.Command
import rs.dusk.utility.inject
import java.util.concurrent.atomic.AtomicInteger

val factory: PlayerFactory by inject()

val botCounter = AtomicInteger(0)

Command where { prefix == "bot" } then {
    runBlocking {
        factory.spawn("Bot ${botCounter.getAndIncrement()}").await()
    }
}

runBlocking {
    repeat(2) {
        factory.spawn("Bot ${botCounter.getAndIncrement()}").await()
    }
}