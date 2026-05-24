package content.skill.summoning.pet

import content.quest.questCompleted
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment

enum class PetStage { Baby, Grown, Overgrown }

private val DOG_BREEDS = setOf("bulldog", "dalmatian", "greyhound", "labrador", "sheepdog", "terrier")

fun RowDefinition.isCatLike(): Boolean = rowId == "hellcat" || rowId == "cat" || rowId.startsWith("cat_")

private val VARIANT_SUFFIX = Regex("(.*)_\\d+$")

/** Strips trailing `_<digit>` suffix from a row id so colour variants share one pet_talks row. */
fun RowDefinition.petTalksKey(): String = VARIANT_SUFFIX.matchEntire(rowId)?.groupValues?.get(1) ?: rowId

/** Maps row ids like `bulldog`, `bulldog_1`, `bulldog_2` back to the canonical breed name, or null for non-dog pets. */
fun RowDefinition.dogBreed(): String? {
    val base = rowId.substringBefore('_')
    val candidate = if (rowId.contains('_') && rowId.substringAfter('_').all(Char::isDigit)) base else rowId
    return if (candidate in DOG_BREEDS) candidate else null
}

fun RowDefinition.itemFor(stage: PetStage): String? = when (stage) {
    PetStage.Baby -> itemOrNull("baby_item")
    PetStage.Grown -> itemOrNull("grown_item")
    PetStage.Overgrown -> itemOrNull("overgrown_item")
}

fun RowDefinition.npcFor(stage: PetStage): String? = when (stage) {
    PetStage.Baby -> npcOrNull("baby_npc")
    PetStage.Grown -> npcOrNull("grown_npc")
    PetStage.Overgrown -> npcOrNull("overgrown_npc")
}

fun RowDefinition.stageForItem(item: String): PetStage? = when (item) {
    itemOrNull("baby_item") -> PetStage.Baby
    itemOrNull("grown_item") -> PetStage.Grown
    itemOrNull("overgrown_item") -> PetStage.Overgrown
    else -> null
}

fun RowDefinition.stageForNpc(npc: String): PetStage? = when (npc) {
    npcOrNull("baby_npc") -> PetStage.Baby
    npcOrNull("grown_npc") -> PetStage.Grown
    npcOrNull("overgrown_npc") -> PetStage.Overgrown
    else -> null
}

fun RowDefinition.nextStageItem(item: String): String? = when (item) {
    itemOrNull("baby_item") -> itemOrNull("grown_item")
    itemOrNull("grown_item") -> itemOrNull("overgrown_item")
    else -> null
}

fun RowDefinition.nextStageNpc(npc: String): String? = when (npc) {
    npcOrNull("baby_npc") -> npcOrNull("grown_npc")
    npcOrNull("grown_npc") -> npcOrNull("overgrown_npc")
    else -> null
}

fun RowDefinition.isFinalStage(item: String): Boolean {
    val stage = stageForItem(item) ?: return true
    return nextStageItem(item) == null || (isCatLike() && stage != PetStage.Baby)
}

fun RowDefinition.ambientPhrases(): List<String> {
    val idle = stringList("idle_phrases")
    if (idle.isNotEmpty()) return idle
    val hungry = stringOrNull("hungry_phrase")?.takeIf { it.isNotBlank() } ?: return emptyList()
    return listOf(hungry)
}

/** Picks the tier-N hunger bark (0 = hungry, 1 = starving, 2 = runaway), falling back to the legacy single `hungry_phrase`. */
fun RowDefinition.hungerPhrase(tier: Int): String? {
    val tiered = stringList("hungry_phrases")
    if (tiered.isNotEmpty()) {
        return tiered.getOrNull(tier) ?: tiered.last()
    }
    return stringOrNull("hungry_phrase")?.takeIf { it.isNotBlank() }
}

// Lazily-built lookup tables. Pet rows are scanned on every timer tick,
// Talk-to, drop, feed and Interact-with click; a linear firstOrNull across
// the table on each call dominated the hot path. Both maps are initialised
// on first access (after Tables load) and remain stable for the lifetime of
// the process.
private val itemIndex: Map<String, RowDefinition> by lazy {
    buildMap {
        for (row in Tables.get("pets").rows()) {
            row.itemOrNull("baby_item")?.let { put(it, row) }
            row.itemOrNull("grown_item")?.let { put(it, row) }
            row.itemOrNull("overgrown_item")?.let { put(it, row) }
        }
    }
}

private val npcIndex: Map<String, RowDefinition> by lazy {
    buildMap {
        for (row in Tables.get("pets").rows()) {
            row.npcOrNull("baby_npc")?.let { put(it, row) }
            row.npcOrNull("grown_npc")?.let { put(it, row) }
            row.npcOrNull("overgrown_npc")?.let { put(it, row) }
        }
    }
}

fun petRowForItem(itemId: String): RowDefinition? = itemIndex[itemId]

fun petRowForNpc(npcId: String): RowDefinition? = npcIndex[npcId]

fun allPetRows(): List<RowDefinition> = Tables.get("pets").rows()

/**
 * Evaluates a `pet_talks` `condition` cell against the player's inventory, equipment, location and quest log.
 *
 * Supported syntaxes:
 *   - `""` (blank): not a conditional row; never matches via this helper.
 *   - `"item_id"`: matches if the item is in inventory or equipped.
 *   - `"a|b|c"`: pipe-separated list; matches if any item is in inventory or equipped.
 *   - `"count:N:a|b|c|d"`: matches if at least N of the listed items are equipped.
 *   - `"tainted:<god>"`: matches if any equipped item has a `god` param that is non-empty and not `<god>`.
 *   - `"area:<name>"`: matches if the player tile is inside the named area.
 *   - `"tag:<tag>"`: matches if the player tile is inside any area carrying the tag.
 *   - `"quest:<name>"`: matches if the player has completed the named quest.
 *   - `"skill_below:<skill>:<level>"`: matches when the player's level in the named skill is strictly less than `<level>`. Used by adult sneakerpeeper to gate a low-Summoning garbled line.
 */
fun Player.matchesPetCondition(condition: String): Boolean {
    if (condition.isBlank()) return false
    return when {
        condition.startsWith("tainted:") -> {
            val ownGod = condition.removePrefix("tainted:").trim()
            equipment.items.any {
                val g = it.def.getOrNull<String>("god") ?: ""
                g.isNotBlank() && g != ownGod
            }
        }
        condition.startsWith("skill_below:") -> {
            val rest = condition.removePrefix("skill_below:")
            val sep = rest.indexOf(':')
            if (sep < 0) return false
            val skillName = rest.substring(0, sep).trim().replaceFirstChar { it.uppercase() }
            val skill = Skill.of(skillName) ?: return false
            val limit = rest.substring(sep + 1).trim().toIntOrNull() ?: return false
            levels.get(skill) < limit
        }
        condition.startsWith("count:") -> {
            val rest = condition.removePrefix("count:")
            val sep = rest.indexOf(':')
            if (sep < 0) return false
            val needed = rest.substring(0, sep).trim().toIntOrNull() ?: return false
            val itemIds = rest.substring(sep + 1).split('|').map { it.trim() }.toSet()
            equipment.items.count { it.id in itemIds } >= needed
        }
        condition.startsWith("area:") -> Areas.get(condition.removePrefix("area:").trim()).contains(tile)
        condition.startsWith("tag:") -> Areas.tagged(condition.removePrefix("tag:").trim()).any { it.area.contains(tile) }
        condition.startsWith("quest:") -> questCompleted(condition.removePrefix("quest:").trim())
        '|' in condition -> condition.split('|').any { carriesItem(it.trim()) }
        else -> carriesItem(condition)
    }
}
