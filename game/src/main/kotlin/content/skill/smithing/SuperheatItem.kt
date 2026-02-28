package content.skill.smithing

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.remove

class SuperheatItem(val spellDefinitions: SpellDefinitions) : Script {

    init {
        onItem("modern_spellbook:superheat_item") { item, _ ->
            if (!item.id.endsWith("_ore")) {
                message("You need to cast superheat item on ore.")
                sound("superheat_fail")
                return@onItem
            }
            var bar = item.id.replace("_ore", "_bar")
            if (bar == "iron_bar" && inventory.count("coal") >= 2) {
                bar = "steel_bar"
            }
            val xp = EnumDefinitions.intOrNull("smelting_xp", bar) ?: return@onItem
            val level = EnumDefinitions.int("smelting_level", bar)
            if (!has(Skill.Smithing, level, message = true)) {
                sound("superheat_fail")
                return@onItem
            }
            val spell = "superheat_item"
            val items = Furnace.requiredOres(bar)
            inventory.transaction {
                removeItems(this@onItem, spell)
                remove(items)
                add(bar)
            }
            if (inventory.transaction.error == TransactionError.None) {
                sound("superheat_all")
                anim(spell)
                gfx(spell)
                val definition = spellDefinitions.get(spell)
                experience.add(Skill.Magic, definition.experience)
                experience.add(Skill.Smithing, Furnace.goldXp(this, bar, xp / 10.0))
            } else {
                sound("superheat_fail")
            }
        }
    }
}
