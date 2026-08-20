package content.skill.smithing

import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Smithing one of the corporeal beast's sigils onto a blessed spirit shield at an anvil turns the
 * shield into the spirit shield of that sigil's kind.
 */
class SpiritShieldSigils : Script {

    private val prayerLevel = 90
    private val smithingLevel = 85

    init {
        itemOnObjectOperate(SIGILS.plus(SHIELD).joinToString(","), "anvil*") { (_, item) ->
            attach(item)
        }
    }

    private suspend fun Player.attach(used: Item) {
        val sigil = if (used.id == SHIELD) SIGILS.firstOrNull { inventory.contains(it) } else used.id
        if (sigil == null) {
            statement("You need a sigil to attach to the shield.")
            return
        }
        if (!inventory.contains(SHIELD)) {
            statement("You need a blessed spirit shield to attach the sigil to.")
            return
        }
        // Prayer's current level is the player's remaining prayer points, so take the base level.
        if (!hasMax(Skill.Prayer, prayerLevel)) {
            statement("You need a Prayer level of $prayerLevel to attach a sigil to a blessed spirit shield.")
            return
        }
        if (!has(Skill.Smithing, smithingLevel)) {
            statement("You need a Smithing level of $smithingLevel to attach a sigil to a blessed spirit shield.")
            return
        }
        if (!inventory.contains("hammer")) {
            statement("You need a hammer to work the metal with.")
            return
        }
        anim("smith_item")
        delay(4)
        val shield = shield(sigil)
        val success = inventory.transaction {
            remove(sigil)
            remove(SHIELD)
            add(shield)
        }
        if (!success) {
            return
        }
        exp(Skill.Smithing, 1800.0)
        item(shield, "You successfully attach the ${sigil.removeSuffix("_sigil")} sigil to the blessed spirit shield.")
    }

    companion object {
        const val SHIELD = "blessed_spirit_shield"

        val SIGILS = listOf("arcane_sigil", "divine_sigil", "elysian_sigil", "spectral_sigil")

        fun shield(sigil: String) = "${sigil.removeSuffix("_sigil")}_spirit_shield"
    }
}
