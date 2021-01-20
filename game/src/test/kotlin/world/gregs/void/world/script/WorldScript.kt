package world.gregs.void.world.script

import org.junit.jupiter.api.BeforeEach
import world.gregs.void.engine.GameLoop
import world.gregs.void.engine.action.schedulerModule
import world.gregs.void.engine.client.cacheConfigModule
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule
import world.gregs.void.engine.client.clientSessionModule
import world.gregs.void.engine.client.ui.detail.interfaceModule
import world.gregs.void.engine.client.update.updatingTasksModule
import world.gregs.void.engine.client.variable.variablesModule
import world.gregs.void.engine.data.file.fileLoaderModule
import world.gregs.void.engine.data.file.jsonPlayerModule
import world.gregs.void.engine.data.playerLoaderModule
import world.gregs.void.engine.entity.character.update.visualUpdatingModule
import world.gregs.void.engine.entity.definition.detailsModule
import world.gregs.void.engine.entity.list.entityListModule
import world.gregs.void.engine.entity.obj.objectFactoryModule
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.eventModule
import world.gregs.void.engine.map.chunk.batchedChunkModule
import world.gregs.void.engine.map.chunk.instanceModule
import world.gregs.void.engine.map.collision.collisionModule
import world.gregs.void.engine.map.instance.instancePoolModule
import world.gregs.void.engine.map.region.regionModule
import world.gregs.void.engine.map.region.xteaModule
import world.gregs.void.engine.path.pathFindModule
import world.gregs.void.engine.task.SyncTask
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.executorModule
import world.gregs.void.network.codec.game.gameCodec
import world.gregs.void.script.scriptModule
import world.gregs.void.utility.get
import world.gregs.void.world.interact.entity.player.spawn.login.loginQueueModule
import world.gregs.void.world.interact.entity.player.spawn.logout.logoutModule
import java.util.concurrent.Executors

abstract class WorldScript : KoinMock() {

    override val modules = listOf(
        eventModule,
        cacheModule,
        fileLoaderModule,
        jsonPlayerModule,
        entityListModule,
        scriptModule,
        clientSessionModule,
        playerLoaderModule,
        xteaModule,
        visualUpdatingModule,
        updatingTasksModule,
        loginQueueModule,
        regionModule,
        collisionModule,
        cacheDefinitionModule,
        cacheConfigModule,
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
        objectFactoryModule,
        gameCodec
    )

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