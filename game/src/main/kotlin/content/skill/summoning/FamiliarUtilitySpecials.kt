package content.skill.summoning

import content.entity.player.bank.bank
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

/**
 * Utility familiar specials: send-the-familiar-to-fight, call/ambush, ground-drop foragers, and
 * banking. Combat-engage moves register as [FamiliarSpecialMoves.npc]; the rest are instant casts.
 * Pack Yak's Winter Storage picks an inventory item, so it is wired with `itemOnNPCApproach`.
 */
class FamiliarUtilitySpecials : Script {

    private val grimyHerbs = listOf(
        "grimy_guam",
        "grimy_marrentill",
        "grimy_tarromin",
        "grimy_harralander",
        "grimy_ranarr",
        "grimy_irit",
        "grimy_avantoe",
        "grimy_kwuarm",
    )
    private val fruit = listOf("orange", "banana", "lemon", "lime", "papaya_fruit")
    private val rawFish = listOf("raw_trout", "raw_salmon", "raw_cod", "raw_pike")

    init {
        // Goad / Pester - the special simply sends the familiar at the target (with a Hunter feel).
        FamiliarSpecialMoves.npc("spirit_graahk_familiar", "spirit_mosquito_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@npc false
            }
            commandFamiliarAttack(target, silent = true)
            true
        }

        // Ambush - teleport the familiar to the owner, ready to strike.
        FamiliarSpecialMoves.instant("spirit_kyatt_familiar") {
            callFollower()
            true
        }

        // Cheese Feast - the albino rat produces a cheese.
        FamiliarSpecialMoves.instant("albino_rat_familiar") {
            familiarSelfSpecial {
                if (!inventory.add("cheese")) {
                    FloorItems.add(tile, "cheese", disappearTicks = 300, owner = this)
                }
            }
        }

        // Herbcall - drop a random grimy herb at the player's feet.
        FamiliarSpecialMoves.instant("macaw_familiar") {
            dropForage(grimyHerbs[random.nextInt(grimyHerbs.size)])
        }

        // Fruitfall - drop a papaya plus a few random fruits.
        FamiliarSpecialMoves.instant("fruit_bat_familiar") {
            dropForage("papaya_fruit")
            repeat(random.nextInt(4)) {
                dropForage(fruit[random.nextInt(fruit.size)])
            }
            true
        }

        // Fish Rain - drop one or two random raw fish.
        FamiliarSpecialMoves.instant("ibis_familiar") {
            repeat(random.nextInt(2) + 1) {
                dropForage(rawFish[random.nextInt(rawFish.size)])
            }
            true
        }

        // Essence Shipment - bank all rune and pure essence carried.
        FamiliarSpecialMoves.instant("abyssal_titan_familiar") {
            val moved = bankAll("rune_essence") + bankAll("pure_essence")
            if (moved == 0) {
                message("You have no essence for your familiar to bank.")
                return@instant false
            }
            message("Your familiar banks your essence.")
            true
        }

        // Winter Storage - bank the item the player uses on the pack yak.
        itemOnNPCApproach("*", "pack_yak_familiar") { (npc, item) ->
            if (npc != follower) {
                return@itemOnNPCApproach
            }
            castFamiliarSpecial {
                if (!inventory.contains(item.id)) {
                    return@castFamiliarSpecial false
                }
                inventory.remove(item.id, 1)
                bank.add(item.id, 1)
                true
            }
        }
    }

    /** Drops [id] on the player's tile (it despawns after ~3 minutes), counting as a successful cast. */
    private fun Player.dropForage(id: String): Boolean {
        FloorItems.add(tile, id, disappearTicks = 300, owner = this)
        return true
    }

    /** Moves every [id] from the inventory to the bank, returning how many were moved. */
    private fun Player.bankAll(id: String): Int {
        val count = inventory.count(id)
        if (count > 0) {
            inventory.remove(id, count)
            bank.add(id, count)
        }
        return count
    }
}
