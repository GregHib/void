package world.gregs.voidps.world.script

import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.schedulerModule
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.clientSessionModule
import world.gregs.voidps.engine.client.ui.detail.interfaceModule
import world.gregs.voidps.engine.client.update.updatingTasksModule
import world.gregs.voidps.engine.client.variable.variablesModule
import world.gregs.voidps.engine.data.file.fileLoaderModule
import world.gregs.voidps.engine.data.file.jsonPlayerModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.character.update.visualUpdatingModule
import world.gregs.voidps.engine.entity.definition.detailsModule
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.entity.obj.objectFactoryModule
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.chunk.batchedChunkModule
import world.gregs.voidps.engine.map.chunk.instanceModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.instance.instancePoolModule
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.engine.task.SyncTask
import world.gregs.voidps.engine.task.TaskExecutor
import world.gregs.voidps.engine.task.executorModule
import world.gregs.voidps.network.codec.game.gameCodec
import world.gregs.voidps.script.scriptModule
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.interact.entity.player.spawn.login.loginQueueModule
import world.gregs.voidps.world.interact.entity.player.spawn.logout.logoutModule
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