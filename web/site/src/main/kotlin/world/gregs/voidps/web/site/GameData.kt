package world.gregs.voidps.web.site

import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * Skill and boss definitions shared by server-rendered pages (Hiscores tile grids) and the
 * client-side Alpine components in `hiscores.js`/`log.js`, which read them off
 * `window.VOID_SKILLS`/`window.VOID_BOSSES` (see [script]) instead of each hardcoding its own copy.
 */
class GameData {

    /** Boss id (matches the npc definition id kills/records are keyed by) to display name. */
    val bosses = NPCDefinitions.definitions
        .filter { it.getOrNull<Set<String>>("categories")?.contains("boss") == true }
        .map { Pair(it.stringId, it.name) }

    /** Inline `<script>` body defining `window.VOID_SKILLS`/`window.VOID_BOSSES` as JSON for the page's other scripts to read. */
    fun script(): String = buildString {
        append("window.VOID_SKILLS=[")
        Skill.entries.joinTo(this, ",") { "{\"name\":\"${it.name}\",\"max\":${it.maximum()},\"icon\":\"void/images/skills/${it.name.lowercase()}.png\"}" }
        append("];window.VOID_BOSSES=[")
        bosses.joinTo(this, ",") { "{\"id\":\"${it.first}\",\"name\":\"${it.second}\"}" }
        append("];")
    }
}
