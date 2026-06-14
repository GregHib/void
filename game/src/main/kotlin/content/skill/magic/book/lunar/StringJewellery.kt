package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.queue.weakQueue

class StringJewellery : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:string_jewellery") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (find(this) == null) {
                message("You have no unstrung items to cast this spell on.")
                return@interfaceOption
            }
            string(this)
        }
    }

    private fun find(player: Player): Triple<Int, String, String>? {
        for (index in player.inventory.indices) {
            val item = player.inventory[index]
            val product = Tables.itemOrNull("string_jewellery.${item.id}.product")
            if (product != null) {
                return Triple(index, item.id, product)
            }
        }
        return null
    }

    private fun string(player: Player) {
        val (index, id, product) = find(player) ?: return
        if (!player.removeSpellItems("string_jewellery")) {
            return
        }
        player.inventory.transaction {
            replace(index, id, product)
        }
        if (player.inventory.transaction.error != TransactionError.None) {
            return
        }
        player.start("action_delay", 4)
        player.anim("string_jewellery")
        player.gfx("string_jewellery")
        player.sound("string_jewellery")
        player.exp(Skill.Magic, Tables.int("spells.string_jewellery.xp") / 10.0)
        player.exp(Skill.Crafting, 4.0)
        player.weakQueue("string_jewellery", 4) {
            string(player)
        }
    }
}
