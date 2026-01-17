import content.bot.TaskManager
import content.bot.interact.navigation.graph.NavigationGraph
import content.bot.interact.path.Dijkstra
import content.bot.interact.path.DijkstraFrontier
import content.entity.obj.ship.CharterShips
import content.entity.player.modal.book.Books
import content.entity.world.music.MusicTracks
import content.quest.member.fairy_tale_part_2.fairy_ring.FairyRingCodes
import content.skill.farming.FarmingDefinitions
import content.social.trade.exchange.GrandExchange
import content.social.trade.exchange.history.ExchangeHistory
import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import java.io.File

fun gameModule(files: ConfigFiles) = module {
    single { ItemSpawns() }
    single { TaskManager() }
    single {
        val size = get<NavigationGraph>().size
        Dijkstra(
            get(),
            object : DefaultPool<DijkstraFrontier>(10) {
                override fun produceInstance() = DijkstraFrontier(size)
            },
        )
    }
    single(createdAtStart = true) { NavigationGraph(get()).load(files.find(Settings["map.navGraph"])) }
    single(createdAtStart = true) { Books().load(files.list(Settings["definitions.books"])) }
    single(createdAtStart = true) { MusicTracks().load(files.find(Settings["map.music"])) }
    single(createdAtStart = true) { FairyRingCodes().load(files.find(Settings["definitions.fairyCodes"])) }
    single(createdAtStart = true) { CharterShips().load(files.find(Settings["map.ships.prices"])) }
    single {
        InstructionHandlers(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            InterfaceHandler(get(), get(), get(), get()),
        )
    }
    single(createdAtStart = true) {
        get<Storage>().offers(Settings["grandExchange.offers.activeDays", 0])
    }
    single(createdAtStart = true) {
        ExchangeHistory(get(), get<Storage>().priceHistory().toMutableMap()).also { it.calculatePrices() }
    }
    single(createdAtStart = true) {
        GrandExchange(get(), get(), get<Storage>().claims().toMutableMap(), get(), get(), get(), get())
    }
    single {
        if (Settings["storage.type", "files"] == "database") {
            val clazz: Class<*>
            val companion: Class<*>
            try {
                clazz = Class.forName("world.gregs.voidps.storage.DatabaseStorage")
                companion = Class.forName("${clazz.name}\$Companion")
            } catch (e: ClassNotFoundException) {
                throw IllegalStateException("Database class not found; are you compiling using `-PincludeDb`?")
            }
            val method = companion.declaredMethods.first { it.name == "connect" }
            val instance = companion.constructors.first().newInstance(null)
            method.invoke(
                instance,
                Settings["storage.database.username"],
                Settings["storage.database.password"],
                Settings["storage.database.driver"],
                Settings["storage.database.jdbcUrl"],
                Settings["storage.database.poolSize", 2],
            )
            clazz.constructors.first().newInstance() as Storage
        } else {
            val saves = File(Settings["storage.players.path"])
            if (!saves.exists()) {
                saves.mkdir()
            }
            FileStorage(saves)
        }
    }
    single(createdAtStart = true) { FarmingDefinitions().load(files.find(Settings["definitions.produce"])) }
}
