package content.skill.magic.book.modern

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class BonesTo(val definitions: SpellDefinitions) : Script {
    init {
        interfaceOption("Cast", "modern_spellbook:bones_to_*") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (!inventory.contains("bones") && !inventory.contains("big_bones")) {
                message("You don't have any bones to cast this spell on.")
                return@interfaceOption
            }
            val spell = it.component
            val produce = if (spell == "bones_to_peaches") "peach" else "banana"
            val success = inventory.transaction {
                removeItems(this@interfaceOption, spell)
                var next = inventory.indexOf("bones")
                while (next != -1 && !failed) {
                    replace(next, "bones", produce)
                    next = inventory.indexOf("bones")
                }
                next = inventory.indexOf("big_bones")
                while (next != -1 && !failed) {
                    replace(next, "big_bones", produce)
                    next = inventory.indexOf("big_bones")
                }
            }
            if (!success) {
                return@interfaceOption
            }
            val definition = definitions.get(spell)
            start("action_delay", 1)
            anim("bones_to_spell")
            gfx("bones_to_spell")
            sound("bones_to_spell")
            exp(Skill.Magic, definition.experience)
        }
    }
}
