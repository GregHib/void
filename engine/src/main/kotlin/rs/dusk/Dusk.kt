package rs.dusk

import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import rs.dusk.cache.cacheModule
import rs.dusk.engine.Startup
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.indexAllocatorModule
import rs.dusk.engine.client.verify.clientVerificationModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.data.file.ymlPlayerModule
import rs.dusk.engine.data.playerLoaderModule
import rs.dusk.engine.entity.factory.entityFactoryModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.map.location.xteaModule
import rs.dusk.engine.map.loginTestModule
import rs.dusk.engine.script.scriptModule
import rs.dusk.network.server.GameServer
import rs.dusk.network.server.World
import rs.dusk.utility.func.PreloadableTask
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
object Dusk : PreloadableTask {

    @JvmStatic
    fun main(args: Array<String>) {
        val world = World(1)
        val server = GameServer(world)

        run()
        server.run()
    }

    override fun preload() {
        startKoin {
            slf4jLogger()
            modules(
                eventBusModule,
                cacheModule,
                fileLoaderModule,
                ymlPlayerModule/*, sqlPlayerModule*/,
                entityFactoryModule,
                scriptModule,
                clientSessionModule,
                clientVerificationModule,
                playerLoaderModule,
                indexAllocatorModule,
                loginTestModule,
                xteaModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }

    override fun run() {
        super.run()
        val bus: EventBus = get()
        bus.emit(Startup())
    }
}