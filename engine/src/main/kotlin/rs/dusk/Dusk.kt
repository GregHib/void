package rs.dusk

import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.engine.Engine
import rs.dusk.engine.client.login.loginQueueModule
import rs.dusk.engine.client.session.clientSessionModule
import rs.dusk.engine.client.verify.clientVerificationModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.data.file.ymlPlayerModule
import rs.dusk.engine.data.playerLoaderModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventBusModule
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.engine.task.engineTasksModule
import rs.dusk.engine.model.entity.factory.entityFactoryModule
import rs.dusk.engine.model.entity.index.update.visualUpdatingModule
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.world.map.collision.collisionModule
import rs.dusk.engine.model.world.map.location.locationModule
import rs.dusk.engine.model.world.map.location.xteaModule
import rs.dusk.engine.model.world.map.mapModule
import rs.dusk.engine.model.world.map.tileModule
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.path.traversalModule
import rs.dusk.engine.script.scriptModule
import rs.dusk.network.codecRepositoryModule
import rs.dusk.network.server.GameServer
import rs.dusk.network.server.World
import rs.dusk.network.server.gameServerFactory
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
object Dusk : Runnable {

    @JvmStatic
    fun main(args: Array<String>) {
	    preload()
	    
        val world = World(1)
        val server = GameServer(world)
        val engine = Engine()

        run()
        server.run()
        engine.start()
    }

    fun preload() {
        startKoin {
            slf4jLogger()
            modules(
                codecRepositoryModule,
                eventBusModule,
                cacheModule,
                fileLoaderModule,
                ymlPlayerModule/*, sqlPlayerModule*/,
                entityFactoryModule,
                entityListModule,
                scriptModule,
                clientSessionModule,
	            gameServerFactory,
                clientVerificationModule,
                playerLoaderModule,
                xteaModule,
                visualUpdatingModule,
                engineTasksModule,
                loginQueueModule,
                mapModule,
                tileModule,
                collisionModule,
                cacheDefinitionModule,
                locationModule,
                traversalModule,
                pathFindModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }

    override fun run() {
        val bus: EventBus = get()
        bus.emit(Startup())
    }
}