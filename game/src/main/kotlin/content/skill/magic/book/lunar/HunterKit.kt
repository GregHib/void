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
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class HunterKit : Script {

    private val contents = listOf(
        "noose_wand",
        "butterfly_net",
        "bird_snare",
        "rabbit_snare",
        "teasing_stick",
        "unlit_torch",
        "box_trap",
    )

    init {
        interfaceOption("Cast", "lunar_spellbook:hunter_kit") {
            val spell = it.component
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (inventory.isFull()) {
                message("You don't have enough inventory space for a hunter kit.")
                return@interfaceOption
            }
            if (!removeSpellItems(spell)) {
                return@interfaceOption
            }
            start("action_delay", 2)
            anim("hunter_kit")
            gfx(spell)
            sound(spell)
            exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
            inventory.add("hunter_kit")
        }

        itemOption("Open", "hunter_kit") {
            if (inventory.spaces < contents.size - 1) {
                message("You don't have enough inventory space to unpack the kit.")
                return@itemOption
            }
            inventory.transaction {
                remove(it.item.id)
                for (tool in contents) {
                    add(tool)
                }
            }
            if (inventory.transaction.error != TransactionError.None) {
                message("You don't have enough inventory space to unpack the kit.")
            }
        }
    }
}
