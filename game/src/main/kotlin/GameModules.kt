import content.bot.BotManager
import content.bot.behaviour.loadGraph
import content.bot.chat.BotChatModel
import content.entity.obj.ship.CharterShips
import content.entity.player.modal.book.Books
import content.entity.world.music.MusicTracks
import content.quest.member.fairy_tale_part_2.fairy_ring.FairyRingCodes
import content.skill.farming.FarmingDefinitions
import content.social.trade.exchange.GrandExchange
import content.social.trade.exchange.history.ExchangeHistory
import org.koin.dsl.module
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Reports
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.exchange.OpenOffers
import world.gregs.voidps.engine.data.file.FileStorage
import java.io.File

fun gameModule(files: ConfigFiles) = module {
    single { Reports(get()) }
    single(createdAtStart = true) { BotManager().load(files) }
    single { loadGraph(files) }
    single { if (Settings["bots.count", 0] > 0) BotChatModel().load(files, get()) else BotChatModel() }
    single(createdAtStart = true) { Books().load(files.list(Settings["definitions.books"])) }
    single(createdAtStart = true) { files.find(Settings["map.music"]).let { MusicTracks().load(it, files.snapshot("music_tracks", listOf(it))) } }
    single(createdAtStart = true) { FairyRingCodes().load(files.find(Settings["definitions.fairyCodes"])) }
    single(createdAtStart = true) { CharterShips().load(files.find(Settings["map.ships.prices"])) }
    single { InstructionHandlers(InterfaceHandler(get())) }
    val loadExchange = Settings["grandExchange.enabled", true]
    single(createdAtStart = true) {
        if (loadExchange) get<Storage>().offers(Settings["grandExchange.offers.activeDays", 0]) else OpenOffers()
    }
    single(createdAtStart = true) {
        if (loadExchange) {
            ExchangeHistory(get<Storage>().priceHistory().toMutableMap()).also { it.calculatePrices() }
        } else {
            ExchangeHistory(mutableMapOf())
        }
    }
    single(createdAtStart = true) {
        GrandExchange(get(), get(), if (loadExchange) get<Storage>().claims().toMutableMap() else mutableMapOf(), get(), get(), loaded = loadExchange)
    }
    single {
        if (Settings["storage.type", "files"] == "database") {
            val clazz: Class<*>
            val companion: Class<*>
            try {
                clazz = Class.forName("world.gregs.voidps.storage.DatabaseStorage")
                companion = Class.forName("${clazz.name}\$Companion")
            } catch (_: ClassNotFoundException) {
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
