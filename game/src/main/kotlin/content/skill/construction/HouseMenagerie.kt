package content.skill.construction

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.summoning.pet.petRowForItem
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit

/**
 * Menagerie pet houses store pets so they don't need to be carried around and feeders feed every stored pet at once.
 * Pets are moved between the pet house and the inventory like any other storage interface.
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
            movePets()
            open("pet_house")
        }

        interfaceOpened("pet_house") { id ->
            open("pet_house_side")
            tab(Tab.Inventory)
            interfaceOptions.send(id, "items")
            interfaceOptions.unlockAll(id, "items", 0 until CAPACITY)
            sendInventory(petHouse)
            sendInventory(inventory)
        }

        interfaceClosed("pet_house") {
            close("pet_house_side")
        }

        interfaceOption("Close", "pet_house:close") {
            closeMenu()
        }

        interfaceOption("Take", "pet_house:items") { (item) ->
            val pets = petHouse
            pets.transaction {
                moveToLimit(item.id, 1, this@interfaceOption.inventory)
            }
            when (pets.transaction.error) {
                is TransactionError.Full -> inventoryFull()
                else -> sync()
            }
        }

        interfaceOption(id = "pet_house_side:inventory") { (item, _, option) ->
            if (!hasOpen("pet_house") || !option.startsWith("Store")) {
                return@interfaceOption
            }
            if (petRowForItem(item.id) == null) {
                message("You can only store pets in the pet house.") // TODO proper message
                return@interfaceOption
            }
            val pets = petHouse
            if (pets.items.count { it.isNotEmpty() } >= CAPACITY) {
                message("Your pet house is full.") // TODO proper message
                return@interfaceOption
            }
            if (inventory.remove(item.id)) {
                pets.add(item.id)
                sync()
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

    private val Player.petHouse: Inventory
        get() = inventories.inventory("pet_house")

    /**
     * Moves pets stored before pet houses had an interface into the pet house inventory
     */
    private fun Player.movePets() {
        val stored: List<String> = remove("house_pets") ?: return
        for (pet in stored) {
            petHouse.add(pet)
        }
    }

    private fun Player.sync() {
        sendInventory(petHouse)
        sendInventory(inventory)
    }

    /**
     * Feeds each stored pet one piece of food it eats from the inventory
     */
    private fun Player.feedAll() {
        movePets()
        val rows = petHouse.items.filter { it.isNotEmpty() }.mapNotNull { petRowForItem(it.id) }
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

        // Slots shown by the pet house interface
        private const val CAPACITY = 40

        // Same as feeding a pet by hand
        private const val FEED_HUNGER_REDUCTION = 1500
    }
}
