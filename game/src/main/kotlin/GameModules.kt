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
import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.data.*
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns

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
        get<Storage>().offers(Settings["grandExchange.offers.activeDays", 0])
    }
    single(createdAtStart = true) {
        ExchangeHistory(get(), get<Storage>().priceHistory().toMutableMap()).also { it.calculatePrices() }
    }
    single(createdAtStart = true) {
        ClaimableOffers(get<Storage>().claims().toMutableMap())
    }
    single(createdAtStart = true) {
        GrandExchange(get(), get(), get(), get(), get(), get(), get())
    }
}
