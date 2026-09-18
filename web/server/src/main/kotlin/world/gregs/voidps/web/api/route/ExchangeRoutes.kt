package world.gregs.voidps.web.api.route

import io.ktor.http.Parameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.exchange.ExchangeService

/**
 * Grand Exchange routes - market summary, categories, highlights, item search/detail/history and
 * a cheap current-prices poll, backed by [ExchangeService]. See `web/openapi.yaml` for the full contract.
 */
fun Route.exchangeRoutes(service: ExchangeService) {
    route("/exchange") {
        get("/summary") {
            call.respond(service.summary())
        }
        get("/categories") {
            call.respond(mapOf("items" to service.categories()))
        }
        get("/highlights") {
            val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 6).coerceIn(1, 25)
            call.respond(service.highlights(limit))
        }
        get("/items") {
            val params = call.request.queryParameters
            call.respond(
                service.items(
                    query = params["q"],
                    category = params["category"],
                    members = params["members"]?.toBooleanStrictOrNull(),
                    sort = params["sort"] ?: "volume",
                    page = params.page(),
                    pageSize = params.pageSize(),
                ),
            )
        }
        route("/items/{itemId}") {
            get {
                val itemId = call.parameters["itemId"] ?: throw ApiException.Validation("itemId", "Required")
                call.respond(service.item(itemId) ?: throw ApiException.NotFound("item", itemId))
            }
            get("/history") {
                val itemId = call.parameters["itemId"] ?: throw ApiException.Validation("itemId", "Required")
                val timeframe = call.request.queryParameters["timeframe"] ?: "24h"
                call.respond(service.history(itemId, timeframe) ?: throw ApiException.NotFound("item", itemId))
            }
            get("/related") {
                val itemId = call.parameters["itemId"] ?: throw ApiException.Validation("itemId", "Required")
                val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 4).coerceIn(1, 12)
                val result = service.related(itemId, limit) ?: throw ApiException.NotFound("item", itemId)
                call.respond(mapOf("items" to result))
            }
        }
        get("/prices") {
            val ids = call.request.queryParameters["ids"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: throw ApiException.Validation("ids", "Required")
            if (ids.isEmpty() || ids.size > 100) {
                throw ApiException.Validation("ids", "Must contain between 1 and 100 ids")
            }
            call.respond(service.prices(ids))
        }
    }
}

private fun Parameters.page(): Int = (this["page"]?.toIntOrNull() ?: 0).coerceAtLeast(0)

private fun Parameters.pageSize(): Int = (this["pageSize"]?.toIntOrNull() ?: 25).coerceIn(1, 100)
