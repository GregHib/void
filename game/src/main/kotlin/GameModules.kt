import content.bot.TaskManager
import content.bot.interact.navigation.graph.NavigationGraph
import content.bot.interact.path.Dijkstra
import content.bot.interact.path.DijkstraFrontier
import content.entity.obj.ship.CharterShips
import content.entity.player.modal.book.Books
import content.entity.world.music.MusicTracks
import content.quest.member.fairy_tale_part_2.fairy_ring.FairyRingCodes
import content.social.trade.exchange.GrandExchange
import content.social.trade.exchange.history.ExchangeHistory
import content.social.trade.exchange.offer.ClaimableOffers
import content.social.trade.exchange.offer.Offers
import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.find
import world.gregs.voidps.engine.data.list
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
    single(createdAtStart = true) { NavigationGraph(get(), get()).load(files.find(Settings["map.navGraph"])) }
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
            InterfaceHandler(get(), get(), get()),
        )
    }
    single(createdAtStart = true) {
        val buy = File(Settings["storage.grand.exchange.offers.buy.path"])
        val sell = File(Settings["storage.grand.exchange.offers.sell.path"])
        buy.mkdirs()
        sell.mkdirs()
        Offers().load(buy, sell, Settings["grandExchange.offers.activeDays", 0])
    }
    single(createdAtStart = true) {
        val file = File(Settings["storage.grand.exchange.history.path"])
        file.mkdirs()
        ExchangeHistory(get()).load(file)
    }
    single(createdAtStart = true) {
        val file = File(Settings["storage.grand.exchange.offers.claim.path"])
        ClaimableOffers().load(file)
    }
    single(createdAtStart = true) {
        val history = File(Settings["storage.grand.exchange.history.path"])
        val buy = File(Settings["storage.grand.exchange.offers.buy.path"])
        val sell = File(Settings["storage.grand.exchange.offers.sell.path"])
        val claims = File(Settings["storage.grand.exchange.offers.claim.path"])
        GrandExchange(get(), get(), get(), get(), get(), get(), history, buy, sell, claims)
    }
}
