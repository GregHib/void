package world.gregs.voidps.web.api.service

import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.ItemCategory
import world.gregs.voidps.web.api.model.ItemDetail
import world.gregs.voidps.web.api.model.ItemQuery
import world.gregs.voidps.web.api.model.ItemSummary
import world.gregs.voidps.web.api.model.MarketHighlights
import world.gregs.voidps.web.api.model.MarketSummary
import world.gregs.voidps.web.api.model.Page
import world.gregs.voidps.web.api.model.PriceHistory
import world.gregs.voidps.web.api.model.PriceSnapshot
import world.gregs.voidps.web.api.model.Timeframe

/**
 * The Grand Exchange. Prices are sampled on a fixed interval rather than computed per request —
 * [summary] carries the sample times, and [prices] is the cheap poll the live ticker uses between
 * page loads.
 *
 * Item ids are the lowercase hyphenated name, e.g. `warden-s-sigil`.
 */
interface ExchangeService {

    suspend fun summary(): MarketSummary

    /** Categories with their tile codes and blurbs, for the search chips and the item page. */
    suspend fun categories(): List<ItemCategory>

    /** All four home-page panels at once, since the page always shows them together. */
    suspend fun highlights(limit: Int): MarketHighlights

    suspend fun search(query: ItemQuery): Page<ItemSummary>

    /** @throws ApiException.NotFound when [itemId] is not tracked */
    suspend fun item(itemId: String): ItemDetail

    /** @throws ApiException.NotFound when [itemId] is not tracked */
    suspend fun history(itemId: String, timeframe: Timeframe): PriceHistory

    /** Other items in the same category. */
    suspend fun related(itemId: String, limit: Int): List<ItemSummary>

    /**
     * Current prices for whichever ids are on screen. Unknown ids are skipped rather than failing
     * the call, so one stale id on a page does not blank the whole ticker.
     */
    suspend fun prices(itemIds: List<String>): PriceSnapshot
}
