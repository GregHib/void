package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.GameLoop.Companion.flow
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.schedulerModule
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.ui.detail.interfaceModule
import world.gregs.voidps.engine.client.update.task.npc.*
import world.gregs.voidps.engine.client.update.task.player.*
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.client.update.updatingTasksModule
import world.gregs.voidps.engine.client.variable.variablesModule
import world.gregs.voidps.engine.data.file.fileLoaderModule
import world.gregs.voidps.engine.data.file.jsonPlayerModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.character.npc.npcLoaderModule
import world.gregs.voidps.engine.entity.character.npc.npcSpawnModule
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.login.loginQueueModule
import world.gregs.voidps.engine.entity.character.update.visualUpdatingModule
import world.gregs.voidps.engine.entity.definition.detailsModule
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.entity.obj.customObjectModule
import world.gregs.voidps.engine.entity.obj.objectFactoryModule
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.chunk.batchedChunkModule
import world.gregs.voidps.engine.map.chunk.instanceModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.instance.instancePoolModule
import world.gregs.voidps.engine.map.nav.navModule
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule
import world.gregs.voidps.engine.path.algorithm.lineOfSightModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.NetworkTask
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.scriptModule
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
        val bus: EventBus = get()
        val service = Executors.newSingleThreadScheduledExecutor()

        val tickStages = getTickStages(protocol)
        val engine = GameLoop(service, tickStages)

        bus.emit(Startup)
        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun getTickStages(protocol: Map<Int, Decoder>): List<Runnable> {
        val loginQueue: LoginQueue = get()
        val playerMovement: PlayerMovementTask = get()
        val movementCallback: PlayerMovementCallbackTask = get()
        val npcMovement: NPCMovementTask = get()
        val viewport: ViewportUpdating = get()
        val playerVisuals: PlayerVisualsTask = get()
        val npcVisuals: NPCVisualsTask = get()
        val playerChange: PlayerChangeTask = get()
        val npcChange: NPCChangeTask = get()
        val playerUpdate: PlayerUpdateTask = get()
        val npcUpdate: NPCUpdateTask = get()
        val playerPostUpdate: PlayerPostUpdateTask = get()
        val npcPostUpdate: NPCPostUpdateTask = get()
        val players: Players = get()
        val bus: EventBus = get()
        val net = NetworkTask(players, protocol)
        return listOf(
            net,
            // Connections/Tick Input
            loginQueue,
            // Tick
            Runnable {
                flow.tryEmit(GameLoop.tick)
            },
            Runnable {
                bus.emit(Tick(GameLoop.tick))
            },
            PlayerPathTask(players, get()),
            movementCallback,
            playerMovement,
            npcMovement,
            // Update
            viewport,
            playerVisuals,
            npcVisuals,
            playerChange,
            npcChange,
            playerUpdate,
            npcUpdate,
            playerPostUpdate,
            npcPostUpdate
        )
    }

    private fun preload() {
        startKoin {
            slf4jLogger()
            modules(
                eventModule,
                cacheModule,
                fileLoaderModule,
                jsonPlayerModule,
                entityListModule,
                scriptModule,
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
                interfaceModule,
                variablesModule,
                instanceModule,
                instancePoolModule,
                detailsModule,
                objectFactoryModule,
                lineOfSightModule,
                navModule,
                customObjectModule,
                npcLoaderModule,
                npcSpawnModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }
}