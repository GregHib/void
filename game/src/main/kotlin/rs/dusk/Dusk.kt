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
				registerHandlers()
				count = GameCodec.decoders.size + GameCodec.handlers.size + GameCodec.encoders.size
			}
		}.run()
		object : TimedLoader<Unit>("Login codec") {
			override fun load(args: Array<out Any?>) {
				LoginCodec.register()
				count = LoginCodec.decoders.size + LoginCodec.handlers.size + LoginCodec.encoders.size
			}
		}.run()
		object : TimedLoader<Unit>("Service codec") {
			override fun load(args: Array<out Any?>) {
				ServiceCodec.register()
				count = ServiceCodec.decoders.size + ServiceCodec.handlers.size + ServiceCodec.encoders.size
			}
		}.run()
		object : TimedLoader<Unit>("Update codec") {
			override fun load(args: Array<out Any?>) {
				UpdateCodec.register()
				count = UpdateCodec.decoders.size + UpdateCodec.handlers.size + UpdateCodec.encoders.size
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

	private fun registerHandlers() {
		LoginCodec.registerHandler(GameLoginMessageHandler())

		GameCodec.registerHandler(ConsoleCommandMessageHandler())
		GameCodec.registerHandler(DialogueContinueMessageHandler())
		GameCodec.registerHandler(FloorItemOptionMessageHandler())
		GameCodec.registerHandler(IntEntryMessageHandler())
		GameCodec.registerHandler(InterfaceClosedMessageHandler())
		GameCodec.registerHandler(InterfaceOptionMessageHandler())
		GameCodec.registerHandler(InterfaceSwitchMessageHandler())
		GameCodec.registerHandler(NPCOptionMessageHandler())
		GameCodec.registerHandler(ObjectOptionMessageHandler())
		GameCodec.registerHandler(PlayerOptionMessageHandler())
		GameCodec.registerHandler(RegionLoadedMessageHandler())
		GameCodec.registerHandler(ScreenChangeMessageHandler())
		GameCodec.registerHandler(StringEntryMessageHandler())
		GameCodec.registerHandler(WalkMapMessageHandler())
		GameCodec.registerHandler(WalkMiniMapMessageHandler())
	}
}