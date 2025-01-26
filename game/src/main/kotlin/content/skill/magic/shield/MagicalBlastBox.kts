package content.skill.magic.shield

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.inv.transact.discharge
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.player.dialogue.type.choice
import content.entity.combat.hit.combatAttack
import content.entity.player.inv.inventoryItem
import kotlin.math.min

inventoryItem("Check*", "magical_blastbox*", "inventory") {
    val charges = player.inventory.charges(player, slot)
    val dungeoneering = if (item.id == "magical_blastbox") "" else "_dungeoneering"
    val blast = player["magical_blastbox_mode$dungeoneering", false]
    choice("The box is currently charged with $charges ${if (blast) "Blast" else "Bolt"} ${"spell".plural(charges)}.") {
        option("I want to empty the ${if (blast) "Blast" else "Bolt"} spells.", filter = { charges > 0 }) { // TODO proper message
            if (emptyRunes(player, blast, dungeoneering, slot, charges)) {
                player.message("You empty the box of ${if (blast) "Blast" else "Bolt"} spells.") // TODO proper message
            } else {
                player.inventoryFull()
            }
        }
        option("I do not wish to change the box settings.", filter = { charges == 0 })
        option("Switch to ${if (blast) "Bolt" else "Blast"}.") {
            if (charges == 0 || emptyRunes(player, blast, dungeoneering, slot, charges)) {
                val blastMode = player.toggle("magical_blastbox_mode$dungeoneering")
                player.message("This box is set to be charged with ${if (blastMode) "Blast" else "Bolt"} spells.")
            } else {
                player.inventoryFull()
            }
        }
    }
}

fun emptyRunes(player: Player, blast: Boolean, dungeoneering: String, slot: Int, charges: Int): Boolean {
    return player.inventory.transaction {
        if (blast) {
            add("air_rune$dungeoneering", charges * 3)
            add("death_rune$dungeoneering", charges)
        } else {
            add("air_rune$dungeoneering", charges * 2)
            add("chaos_rune$dungeoneering", charges)
        }
        discharge(player, slot, amount = charges)
    }
}

inventoryItem("Check-charges", "magical_blastbox*", "worn_equipment") {
    val blast = player["magical_blastbox_mode", false]
    val charges = player.equipment.charges(player, EquipSlot.Shield.index)
    player.message("The box is currently charged with $charges ${if (blast) "Blast" else "Bolt"} ${"spell".plural(charges)}.") // TODO proper message
}

playerSpawn { player ->
    val box = player.equipped(EquipSlot.Shield).id
    if (box.startsWith("magical_blastbox")) {
        updateCharges(player, EquipSlot.Shield.index, box != "magical_blastbox")
    } else {
        setCharges(player, 0, box != "magical_blastbox")
    }
}

itemAdded("magical_blastbox*", EquipSlot.Shield, "worn_equipment") { player ->
    updateCharges(player, index, item.id != "magical_blastbox")
}

fun updateCharges(player: Player, index: Int, dungeoneering: Boolean) {
    val charges = player.equipment.charges(player, index)
    setCharges(player, charges, dungeoneering)
}

fun setCharges(player: Player, charges: Int, dungeoneering: Boolean) {
    val type = if (player["magical_blastbox_mode${if (dungeoneering) "_dungeoneering" else ""}", false]) "blast" else "bolt"
    player["magical_blastbox_$type"] = charges
}

combatAttack(spell = "*_bolt") { player ->
    val box = player.equipped(EquipSlot.Shield).id
    if (box.startsWith("magical_blastbox")) {
        updateCharges(player, EquipSlot.Shield.index, box != "magical_blastbox")
    }
}

combatAttack(spell = "*_blast") { player ->
    val box = player.equipped(EquipSlot.Shield).id
    if (box.startsWith("magical_blastbox")) {
        updateCharges(player, EquipSlot.Shield.index, box != "magical_blastbox")
    }
}

itemRemoved("magical_blastbox*", EquipSlot.Shield, "worn_equipment") { player ->
    setCharges(player, 0, fromItem.id != "magical_blastbox")
}

inventoryItem("Charge", "magical_blastbox*", "inventory") {
    charge(player, item, slot)
}

itemOnItem("air_rune", "magical_blastbox*", "inventory") {
    charge(it, toItem, toSlot)
}

itemOnItem("chaos_rune", "magical_blastbox*", "inventory") {
    charge(it, toItem, toSlot)
}

itemOnItem("death_rune", "magical_blastbox*", "inventory") {
    charge(it, toItem, toSlot)
}

fun charge(player: Player, item: Item, slot: Int) {
    val dungeoneering = if (item.id == "magical_blastbox") "" else "_dungeoneering"
    val blast = player["magical_blastbox_mode$dungeoneering", false]
    val maximum: Int = item.def.getOrNull("charges_max") ?: item.def.getOrNull("charges") ?: return
    val charges = player.inventory.charges(player, slot)
    player.inventory.transaction {
        val actual = (if (blast) {
            min(inventory.count("air_rune$dungeoneering") / 3, inventory.count("death_rune$dungeoneering"))
        } else {
            min(inventory.count("air_rune$dungeoneering") / 2, inventory.count("chaos_rune$dungeoneering"))
        }).coerceAtMost(maximum - charges)

        remove("air_rune$dungeoneering", actual * if (blast) 3 else 2)
        remove("${if (blast) "death_rune" else "chaos_rune"}$dungeoneering", actual)
        charge(player, slot, actual)
    }
    if (player.inventory.transaction.error != TransactionError.None) {
        player.message("You don't have enough runes to charge the box.") // TODO proper message
    }
}