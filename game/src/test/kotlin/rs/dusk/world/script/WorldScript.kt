package rs.dusk.world.script

import org.junit.jupiter.api.BeforeEach
import rs.dusk.engine.GameLoop
import rs.dusk.engine.action.schedulerModule
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.client.clientSessionModule
import rs.dusk.engine.client.ui.detail.interfaceModule
import rs.dusk.engine.client.update.updatingTasksModule
import rs.dusk.engine.client.variable.variablesModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.data.file.ymlPlayerModule
import rs.dusk.engine.data.playerLoaderModule
import rs.dusk.engine.entity.character.update.visualUpdatingModule
import rs.dusk.engine.entity.definition.detailsModule
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.obj.objectFactoryModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.map.chunk.batchedChunkModule
import rs.dusk.engine.map.chunk.instanceModule
import rs.dusk.engine.map.collision.collisionModule
import rs.dusk.engine.map.instance.instancePoolModule
import rs.dusk.engine.map.region.obj.objectMapModule
import rs.dusk.engine.map.region.obj.xteaModule
import rs.dusk.engine.map.region.regionModule
import rs.dusk.engine.map.region.tile.tileModule
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.task.SyncTask
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.executorModule
import rs.dusk.network.codecRepositoryModule
import rs.dusk.network.server.gameServerFactory
import rs.dusk.script.scriptModule
import rs.dusk.utility.get
import rs.dusk.world.interact.entity.player.spawn.login.loginQueueModule
import rs.dusk.world.interact.entity.player.spawn.logout.logoutModule
import java.util.concurrent.Executors

abstract class WorldScript : KoinMock() {

    override val modules = listOf(codecRepositoryModule,
        eventModule,
        cacheModule,
        fileLoaderModule,
        ymlPlayerModule,
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
        instancePoolModule,
        detailsModule,
        logoutModule,
        objectFactoryModule)

    override val propertyPaths = listOf("/game.properties", "/private.properties")

    private lateinit var executor: TaskExecutor

    fun tick() {
        executor.run()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> loadScript(name: String): T {
        val clazz = this::class.java
        val scriptPackage = "${clazz.packageName}.$name"
        return Class.forName(scriptPackage).constructors.first().newInstance(emptyArray<String>()) as T
    }

    // TODO player setup & teardown

    @BeforeEach
    open fun setup() {
        val bus: EventBus = get()
        executor = get()
        val service = Executors.newSingleThreadScheduledExecutor()
        val start: SyncTask = get()
        val engine = GameLoop(bus, executor, service)

        engine.setup(start)
    }
}