package content.skill.smithing

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
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
import content.skill.magic.spell.Spell.removeItems
import content.entity.sound.playSound

val spellDefinitions: SpellDefinitions by inject()
val itemDefinitions: ItemDefinitions by inject()

itemOnItem(fromInterface = "modern_spellbook", fromComponent = "superheat_item") { player ->
    if (!toItem.id.endsWith("_ore")) {
        player.message("You need to cast superheat item on ore.")
        return@itemOnItem
    }
    var bar = toItem.id.replace("_ore", "_bar")
    if (bar == "iron_bar" && player.inventory.count("coal") >= 2) {
        bar = "steel_bar"
    }
    val smelting: Smelting = itemDefinitions.get(bar)["smelting"]
    if (!player.has(Skill.Smithing, smelting.level, message = true)) {
        return@itemOnItem
    }
    val spell = fromComponent
    player.inventory.transaction {
        removeItems(player, spell)
        remove(smelting.items)
        add(bar)
    }
    if (player.inventory.transaction.error == TransactionError.None) {
        player.playSound("superheat_all")
        player.anim(spell)
        player.gfx(spell)
        val definition = spellDefinitions.get(spell)
        player.experience.add(Skill.Magic, definition.experience)
        player.experience.add(Skill.Smithing, smelting.exp(player, bar))
    }
}