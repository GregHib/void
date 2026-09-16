package world.gregs.voidps.web.site

import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Skill and boss definitions shared by server-rendered pages (Hiscores tile grids) and the
 * client-side Alpine components in `hiscores.js`/`log.js`, which read them off
 * `window.VOID_SKILLS`/`window.VOID_BOSSES` (see [script]) instead of each hardcoding its own copy.
 */
object GameData {

    class BossDef(val id: String, val name: String)

    /** Boss id (matches the npc definition id kills/records are keyed by) to display name. */
    val bosses = listOf(
        BossDef("giant_mole", "Giant Mole"), BossDef("king_black_dragon", "King Black Dragon"), BossDef("kril_tsutsaroth", "Kril Tsutsaroth"),
        BossDef("commander_zilyana", "Commander Zilyana"), BossDef("general_graardor", "General Graardor"), BossDef("kree_arra", "Kree'arra"),
        BossDef("chaos_elemental", "Chaos Elemental"), BossDef("dagannoth_rex", "Dagannoth Rex"), BossDef("dagannoth_prime", "Dagannoth Prime"),
        BossDef("dagannoth_supreme", "Dagannoth Supreme"), BossDef("kalphite_queen", "Kalphite Queen"), BossDef("tztok_jad", "TzTok-Jad"),
    )

    /** Inline `<script>` body defining `window.VOID_SKILLS`/`window.VOID_BOSSES` as JSON for the page's other scripts to read. */
    fun script(): String = buildString {
        append("window.VOID_SKILLS=[")
        Skill.entries.joinTo(this, ",") { "{\"name\":\"${it.name}\",\"max\":${it.maximum()},\"icon\":\"void/images/skills/${it.name.lowercase()}.png\"}" }
        append("];window.VOID_BOSSES=[")
        bosses.joinTo(this, ",") { "{\"id\":\"${it.id}\",\"name\":\"${it.name}\"}" }
        append("];")
    }
}
