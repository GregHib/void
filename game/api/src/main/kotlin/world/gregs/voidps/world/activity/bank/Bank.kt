package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.holdsItem

val Player.bank: Inventory
    get() = inventories.inventory("bank")

fun Player.ownsItem(id: String) = holdsItem(id) || bank.contains(id)

fun Player.ownsItem(id: String, amount: Int) = holdsItem(id, amount) || bank.contains(id, amount)

val Item.isNote: Boolean
    get() = def.notedTemplateId != -1

val Item.noted: Item?
    get() = if (def.noteId != -1) {
        val definition = get<ItemDefinitions>().get(def.noteId)
        copy(id = definition.stringId)
    } else if (def.notedTemplateId != -1) {
        null
    } else {
        this
    }
