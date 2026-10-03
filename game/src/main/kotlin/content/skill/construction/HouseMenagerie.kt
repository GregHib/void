package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.HouseFurniture.Companion.pick
import content.skill.summoning.pet.petRowForItem
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Menagerie pet houses store pets so they don't need to be carried around and feeders feed every stored pet at once.
 * The small obelisk renews summoning points like any other obelisk.
 * TODO stored pets roaming the menagerie
 */
class HouseMenagerie : Script {
    init {
        objectOperate("Store", PET_HOUSES) {
            if (!inOwnHouse()) {
                message("You can only do that in your own house.") // TODO proper message
                return@objectOperate
            }
            choice {
                option("Store a pet") {
                    store()
                }
                option("Take a pet") {
                    take()
                }
                option("Cancel")
            }
        }

        objectOperate("Feed-all", PET_FEEDERS) {
            if (!inOwnHouse()) {
                message("You can only do that in your own house.") // TODO proper message
                return@objectOperate
            }
            feedAll()
        }
    }

    private val Player.storedPets: List<String>
        get() = get("house_pets") ?: emptyList()

    private suspend fun Player.store() {
        val pets = inventory.items.map { it.id }.distinct().filter { petRowForItem(it) != null }
        if (pets.isEmpty()) {
            message("You don't have any pets to store.") // TODO proper message
            return
        }
        val index = pick(pets.map { ItemDefinitions.get(it).name })
        if (index == -1 || !inventory.remove(pets[index])) {
            return
        }
        set("house_pets", storedPets + pets[index])
        message("You put your pet in the pet house.") // TODO proper message
    }

    private suspend fun Player.take() {
        val pets = storedPets
        if (pets.isEmpty()) {
            message("You don't have any pets stored.") // TODO proper message
            return
        }
        val index = pick(pets.map { ItemDefinitions.get(it).name })
        if (index == -1) {
            return
        }
        if (!inventory.add(pets[index])) {
            inventoryFull()
            return
        }
        set("house_pets", pets.filterIndexed { i, _ -> i != index })
    }

    /**
     * Feeds each stored pet one piece of food it eats from the inventory
     */
    private fun Player.feedAll() {
        val rows = storedPets.mapNotNull { petRowForItem(it) }
        if (rows.isEmpty()) {
            message("You don't have any pets stored.") // TODO proper message
            return
        }
        var fed = 0
        for (row in rows) {
            val food = row.itemList("food").firstOrNull { inventory.contains(it) } ?: continue
            if (!inventory.remove(food)) {
                continue
            }
            dec("pet_${row.rowId}_hunger", FEED_HUNGER_REDUCTION)
            fed++
        }
        if (fed == 0) {
            message("You don't have any food your pets will eat.") // TODO proper message
            return
        }
        anim("climb_down")
        message("You fill the feeder and your pets happily eat.") // TODO proper message
    }

    companion object {
        private const val PET_HOUSES = "oak_pet_house,teak_pet_house,mahogany_pet_house,consecrated_pet_house,desecrated_pet_house,natural_pet_house"
        private const val PET_FEEDERS = "oak_pet_feeder,teak_pet_feeder,mahogany_pet_feeder"

        // Same as feeding a pet by hand
        private const val FEED_HUNGER_REDUCTION = 1500
    }
}
