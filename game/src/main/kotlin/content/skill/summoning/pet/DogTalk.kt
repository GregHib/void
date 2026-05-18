package content.skill.summoning.pet

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

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

    for (line in chosen.stringList("lines")) {
        when {
            line.startsWith("d:") -> npc<Happy>(dog.id, line.removePrefix("d:").trim())
            line.startsWith("b:") -> dog.say(line.removePrefix("b:").trim())
            line.startsWith("p:") -> player<Happy>(line.removePrefix("p:").trim())
            else -> statement(line)
        }
    }
}
