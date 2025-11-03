package content.skill.constitution.drink

import content.entity.effect.toxin.poisoned
import content.entity.player.dialogue.type.item
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.entity.player.inv.inventoryItem
import content.skill.constitution.consume
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddCharge.charge
import world.gregs.voidps.engine.inv.transact.operation.RemoveCharge.discharge
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.type.random

class Tea : Script {

    init {
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
            item(
                "tea_flask",
                400,
                when (charges) {
                    0 -> "There's no tea in this flask."
                    1 -> "There is one serving of tea in this flask."
                    else -> "There is $charges servings of tea in this flask."
                },
            )
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

        itemOnItem("tea_flask", "empty_cup") { _, toItem, fromSlot, toSlot ->
            val success = inventory.transaction {
                discharge(fromSlot, 1)
                replace(toSlot, toItem.id, "cup_of_tea")
            }
            if (success) {
                message("You fill the cup with tea.")
            } else {
                message("There's nothing left in the flask.")
            }
        }

        itemOnItem("cup_of_tea", "tea_flask") { fromItem, _, fromSlot, toSlot ->
            val success = inventory.transaction {
                replace(fromSlot, fromItem.id, "empty_cup")
                charge(toSlot, 1)
            }
            if (success) {
                message("You add the tea to the flask.")
            } else {
                message("The flask is full!")
            }
        }
    }
}
