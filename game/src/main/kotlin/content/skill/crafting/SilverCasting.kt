package content.skill.crafting

import content.entity.player.dialogue.type.intEntry
import content.quest.quest
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.queue.weakQueue

class SilverCasting : Script {

    val moulds = listOf(
        Item("holy_mould"),
        Item("sickle_mould"),
        Item("tiara_mould"),
        Item("demonic_sigil_mould"),
        Item("chain_link_mould"),
        Item("unholy_mould"),
        Item("conductor_mould"),
        Item("rod_clay_mould"),
        Item("bolt_mould"),
        Item("key_mould"),
    )

    init {
        interfaceOpened("silver_mould") { id ->
            for (mould in moulds) {
                EnumDefinitions.intOrNull("silver_jewellery_xp", mould.id) ?: continue
                val item = EnumDefinitions.string("silver_jewellery_item", mould.id)
                val quest = EnumDefinitions.stringOrNull("silver_jewellery_quest", mould.id)
                interfaces.sendVisibility(id, mould.id, quest == null || quest(quest) != "unstarted")
                val has = carriesItem(mould.id)
                interfaces.sendText(
                    id,
                    "${mould.id}_text",
                    if (has) {
                        val colour = if (carriesItem("silver_bar")) "green" else "orange"
                        "<$colour>Make ${ItemDefinitions.get(item).name.toTitleCase()}"
                    } else {
                        val name = EnumDefinitions.stringOrNull("silver_jewellery_name", mould.id)
                        "<orange>You need a ${name ?: mould.def.name.lowercase()} to make this item."
                    },
                )
                interfaces.sendItem(id, "${mould.id}_model", if (has) ItemDefinitions.get(item).id else mould.def.id)
            }
        }

        itemOnObjectOperate("silver_bar", "furnace*", arrive = false) {
            open("silver_mould")
        }

        itemOnObjectOperate(obj = "furnace*") {
            if (!it.item.def.contains("silver_jewellery")) {
                return@itemOnObjectOperate
            }
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
        val exp = EnumDefinitions.intOrNull("silver_jewellery_xp", item.id) ?: return
        val product = EnumDefinitions.string("silver_jewellery_item", item.id)
        val produce = EnumDefinitions.int("silver_jewellery_amount", item.id)
        closeMenu()
        if (!inventory.contains(item.id)) {
            message("You need a ${item.def.name} in order to make a ${ItemDefinitions.get(product).name}.")
            return
        }
        if (!inventory.contains("silver_bar")) {
            message("You need a silver bar in order to make a ${ItemDefinitions.get(product).name}.")
            return
        }
        val level = EnumDefinitions.int("silver_jewellery_level", item.id)
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
