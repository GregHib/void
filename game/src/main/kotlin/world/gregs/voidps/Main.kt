package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getIntProperty
import world.gregs.voidps.utility.getProperty
import java.math.BigInteger
import java.util.concurrent.Executors

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    lateinit var name: String
    private val logger = InlineLogger()

    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        preload()

        name = getProperty("name")
        val revision = getProperty("revision").toInt()
        val limit = getProperty("loginLimit").toInt()
        val modulus = BigInteger(getProperty("rsaModulus"), 16)
        val private = BigInteger(getProperty("rsaPrivate"), 16)

        val server = Network(protocol, revision, modulus, private, get(), get(), Contexts.Game, limit)
        val service = Executors.newSingleThreadScheduledExecutor()

        val tickStages = getTickStages(get(), get(), get(), get(), get(), get(), get())
        val engine = GameLoop(service, tickStages)

        get<EventHandlerStore>().populate(World)
        World.events.emit(Startup)

        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload() {
        startKoin {
            slf4jLogger()
            modules(getGameModules())
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }
}