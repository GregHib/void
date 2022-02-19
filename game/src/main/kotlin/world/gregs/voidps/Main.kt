package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.map.file.Maps
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.engine.utility.getProperty
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
import java.math.BigInteger

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
        val compress = getProperty("compressMaps") == "true"
        val path = getProperty("mapPath")

        val accountLoader = PlayerAccountLoader(get<ConnectionQueue>(), get(), Contexts.Game)
        val protocol = protocol(get())
        val server = Network(revision, modulus, private, get<ConnectionGatekeeper>(), accountLoader, limit, Contexts.Game, protocol)

        val tickStages = getTickStages(get(), get(), get<ConnectionQueue>(), get(), get(), get(), get())
        val engine = GameLoop(tickStages)

        World.start()
        Maps(get(), get(), get(), get(), get(), get(), get()).load(compress, path)

        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun preload() {
        startKoin {
            slf4jLogger(level = Level.ERROR)
            fileProperties("/game.properties")
            fileProperties("/private.properties")
            modules(getGameModules())
        }
    }
}