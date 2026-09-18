package world.gregs.voidps.web.hiscores

/**
 * Bosses tracked on the hiscores, keyed by their npc definition id - the same id
 * [world.gregs.voidps.engine.data.PlayerSave.kills] and `records` are keyed by. Curated from the
 * npc definitions tagged `categories = [..., "boss"]` under `data/`.
 */
object Bosses {

    /** Mirrors the `bosses` list in `Hiscores.kt` (the `:web:site` module) - keep them in sync. */
    private val names: Map<String, String> = linkedMapOf(
        "giant_mole" to "Giant Mole",
        "king_black_dragon" to "King Black Dragon",
        "kril_tsutsaroth" to "Kril Tsutsaroth",
        "commander_zilyana" to "Commander Zilyana",
        "general_graardor" to "General Graardor",
        "kree_arra" to "Kree'arra",
        "chaos_elemental" to "Chaos Elemental",
        "dagannoth_rex" to "Dagannoth Rex",
        "dagannoth_prime" to "Dagannoth Prime",
        "dagannoth_supreme" to "Dagannoth Supreme",
        "kalphite_queen" to "Kalphite Queen",
        "tztok_jad" to "TzTok-Jad",
    )

    val ids: List<String> = names.keys.toList()

    fun exists(id: String): Boolean = id in names

    fun name(id: String): String = names[id] ?: id
}
