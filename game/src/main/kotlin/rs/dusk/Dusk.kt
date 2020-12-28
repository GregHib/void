package rs.dusk

import org.koin.core.context.startKoin
import org.koin.logger.slf4jLogger
import rs.dusk.engine.GameLoop
import rs.dusk.engine.TimedLoader
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
import rs.dusk.engine.map.region.regionModule
import rs.dusk.engine.map.region.xteaModule
import rs.dusk.engine.path.pathFindModule
import rs.dusk.engine.storage.databaseModule
import rs.dusk.engine.task.SyncTask
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.executorModule
import rs.dusk.handle.*
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.game.GameOpcodes
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.service.ServiceCodec
import rs.dusk.network.rs.codec.update.UpdateCodec
import rs.dusk.network.server.GameServer
import rs.dusk.network.server.gameServerFactory
import rs.dusk.script.scriptModule
import rs.dusk.utility.get
import rs.dusk.utility.getIntProperty
import rs.dusk.world.interact.entity.player.spawn.login.loginQueueModule
import rs.dusk.world.interact.entity.player.spawn.logout.DisconnectEvent
import rs.dusk.world.interact.entity.player.spawn.logout.logoutModule
import java.util.concurrent.Executors

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
object Dusk {
	
	const val name = "Dusk"
	
	@JvmStatic
	fun main(args : Array<String>) {
		preload()
		
		val disconnect = DisconnectEvent()
		val server = GameServer(getIntProperty("world"), getIntProperty("port"), disconnect)
		
		val bus : EventBus = get()
		val executor : TaskExecutor = get()
		val service = Executors.newSingleThreadScheduledExecutor()
		val start : SyncTask = get()
		val engine = GameLoop(bus, executor, service)

		object : TimedLoader<Unit>("Game codec") {
			override fun load(args: Array<out Any?>) {
				GameCodec.register()
				registerGameHandlers()
				count = GameCodec.decoders.size + GameCodec.encoders.size
			}
		}.run()
		object : TimedLoader<Unit>("Login codec") {
			override fun load(args: Array<out Any?>) {
				LoginCodec.register()
				registerLoginHandlers()
				count = LoginCodec.decoders.size + LoginCodec.encoders.size
			}
		}.run()
		object : TimedLoader<Unit>("Service codec") {
			override fun load(args: Array<out Any?>) {
				ServiceCodec.register()
				count = ServiceCodec.decoders.size + ServiceCodec.encoders.size
			}
		}.run()
		object : TimedLoader<Unit>("Update codec") {
			override fun load(args: Array<out Any?>) {
				UpdateCodec.register()
				count = UpdateCodec.decoders.size + UpdateCodec.encoders.size
			}
		}.run()
		server.run()
		engine.setup(start)
		engine.start()
	}
	
	fun preload() {
		startKoin {
			slf4jLogger()
			modules(
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
				databaseModule,
				logoutModule,
				objectFactoryModule
			)
			fileProperties("/game.properties")
			fileProperties("/private.properties")
		}
	}

	private fun registerLoginHandlers() {
		LoginCodec.registerHandler(GameOpcodes.GAME_LOGIN, GameLoginMessageHandler())
	}

	private fun registerGameHandlers() {
		GameCodec.registerHandler(GameOpcodes.CONSOLE_COMMAND, ConsoleCommandMessageHandler())
		GameCodec.registerHandler(GameOpcodes.DIALOGUE_CONTINUE, DialogueContinueMessageHandler())
		GameCodec.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_1, FloorItemOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_2, FloorItemOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_4, FloorItemOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_5, FloorItemOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.FLOOR_ITEM_OPTION_6, FloorItemOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.ENTER_INTEGER, IntEntryMessageHandler())
		GameCodec.registerHandler(GameOpcodes.SCREEN_CLOSE, InterfaceClosedMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_1,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_2,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_3,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_4,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_5,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_6,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_7,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_8,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_9,InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.INTERFACE_OPTION_10, InterfaceOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.SWITCH_INTERFACE_COMPONENTS, InterfaceSwitchMessageHandler())
		GameCodec.registerHandler(GameOpcodes.NPC_OPTION_1, NPCOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.NPC_OPTION_2, NPCOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.NPC_OPTION_3, NPCOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.NPC_OPTION_4, NPCOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.NPC_OPTION_5, NPCOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.NPC_OPTION_6, NPCOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.OBJECT_OPTION_1, ObjectOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.OBJECT_OPTION_2, ObjectOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.OBJECT_OPTION_3, ObjectOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.OBJECT_OPTION_4, ObjectOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.OBJECT_OPTION_5, ObjectOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.OBJECT_OPTION_6, ObjectOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_1, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_2, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_3, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_4, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_5, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_6, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_7, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_8, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_9, PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.PLAYER_OPTION_10,PlayerOptionMessageHandler())
		GameCodec.registerHandler(GameOpcodes.DONE_LOADING_REGION, RegionLoadedMessageHandler())
		GameCodec.registerHandler(GameOpcodes.SCREEN_CHANGE, ScreenChangeMessageHandler())
		GameCodec.registerHandler(GameOpcodes.STRING_ENTRY, StringEntryMessageHandler())
		GameCodec.registerHandler(GameOpcodes.WALK, WalkMapMessageHandler())
		GameCodec.registerHandler(GameOpcodes.MINI_MAP_WALK, WalkMiniMapMessageHandler())
	}
}