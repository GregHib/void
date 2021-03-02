package world.gregs.voidps

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.GameLoop.Companion.flow
import world.gregs.voidps.engine.action.schedulerModule
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.clientSessionModule
import world.gregs.voidps.engine.client.ui.detail.interfaceModule
import world.gregs.voidps.engine.client.update.task.npc.*
import world.gregs.voidps.engine.client.update.task.player.*
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.client.update.updatingTasksModule
import world.gregs.voidps.engine.client.variable.variablesModule
import world.gregs.voidps.engine.data.file.fileLoaderModule
import world.gregs.voidps.engine.data.file.jsonPlayerModule
import world.gregs.voidps.engine.data.playerLoaderModule
import world.gregs.voidps.engine.entity.character.player.Players
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
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.path.algorithm.lineOfSightModule
import world.gregs.voidps.engine.path.pathFindModule
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.handle.*
import world.gregs.voidps.network.GameServer
import world.gregs.voidps.network.codec.game.GameCodec
import world.gregs.voidps.network.codec.game.GameOpcodes
import world.gregs.voidps.network.codec.game.gameCodec
import world.gregs.voidps.network.codec.login.LoginCodec
import world.gregs.voidps.network.codec.service.ServiceOpcodes
import world.gregs.voidps.network.networkCodecs
import world.gregs.voidps.script.scriptModule
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getIntProperty
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.login.loginQueueModule
import world.gregs.voidps.engine.entity.character.player.logout.LogoutQueue
import world.gregs.voidps.engine.entity.character.player.logout.logoutModule
import java.util.concurrent.Executors

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
object Main {

    const val name = "Void"
    private val logger = InlineLogger()

    @JvmStatic
    fun main(args: Array<String>) {
        val startTime = System.currentTimeMillis()
        preload()

        val server = GameServer(getIntProperty("port"))
        val bus: EventBus = get()
        val service = Executors.newSingleThreadScheduledExecutor()

        val tickStages = getTickStages()
        val engine = GameLoop(service, tickStages)

        server.run()
        bus.emit(Startup)
        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
    }

    private fun getTickStages(): List<Runnable> {
        val loginQueue: LoginQueue = get()
        val logoutQueue: LogoutQueue = get()
        val playerMovement: PlayerMovementTask = get()
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
        val playerPath = PlayerPathTask(players, get())
        return listOf(
            // Connections/Tick Input
            loginQueue,
            logoutQueue,
            // Tick
            Runnable {
                flow.tryEmit(GameLoop.tick)
            },
            Runnable {
                bus.emit(Tick(GameLoop.tick))
            },
            playerPath,
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
                clientSessionModule,
                networkCodecs,
                gameCodec,
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
                logoutModule,
                objectFactoryModule,
                lineOfSightModule
            )
            fileProperties("/game.properties")
            fileProperties("/private.properties")
        }
        registerGameHandlers()
        registerLoginHandlers()
    }

    private fun registerLoginHandlers() {
        val login: LoginCodec = get()
        login.registerHandler(ServiceOpcodes.GAME_LOGIN, GameLoginHandler())
        login.registerHandler(ServiceOpcodes.GAME_RECONNECT, GameLoginHandler())
    }

    private fun registerGameHandlers() {
        val game: GameCodec = get()
        game.registerHandler(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandHandler())
        game.registerHandler(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_1, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_2, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_3, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_4, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_5, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.INTEGER_ENTRY, IntEntryHandler())
        game.registerHandler(GameOpcodes.SCREEN_CLOSE, InterfaceClosedHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_1, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_2, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_3, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_4, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_5, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_6, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_7, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_8, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_9, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.INTERFACE_OPTION_10, InterfaceOptionHandler())
        game.registerHandler(GameOpcodes.SWITCH_INTERFACE_COMPONENTS, InterfaceSwitchHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_1, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_2, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_3, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_4, NPCOptionHandler())
        game.registerHandler(GameOpcodes.NPC_OPTION_5, NPCOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_1, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_2, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_3, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_4, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_5, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_1, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_2, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_3, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_4, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_5, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_6, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_7, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_8, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.DONE_LOADING_REGION, RegionLoadedHandler())
        game.registerHandler(GameOpcodes.SCREEN_CHANGE, ScreenChangeHandler())
        game.registerHandler(GameOpcodes.STRING_ENTRY, StringEntryHandler())
        game.registerHandler(GameOpcodes.WALK, WalkMapHandler())
        game.registerHandler(GameOpcodes.MINI_MAP_WALK, WalkMiniMapHandler())
    }
}