package content.skill.magic.book.lunar

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class PlankMake : Script {

    private val planks: Map<String, Pair<String, Int>> by lazy {
        val table = Tables.getOrNull("plank_make") ?: return@lazy emptyMap()
        table.rows.associate {
            val row = Rows.get(it)
            row.item("log") to (row.item("plank") to row.int("cost"))
        }
    }

    init {
        onItem("lunar_spellbook:plank_make") { item, id ->
            if (hasClock("action_delay")) {
                return@onItem
            }
            val spell = id.substringAfter(":")
            val (plank, cost) = planks[item.id] ?: run {
                message("You need to cast this spell on logs.")
                return@onItem
            }
            inventory.transaction {
                removeItems(this@onItem, spell, message = false)
                remove("coins", cost)
                replace(item.id, plank)
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    start("action_delay", 3)
                    anim("plank_make")
                    gfx(spell)
                    sound(spell)
                    exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
                }
                is TransactionError.Deficient -> {
                    if (!inventory.contains("coins", cost)) {
                        message("You need $cost coins to convert those logs into planks.")
                    } else {
                        message("You do not have the required items to cast this spell.")
                    }
                }
                else -> return@onItem
            }
        }
    }
}
