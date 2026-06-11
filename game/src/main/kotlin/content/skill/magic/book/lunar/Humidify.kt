package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class Humidify : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:humidify") {
            val spell = it.component
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (inventory.items.none { item -> item.id != "enchanted_vial" && item.def.contains("full") }) {
                message("You have no empty containers to fill.")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            start("action_delay", 2)
            anim("humidify")
            gfx(spell)
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
            inventory.transaction {
                for (index in inventory.indices) {
                    val item = inventory[index]
                    // Holy water requires blessed water from the Legends' Quest golden bowl
                    if (item.id == "enchanted_vial" || !item.def.contains("full")) {
                        continue
                    }
                    replace(index, item.id, item.def["full"])
                }
            }
            if (inventory.transaction.error != TransactionError.None) {
                return@interfaceOption
            }
            message("You cast the spell and the empty containers fill with water.")
        }
    }
}
