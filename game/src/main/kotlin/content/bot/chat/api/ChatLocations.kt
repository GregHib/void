package content.bot.chat.api

import content.bot.chat.tag.ChatEntityTagger
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.type.Tile

/**
 * Quick chat locations (the only place names bots can say) and their tiles from the `locations` table
 */
object ChatLocations {

    /**
     * Key and tile of the quick chat location nearest to [tile]
     */
    fun nearest(tile: Tile): Pair<String, Tile>? {
        val names = ChatEntityTagger.locationNames()
        return Tables.get("locations").rows()
            .filter { it.rowId in names }
            .mapNotNull { row -> row.tileOrNull("tile")?.takeIf { it.x != 0 }?.let { row.rowId to it } }
            .minByOrNull { (_, location) -> location.distanceTo(tile) }
    }

    /**
     * Display name e.g. "seers_village" -> "Seers' Village"
     */
    fun name(key: String): String = ChatEntityTagger.locationNames()[key] ?: key.split('_').joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
}
