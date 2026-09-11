package world.gregs.voidps.web.api.route

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.model.ItemQuery
import world.gregs.voidps.web.api.model.ItemSort
import world.gregs.voidps.web.api.model.Items
import world.gregs.voidps.web.api.model.Timeframe
import world.gregs.voidps.web.api.model.WorldStatus
import world.gregs.voidps.web.api.service.ExchangeService
import world.gregs.voidps.web.api.service.WorldService

/** At most this many ids on the live ticker's poll, so one call cannot fetch the whole market. */
private const val MAX_PRICE_IDS = 100

fun Route.exchangeRoutes(exchange: ExchangeService) {
    route("/exchange") {
        get("/summary") {
            call.respond(exchange.summary())
        }

        get("/categories") {
            call.respond(Items(exchange.categories()))
        }

        get("/highlights") {
            call.respond(exchange.highlights(call.limit(default = 6, max = 25)))
        }

        // Declared before `/items/{itemId}` so `prices` is not read as an item id.
        get("/prices") {
            call.respond(exchange.prices(call.list("ids", MAX_PRICE_IDS)))
        }

        get("/items") {
            val query = ItemQuery(
                name = call.text("q"),
                category = call.filter("category") { it },
                members = call.boolean("members"),
                sort = call.choice("sort", ItemSort.Volume, ItemSort::of),
                page = call.page(),
            )
            call.respond(exchange.search(query))
        }

        route("/items/{itemId}") {
            get {
                call.respond(exchange.item(call.path("itemId")))
            }

            get("/history") {
                val timeframe = call.choice("timeframe", Timeframe.Day, Timeframe::of)
                call.respond(exchange.history(call.path("itemId"), timeframe))
            }

            get("/related") {
                val related = exchange.related(call.path("itemId"), call.limit(default = 4, max = 12))
                call.respond(Items(related))
            }
        }
    }
}

fun Route.worldRoutes(worlds: WorldService) {
    get("/worlds") {
        call.respond(worlds.worlds(call.filter("status", WorldStatus::of)))
    }
}
