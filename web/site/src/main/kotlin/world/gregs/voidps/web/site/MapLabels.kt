package world.gregs.voidps.web.site

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index.WORLD_MAP
import world.gregs.voidps.cache.config.data.WorldMapInfoDefinition
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.cache.definition.decoder.WorldMapDetailsDecoder

/**
 * The place names the client paints over the world map — "Varrock", "Kingdom of Misthalin",
 * "Here be goblins" — read straight out of the cache so [WorldMap] doesn't have to hardcode them.
 *
 * They're assembled from two indexes. [WorldMapDetailsDecoder] gives one definition per map
 * ("main" for the surface, plus a map apiece for Zanaris, God Wars, every dungeon...), whose
 * [world.gregs.voidps.cache.definition.data.WorldMapDefinition.map] names the
 * `{map}_staticelements` archive holding that map's elements: a packed tile position and a
 * [WorldMapInfoDecoder] id each. That info definition is what carries the text, its colour and its
 * font size — an element whose definition has a `spriteId` instead is a map *icon* (bank, altar,
 * quest start) rather than a label, so it's skipped here.
 */
class MapLabels(cache: Cache) {

    /**
     * [lines] is the label's text pre-split on the cache's `<br>` markers — these names are
     * authored to wrap at a specific point ("Kingdom of" / "Misthalin"), not to reflow.
     * [colour] is a CSS `#rrggbb`: white for most, orange for the kingdom/region headings.
     * [size] is the cache's own font size class — 0 small, 1 medium, 2 large — which `worldmap.js`
     * turns into both a font size and the zoom the label starts appearing at.
     */
    data class Label(
        val lines: List<String>,
        val x: Int,
        val y: Int,
        val level: Int,
        val colour: String,
        val size: Int,
    )

    val labels: List<Label> = load(cache)

    private fun load(cache: Cache): List<Label> {
        val info = WorldMapInfoDecoder().load(cache)
        val labels = mutableListOf<Label>()
        for (map in WorldMapDetailsDecoder().load(cache)) {
            val archive = cache.archiveId(WORLD_MAP, "${map.map}_staticelements")
            for (file in 0 until cache.fileCount(WORLD_MAP, archive)) {
                val data = cache.data(WORLD_MAP, archive, file) ?: continue
                val buffer = ArrayReader(data)
                val position = buffer.readInt()
                val definition = info.getOrNull(buffer.readShort()) ?: continue
                val name = definition.name
                if (definition.spriteId != -1 || name.isNullOrBlank() || definition.hiddenOnWorldMap) {
                    continue
                }
                if (!shownByDefault(definition)) {
                    continue
                }
                labels.add(
                    Label(
                        lines = name.split("<br>"),
                        x = position shr 14 and 0x3fff,
                        y = position and 0x3fff,
                        level = position shr 28 and 0x3,
                        colour = "#%06x".format(definition.anInt1058 and 0xffffff),
                        size = definition.fontSize,
                    ),
                )
            }
        }
        return labels
    }

    /**
     * Whether a label is drawn for a player who has made no progress anywhere.
     *
     * A label can be gated on a var (opcode 9): the client only draws it while that varbit/varp's
     * value falls in `anInt1087..anInt1042`, which is how one tile carries several mutually
     * exclusive names. A static page has no player and so no var state, so the default value of 0
     * decides. Barbarian Village is the only place in the cache that uses this — it holds
     * "Barbarian Village" (`0..0`) plus "Gunnarsgrunn" and its "(Barbarian Village)" subtitle
     * (both `1..1`, once the rename var is set) on the same tile — so without this they'd stack
     * on top of each other.
     */
    private fun shownByDefault(definition: WorldMapInfoDefinition): Boolean {
        if (definition.varbit == -1 && definition.varp == -1) {
            return true
        }
        return 0 in definition.anInt1087..definition.anInt1042
    }

    /** Inline `<script>` body defining `window.VOID_MAP_LABELS` as JSON for `worldmap.js` to read. */
    fun script(): String = buildString {
        append("window.VOID_MAP_LABELS=[")
        labels.joinTo(this, ",") { label ->
            "{\"lines\":[${label.lines.joinToString(",") { "\"${jsonString(it)}\"" }}]," +
                "\"x\":${label.x},\"y\":${label.y},\"level\":${label.level}," +
                "\"colour\":\"${label.colour}\",\"size\":${label.size}}"
        }
        append("];")
    }
}

/** Escapes [text] for use inside a JSON double-quoted string in a generated inline `<script>`. */
internal fun jsonString(text: String): String = text.replace("\\", "\\\\").replace("\"", "\\\"")
