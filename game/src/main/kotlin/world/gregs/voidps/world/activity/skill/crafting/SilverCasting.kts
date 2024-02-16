package world.gregs.voidps.world.activity.skill.crafting

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Silver
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.type.intEntry

val itemDefinitions: ItemDefinitions by inject()

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
    Item("key_mould")
)

val Item.silver: Silver?
    get() = def.getOrNull("silver_jewellery")

interfaceOpen("silver_mould") { player: Player ->
    for (mould in moulds) {
        val silver = mould.silver ?: continue
        val item = silver.item
        val quest = silver.quest
        player.interfaces.sendVisibility(id, mould.id, quest == null || player.quest(quest) != "unstarted")
        val has = player.holdsItem(mould.id)
        player.interfaces.sendText(id,
            "${mould.id}_text",
            if (has) {
                val colour = if (has && player.holdsItem("silver_bar")) "green" else "orange"
                "<$colour>Make ${itemDefinitions.get(item).name.toTitleCase()}"
            } else {
                "<orange>You need a ${silver.name ?: mould.def.name.lowercase()} to make this item."
            })
        player.interfaces.sendItem(id, "${mould.id}_model", if (has) itemDefinitions.get(item).id else mould.def.id)
    }
}

itemOnObjectOperate("silver_bar", "furnace*", arrive = false) {
    player.open("silver_mould")
}

itemOnObjectOperate(obj = "furnace*", def = "silver_jewellery") {
    player.make(item, 1)
}

interfaceOption(component = "*_button", id = "silver_mould") {
    val amount = when (option) {
        "Make 1" -> 1
        "Make 5" -> 5
        "Make All" -> 28
        "Make X" -> intEntry("Enter amount:")
        else -> return@interfaceOption
    }
    player.make(Item(component.removeSuffix("_button")), amount)
}

interfaceClose("silver_mould") { player: Player ->
    player.sendScript("clear_dialogues")
}

fun Player.make(item: Item, amount: Int) {
    if (amount <= 0) {
        return
    }
    val data = item.silver ?: return
    closeMenu()
    if (!inventory.contains(item.id)) {
        message("You need a ${item.def.name} in order to make a ${itemDefinitions.get(data.item).name}.")
        return
    }
    if (!inventory.contains("silver_bar")) {
        message("You need a silver bar in order to make a ${itemDefinitions.get(data.item).name}.")
        return
    }
    if (!has(Skill.Crafting, data.level)) {
        return
    }
    if (!inventory.contains("silver_bar")) {
        message("You have run out of silver bars to make another ${itemDefinitions.get(data.item).name}.")
        return
    }
    setAnimation("cook_range")
    weakQueue("cast_silver", 3) {
        inventory.replace("silver_bar", data.item)
        exp(Skill.Crafting, data.xp)
        make(item, amount - 1)
    }
}