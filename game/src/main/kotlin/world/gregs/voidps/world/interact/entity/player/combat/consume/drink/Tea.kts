package world.gregs.voidps.world.interact.entity.player.combat.consume.drink

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnItem
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddCharge.charge
import world.gregs.voidps.engine.inv.transact.operation.RemoveCharge.discharge
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.type.random
import content.entity.player.dialogue.type.item
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem
import world.gregs.voidps.world.interact.entity.player.toxin.poisoned

consume("cup_of_tea") { player ->
    player.levels.boost(Skill.Attack, 3)
    player.levels.restore(Skill.Constitution, 30)
}

consume("guthix_rest_4", "guthix_rest_3", "guthix_rest_2", "guthix_rest_1") { player ->
    if (player.poisoned) {
        player["poison_damage"] = player["poison_damage", 0] - 10
    }
    player.runEnergy += (MAX_RUN_ENERGY / 100) * 5
    val range: IntRange = item.def.getOrNull("heals") ?: return@consume
    val amount = range.random(random)
    player.levels.boost(Skill.Constitution, amount, maximum = 50)
    cancel()
}

consume("nettle_tea") { player ->
    player.runEnergy = (MAX_RUN_ENERGY / 100) * 5
    player.levels.restore(Skill.Constitution, 30)
}

inventoryItem("Look-in", "tea_flask") {
    val charges = player.inventory.charges(player, slot)
    item("tea_flask", 400, when (charges) {
        0 -> "There's no tea in this flask."
        1 -> "There is one serving of tea in this flask."
        else -> "There is $charges servings of tea in this flask."
    })
}

inventoryItem("Drink", "tea_flask") {
    if (!player.inventory.discharge(player, slot)) {
        player.message("There's nothing left in the flask.")
        return@inventoryItem
    }

    player.say("Ahhh, tea is so refreshing!")
    player.levels.boost(Skill.Attack, 3)
    player.levels.restore(Skill.Constitution, 30)
    player.message("You take a drink from the flask...")
}

itemOnItem("tea_flask", "empty_cup") { player ->
    val success = player.inventory.transaction {
        discharge(fromSlot, 1)
        replace(toSlot, toItem.id, "cup_of_tea")
    }
    if (success) {
        player.message("You fill the cup with tea.")
    } else {
        player.message("There's nothing left in the flask.")
    }
}

itemOnItem("cup_of_tea", "tea_flask") { player ->
    val success = player.inventory.transaction {
        replace(fromSlot, fromItem.id, "empty_cup")
        charge(toSlot, 1)
    }
    if (success) {
        player.message("You add the tea to the flask.")
    } else {
        player.message("The flask is full!")
    }
}