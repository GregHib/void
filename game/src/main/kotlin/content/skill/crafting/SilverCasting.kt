package content.skill.crafting

import content.entity.player.dialogue.type.intEntry
import content.quest.quest
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.weakQueue

class SilverCasting : Script {

    init {
        interfaceOpened("silver_mould") { id ->
            for (row in Tables.get("silver_casting").rows()) {
                val item = row.item("product")
                val quest = row.stringOrNull("quest")
                interfaces.sendVisibility(id, row.stringId, quest == null || quest(quest) != "unstarted")
                val has = carriesItem(row.itemId)
                val mould = ItemDefinitions.get(row.itemId)
                interfaces.sendText(
                    id,
                    "${row.itemId}_text",
                    if (has) {
                        val colour = if (carriesItem("silver_bar")) "green" else "orange"
                        "<$colour>Make ${ItemDefinitions.get(item).name.toTitleCase()}"
                    } else {
                        val name = row.stringOrNull("name")
                        "<orange>You need a ${name ?: mould.name.lowercase()} to make this item."
                    },
                )
                interfaces.sendItem(id, "${row.itemId}_model", if (has) ItemDefinitions.get(item).id else mould.id)
            }
        }

        itemOnObjectOperate("silver_bar", "furnace*", arrive = false) {
            open("silver_mould")
        }

        itemOnObjectOperate(obj = "furnace*") {
            make(it.item, 1)
        }

        interfaceOption(id = "silver_mould:*_button") {
            val amount = when (it.option) {
                "Make 1" -> 1
                "Make 5" -> 5
                "Make All" -> 28
                "Make X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            make(Item(it.component.removeSuffix("_button")), amount)
        }

        interfaceClosed("silver_mould") {
            sendScript("clear_dialogues")
        }
    }

    fun Player.make(item: Item, amount: Int) {
        if (amount <= 0) {
            return
        }
        val row = Rows.getOrNull("silver_casting.${item.id}") ?: return
        val exp = row.int("xp")
        val product = row.item("product")
        val produce = row.int("amount")
        closeMenu()
        if (!inventory.contains(item.id)) {
            message("You need a ${item.def.name} in order to make a ${ItemDefinitions.get(product).name}.")
            return
        }
        if (!inventory.contains("silver_bar")) {
            message("You need a silver bar in order to make a ${ItemDefinitions.get(product).name}.")
            return
        }
        val level = row.int("level")
        if (!has(Skill.Crafting, level)) {
            return
        }
        if (!inventory.contains("silver_bar")) {
            message("You have run out of silver bars to make another ${ItemDefinitions.get(product).name}.")
            return
        }
        anim("cook_range")
        weakQueue("cast_silver", 3) {
            if (produce >= 1) {
                inventory.remove("silver_bar")
                inventory.add(product, produce)
            } else {
                inventory.replace("silver_bar", product)
            }
            exp(Skill.Crafting, exp / 10.0)
            make(item, amount - 1)
        }
    }
}
