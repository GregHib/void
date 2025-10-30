package content.skill.smithing

import content.entity.sound.sound
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.interfaceOnItem
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
        interfaceOnItem("modern_spellbook", "superheat_item") { player ->
            if (!item.id.endsWith("_ore")) {
                player.message("You need to cast superheat item on ore.")
                player.sound("superheat_fail")
                return@interfaceOnItem
            }
            var bar = item.id.replace("_ore", "_bar")
            if (bar == "iron_bar" && player.inventory.count("coal") >= 2) {
                bar = "steel_bar"
            }
            val smelting: Smelting = itemDefinitions.get(bar)["smelting"]
            if (!player.has(Skill.Smithing, smelting.level, message = true)) {
                player.sound("superheat_fail")
                return@interfaceOnItem
            }
            val spell = component
            player.inventory.transaction {
                removeItems(player, spell)
                remove(smelting.items)
                add(bar)
            }
            if (player.inventory.transaction.error == TransactionError.None) {
                player.sound("superheat_all")
                player.anim(spell)
                player.gfx(spell)
                val definition = spellDefinitions.get(spell)
                player.experience.add(Skill.Magic, definition.experience)
                player.experience.add(Skill.Smithing, smelting.exp(player, bar))
            } else {
                player.sound("superheat_fail")
            }
        }
    }
}
