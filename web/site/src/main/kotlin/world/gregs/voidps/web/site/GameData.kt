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

    /**
     * Fixed per-skill colour for xp charts (the adventurer's log), so a skill is always the same
     * colour no matter which other skills it's stacked alongside.
     */
    val skillColors: Map<String, String> = mapOf(
        "Attack" to "#c2493a",
        "Defence" to "#7d9db0",
        "Strength" to "#dd9a2b",
        "Constitution" to "#d1495c",
        "Ranged" to "#7fae4f",
        "Prayer" to "#f0c667",
        "Magic" to "#8b6bc4",
        "Cooking" to "#e08a3c",
        "Woodcutting" to "#6b8e4e",
        "Fletching" to "#c9a66b",
        "Fishing" to "#5b8fb0",
        "Firemaking" to "#e0663c",
        "Crafting" to "#b06bb0",
        "Smithing" to "#a0a8ad",
        "Mining" to "#7a6a57",
        "Herblore" to "#4f9e6e",
        "Agility" to "#4fb0a8",
        "Thieving" to "#6b4e8e",
        "Slayer" to "#8e2f2f",
        "Farming" to "#6a9e3f",
        "Runecrafting" to "#3f9ea0",
        "Hunter" to "#9e7a4f",
        "Construction" to "#71542c",
        "Summoning" to "#7a5ea8",
        "Dungeoneering" to "#c2a34a",
    )

    private val otherColor = "#5a646b"

    /** Inline `<script>` body defining `window.VOID_SKILLS`/`window.VOID_BOSSES` as JSON for the page's other scripts to read. */
    fun script(): String = buildString {
        append("window.VOID_SKILLS=[")
        Skill.entries.joinTo(this, ",") {
            "{\"name\":\"${it.name}\",\"max\":${it.maximum()},\"icon\":\"images/skills/${it.name.lowercase()}.png\"," +
                "\"color\":\"${skillColors[it.name] ?: otherColor}\"}"
        }
        append("];window.VOID_BOSSES=[")
        bosses.joinTo(this, ",") { "{\"id\":\"${it.first}\",\"name\":\"${it.second}\"}" }
        append("];")
    }
}
