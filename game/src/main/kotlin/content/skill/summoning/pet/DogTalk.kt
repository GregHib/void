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
import world.gregs.voidps.engine.inv.inventory

/** Summoning level at which the player starts understanding their pets' speech. */
private const val UNDERSTAND_LEVEL = 14

/**
 * Walks one of the breed/stage conversations declared in `dog_talks.tables.toml`.
 *
 * Inventory-conditional rows (Dalmatian + logs, Bulldog + cup_of_tea, Sheepdog + wool)
 * win over the random pool when the relevant item is on the player.
 */
suspend fun Player.talkToDog(row: RowDefinition, dog: NPC) {
    val breed = row.dogBreed() ?: return
    val stageKey = if (row.stageForNpc(dog.id) == PetStage.Baby) "baby" else "grown"

    val rows = Tables.get("dog_talks").rows().filter {
        it.string("breed") == breed && it.string("stage") == stageKey
    }
    if (rows.isEmpty()) return

    val conditional = rows.firstOrNull {
        val item = it.string("condition")
        item.isNotBlank() && inventory.contains(item)
    }
    val chosen = conditional
        ?: rows.filter { it.string("condition").isBlank() }.randomOrNull()
        ?: return

    val understandsPet = levels.get(Skill.Summoning) >= UNDERSTAND_LEVEL
    for (line in chosen.stringList("lines")) {
        when {
            line.startsWith("d:") -> {
                val text = renderDogLine(line.removePrefix("d:").trim(), understandsPet)
                if (text.isNotBlank()) {
                    npc<Happy>(dog.id, text, largeHead = true)
                }
            }
            line.startsWith("p:") -> player<Happy>(line.removePrefix("p:").trim())
            else -> statement(line)
        }
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
