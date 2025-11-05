package content.skill.smithing

import world.gregs.voidps.engine.entity.character.sound
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.data.definition.data.Smelting
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.remove

class SuperheatItem : Script {

    val spellDefinitions: SpellDefinitions by inject()
    val itemDefinitions: ItemDefinitions by inject()

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
            val smelting: Smelting = itemDefinitions.get(bar)["smelting"]
            if (!has(Skill.Smithing, smelting.level, message = true)) {
                sound("superheat_fail")
                return@onItem
            }
            val spell = "superheat_item"
            inventory.transaction {
                removeItems(this@onItem, spell)
                remove(smelting.items)
                add(bar)
            }
            if (inventory.transaction.error == TransactionError.None) {
                sound("superheat_all")
                anim(spell)
                gfx(spell)
                val definition = spellDefinitions.get(spell)
                experience.add(Skill.Magic, definition.experience)
                experience.add(Skill.Smithing, smelting.exp(this, bar))
            } else {
                sound("superheat_fail")
            }
        }
    }
}
