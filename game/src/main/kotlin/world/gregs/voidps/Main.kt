package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import world.gregs.voidps.bot.taskModule
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.GameLoop.Companion.flow
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.schedulerModule
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.update.task.npc.*
import world.gregs.voidps.engine.client.update.task.player.*
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.client.update.updatingTasksModule
import world.gregs.voidps.engine.client.variable.variablesModule
import world.gregs.voidps.engine.data.file.fileLoaderModule
import world.gregs.voidps.engine.data.file.jsonPlayerModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.npcSpawnModule
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.login.loginQueueModule
import world.gregs.voidps.engine.entity.character.update.visualUpdatingModule
import world.gregs.voidps.engine.entity.definition.definitionsModule
import world.gregs.voidps.engine.entity.item.floorItemSpawnModule
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.entity.obj.customObjectModule
import world.gregs.voidps.engine.entity.obj.objectFactoryModule
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.area.areasModule
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.engine.map.chunk.batchedChunkModule
import world.gregs.voidps.engine.map.chunk.instanceModule
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.instance.instancePoolModule
import world.gregs.voidps.engine.map.nav.navModule
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule
import world.gregs.voidps.engine.path.algorithm.lineOfSightModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.network.InstructionHandler
import world.gregs.voidps.network.InstructionTask
import world.gregs.voidps.network.Network
import world.gregs.voidps.network.protocol
import world.gregs.voidps.script.scriptModule
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getIntProperty
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.world.interact.entity.player.music.musicModule
import world.gregs.voidps.world.interact.world.stairsModule
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
        val service = Executors.newSingleThreadScheduledExecutor()

        val tickStages = getTickStages()
        val engine = GameLoop(service, tickStages)

        get<EventHandlerStore>().populate(World)
        World.events.emit(Startup)

        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
        server.start(getIntProperty("port"))
    }

    private fun getTickStages(): List<Runnable> {
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
        val batcher: ChunkBatcher = get()
        val scheduler: Scheduler = get()
        val net = InstructionTask(players, InstructionHandler())
        return listOf(
            net,
            // Connections/Tick Input
            loginQueue,
            // Tick
            Runnable {
                runBlocking {
                    scheduler.tick()
                }
                flow.tryEmit(GameLoop.tick)
            },
            Runnable {
                World.events.emit(Tick(GameLoop.tick))
            },
            PlayerPathTask(players, get()),
            movementCallback,
            playerMovement,
            npcMovement,
            // Update
            batcher,
            viewport,
            playerVisuals,
            npcVisuals,
            playerChange,
            npcChange,
            playerUpdate,
            npcUpdate,
            playerPostUpdate,
            npcPostUpdate,
            Runnable {
                World.events.emit(AiTick)
            }
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
                variablesModule,
                instanceModule,
                instancePoolModule,
                definitionsModule,
                objectFactoryModule,
                lineOfSightModule,
                navModule,
                customObjectModule,
                npcSpawnModule,
                stairsModule,
                floorItemSpawnModule,
                musicModule,
                areasModule,
                taskModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
    }
}