package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/** Summoning level at which the player starts understanding their pets' speech. */
private const val UNDERSTAND_LEVEL = 14

/**
 * Walks one of the breed/stage conversations declared in `pet_talks.tables.toml`.
 *
 * Inventory-conditional rows (Dalmatian + logs, Bulldog + cup_of_tea, Sheepdog + wool)
 * win over the random pool when the relevant item is on the player.
 */
suspend fun Player.talkToDog(row: RowDefinition, dog: NPC) {
    val breed = row.dogBreed() ?: return
    val stageKey = if (row.stageForNpc(dog.id) == PetStage.Baby) "baby" else "grown"

    val rows = Tables.get("pet_talks").rows().filter {
        val stages = it.string("stage")
        it.string("pet") == breed && (stages.isEmpty() || stages.split(',').any { s -> s.trim() == stageKey })
    }
    if (rows.isEmpty()) return

    val matchingConditional = rows.filter { matchesPetCondition(it.string("condition")) }
    val chosen = matchingConditional.randomOrNull()
        ?: rows.filter { it.string("condition").isBlank() }.randomOrNull()
        ?: return

    val understandsPet = levels.get(Skill.Summoning) >= UNDERSTAND_LEVEL
    for (line in chosen.stringList("lines")) {
        when {
            line.startsWith("npc:") -> {
                val raw = line.removePrefix("npc:").trim()
                val text = renderDogLine(raw, understandsPet)
                if (text.isNotBlank()) {
                    npc(dog.id, dogExpressionFor(raw), text)
                }
            }
            line.startsWith("player:") -> player<Happy>(line.removePrefix("player:").trim())
            else -> statement(line)
        }
    }
}

/**
 * Picks the dog chathead expression for a given dialogue line. The cache exposes four:
 *   expression_dog_normal (6551) — default idle / talking
 *   expression_dog_quiz   (6553) — asking / questioning
 *   expression_dog_no     (6552) — head shake / disagreement
 *   expression_dog_down   (6550) — head down / sad
 *
 * We pick based on the line's punctuation and a couple of word hints. Falls back to
 * `dog_normal` for anything that doesn't fit one of the other moods.
 */
private fun dogExpressionFor(line: String): String {
    val trimmed = line.trim().trimEnd('.', '!')
    val lower = trimmed.lowercase()
    val translation = trimmed.substringAfter('(', "").substringBeforeLast(')').lowercase()
    return when {
        trimmed.endsWith('?') -> "dog_quiz"
        translation.startsWith("no") || lower.startsWith("no ") || lower == "no" -> "dog_no"
        "whine" in lower || "grumble" in lower || "sad" in translation || "bored" in translation -> "dog_down"
        else -> "dog_normal"
    }
}

/**
 * Lines wrapped in [brackets] are thought-bubbles the player picks up via context, so they render
 * as-is. Lines like "Whiiiiine. (Boring!)" carry both a bark and an English translation; below the
 * understanding threshold the chathead shows only the bark, at or above it the chathead shows the
 * bark on the first line with the translation on a second line beneath it.
 */
private fun renderDogLine(line: String, understandsPet: Boolean): String {
    if (line.startsWith("[") && line.endsWith("]")) {
        return line.removePrefix("[").removeSuffix("]").trim()
    }
    val parenStart = line.indexOf('(')
    val parenEnd = if (parenStart >= 0) line.indexOf(')', parenStart + 1) else -1
    if (parenStart < 0 || parenEnd < 0) {
        return line
    }
    val bark = line.substring(0, parenStart).trim()
    val translation = line.substring(parenStart + 1, parenEnd).trim()
    return if (understandsPet) "$bark\n($translation)" else bark
}
