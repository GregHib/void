package rs.dusk

import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import rs.dusk.cache.cacheConfigModule
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.engine.GameLoop
import rs.dusk.engine.action.schedulerModule
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.ui.interfaceModule
import rs.dusk.engine.client.update.updatingTasksModule
import rs.dusk.engine.client.variable.variablesModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.data.file.ymlPlayerModule
import rs.dusk.engine.data.playerLoaderModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.model.entity.character.update.visualUpdatingModule
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.map.chunk.batchedChunkModule
import rs.dusk.engine.model.map.chunk.instanceModule
import rs.dusk.engine.model.map.collision.collisionModule
import rs.dusk.engine.model.map.instance.instancePoolModule
import rs.dusk.engine.model.map.region.obj.objectMapModule
import rs.dusk.engine.model.map.region.obj.xteaModule
import rs.dusk.engine.model.map.region.regionModule
import rs.dusk.engine.model.map.region.tile.tileModule
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.task.StartTask
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.executorModule
import rs.dusk.network.codecRepositoryModule
import rs.dusk.network.server.GameServer
import rs.dusk.network.server.World
import rs.dusk.network.server.gameServerFactory
import rs.dusk.script.scriptModule
import rs.dusk.utility.get
import rs.dusk.world.interact.player.spawn.login.loginQueueModule
import java.util.concurrent.Executors

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

        val bus: EventBus = get()
        val executor: TaskExecutor = get()
        val service = Executors.newSingleThreadScheduledExecutor()
        val start: StartTask = get()
        val engine = GameLoop(bus, executor, service)

        server.run()
        engine.setup(start)
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
                regionModule,
                tileModule,
                collisionModule,
                cacheDefinitionModule,
                cacheConfigModule,
                objectMapModule,
                pathFindModule,
                schedulerModule,
                batchedChunkModule,
                executorModule,
                interfaceModule,
                variablesModule,
                instanceModule,
                instancePoolModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }
}