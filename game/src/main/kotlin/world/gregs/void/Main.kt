package world.gregs.void

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
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
import world.gregs.void.engine.path.algorithm.lineOfSightModule
import world.gregs.void.engine.path.pathFindModule
import world.gregs.void.engine.task.SyncTask
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.executorModule
import world.gregs.void.handle.*
import world.gregs.void.network.GameServer
import world.gregs.void.network.codec.game.GameCodec
import world.gregs.void.network.codec.game.GameOpcodes
import world.gregs.void.network.codec.game.gameCodec
import world.gregs.void.network.codec.login.LoginCodec
import world.gregs.void.network.networkCodecs
import world.gregs.void.script.scriptModule
import world.gregs.void.utility.get
import world.gregs.void.utility.getIntProperty
import world.gregs.void.utility.getProperty
import world.gregs.void.world.interact.entity.player.spawn.login.loginQueueModule
import world.gregs.void.world.interact.entity.player.spawn.logout.logoutModule
import java.util.concurrent.Executors

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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
        val executor: TaskExecutor = get()
        val service = Executors.newSingleThreadScheduledExecutor()
        val start: SyncTask = get()
        val engine = GameLoop(bus, executor, service)

        server.run()
        engine.setup(start)
        engine.start()
        logger.info { "${getProperty("name")} loaded in ${System.currentTimeMillis() - startTime}ms" }
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
                executorModule,
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
        login.registerHandler(GameOpcodes.GAME_LOGIN, GameLoginHandler())
    }

    private fun registerGameHandlers() {
        val game: GameCodec = get()
        game.registerHandler(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandHandler())
        game.registerHandler(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_1, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_2, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_4, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_5, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_6, FloorItemOptionHandler())
        game.registerHandler(GameOpcodes.ENTER_INTEGER, IntEntryHandler())
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
        game.registerHandler(GameOpcodes.NPC_OPTION_6, NPCOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_1, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_2, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_3, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_4, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_5, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.OBJECT_OPTION_6, ObjectOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_1, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_2, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_3, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_4, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_5, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_6, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_7, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_8, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_9, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.PLAYER_OPTION_10, PlayerOptionHandler())
        game.registerHandler(GameOpcodes.DONE_LOADING_REGION, RegionLoadedHandler())
        game.registerHandler(GameOpcodes.SCREEN_CHANGE, ScreenChangeHandler())
        game.registerHandler(GameOpcodes.STRING_ENTRY, StringEntryHandler())
        game.registerHandler(GameOpcodes.WALK, WalkMapHandler())
        game.registerHandler(GameOpcodes.MINI_MAP_WALK, WalkMiniMapHandler())
    }
}