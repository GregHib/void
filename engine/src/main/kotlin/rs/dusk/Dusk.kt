package rs.dusk

import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import rs.dusk.cache.cacheConfigModule
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.engine.Engine
import rs.dusk.engine.action.schedulerModule
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.update.updatingTasksModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.data.file.ymlPlayerModule
import rs.dusk.engine.data.playerLoaderModule
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.index.update.visualUpdatingModule
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.world.map.chunk.batchedChunkModule
import rs.dusk.engine.model.world.map.collision.collisionModule
import rs.dusk.engine.model.world.map.location.locationModule
import rs.dusk.engine.model.world.map.location.xteaModule
import rs.dusk.engine.model.world.map.mapModule
import rs.dusk.engine.model.world.map.tileModule
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.script.scriptModule
import rs.dusk.network.codecRepositoryModule
import rs.dusk.network.server.GameServer
import rs.dusk.network.server.World
import rs.dusk.network.server.gameServerFactory
import rs.dusk.utility.get
import rs.dusk.world.entity.player.login.loginQueueModule

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
object Dusk {

    @JvmStatic
    fun main(args: Array<String>) {
	    preload()

        val world = World(1)
        val server = GameServer(world)
        val engine = Engine(get())

        server.run()
        engine.start()
    }

    fun preload() {
        startKoin {
            slf4jLogger()
            modules(
                codecRepositoryModule,
                eventModule,
                cacheModule,
                fileLoaderModule,
                ymlPlayerModule/*, sqlPlayerModule*/,
                entityListModule,
                scriptModule,
                clientSessionModule,
	            gameServerFactory,
                playerLoaderModule,
                xteaModule,
                visualUpdatingModule,
                updatingTasksModule,
                loginQueueModule,
                mapModule,
                tileModule,
                collisionModule,
                cacheDefinitionModule,
                cacheConfigModule,
                locationModule,
                pathFindModule,
                schedulerModule,
                batchedChunkModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }
}