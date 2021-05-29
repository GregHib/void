package world.gregs.voidps.world.script

import org.junit.jupiter.api.BeforeEach
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.schedulerModule
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.variable.variablesModule
import world.gregs.voidps.engine.data.file.fileLoaderModule
import world.gregs.voidps.engine.data.file.jsonPlayerModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.login.loginQueueModule
import world.gregs.voidps.engine.entity.definition.definitionsModule
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.entity.obj.objectFactoryModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.chunk.batchedChunkModule
import world.gregs.voidps.engine.map.chunk.instanceModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.instance.instancePoolModule
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.script.scriptModule
import java.util.concurrent.Executors

abstract class WorldScript : KoinMock() {

    override val modules = listOf(
        eventModule,
        cacheModule,
        fileLoaderModule,
        jsonPlayerModule,
        entityListModule,
        scriptModule,
        playerLoaderModule,
        xteaModule,
        loginQueueModule,
        regionModule,
        collisionModule,
        cacheDefinitionModule,
        cacheConfigModule,
        pathFindModule,
        schedulerModule,
        batchedChunkModule,
        variablesModule,
        instanceModule,
        instancePoolModule,
        definitionsModule,
        objectFactoryModule
    )

    override val propertyPaths = listOf("/game.properties", "/private.properties")

    private lateinit var engine: GameLoop

    fun tick() {
        engine.run()
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
        val service = Executors.newSingleThreadScheduledExecutor()
        engine = GameLoop(service, listOf())
        World.events.emit(Startup)
    }
}