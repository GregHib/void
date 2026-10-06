package content.skill.construction

import content.entity.player.dialogue.type.statement
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.roomZone
import content.skill.summoning.pet.getPetHunger
import content.skill.summoning.pet.itemFor
import content.skill.summoning.pet.npcFor
import content.skill.summoning.pet.pet
import content.skill.summoning.pet.petRowForItem
import content.skill.summoning.pet.petRowForNpc
import content.skill.summoning.pet.sendPetDetailsStats
import content.skill.summoning.pet.stageForItem
import content.skill.summoning.pet.stageForNpc
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.type.Zone

/**
 * Menagerie pet houses store pets so they don't need to be carried around and feeders keep every stored pet fed and refill the hunger of the pet following the player.
 * Pets are moved between the pet house and the inventory like any other storage interface.
 * The small obelisk renews summoning points like any other obelisk.
 * Stored pets roam the menagerie, using versions without the pick-up option.
 */
class HouseMenagerie : Script {
    init {
        objectOperate("Store", PET_HOUSES) {
            if (!inOwnHouse()) {
                message("You can only do that in your own house.") // TODO proper message
                return@objectOperate
            }
            if (get("house_build_mode", false)) {
                // https://youtu.be/7VRYU4UqbYo?t=400
                statement("You cannot do that in building mode.")
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
                else -> {
                    despawnHousePet(item.id)
                    sync()
                    // https://youtu.be/X0FDOfY6Kb4?t=77
                    message("You take your pet out of the menagerie.")
                }
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
                // The feeder keeps stored pets fed
                petRowForItem(item.id)?.let { row -> if (get("pet_active_item", "") != item.id) feed(row.rowId) }
                spawnHousePet(item.id)
                sync()
                // https://youtu.be/7VRYU4UqbYo?t=519
                message("Your pet clambers into the pet house.")
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

    /**
     * Moves pets stored before pet houses had an interface into the pet house inventory
     */
    private fun Player.movePets() {
        val stored: List<String> = remove("house_pets") ?: return
        for (pet in stored) {
            petHouse.add(pet)
        }
    }

    private fun Player.feed(pet: String) {
        clear("pet_${pet}_hunger")
        clear("pet_${pet}_warn")
    }

    private fun Player.sync() {
        sendInventory(petHouse)
        sendInventory(inventory)
    }

    /**
     * Fills the hunger of the pet currently following the player.
     * Stored pets don't get hungry so there is nothing to do for them.
     */
    private fun Player.feedAll() {
        val item = get("pet_active_item", "")
        val row = if (item.isBlank()) null else petRowForItem(item)
        if (row == null || pet == null) {
            message("You have no pet following you.") // TODO proper message
            return
        }
        if (getPetHunger(row.rowId) <= 0) {
            message("Your pet isn't hungry.") // TODO proper message
            return
        }
        feed(row.rowId)
        sendPetDetailsStats()
        anim("climb_down")
        message("You fill the feeder and your pet happily eats.") // TODO proper message
    }

    companion object {
        private val Player.petHouse: Inventory
            get() = inventories.inventory("pet_house")

        /**
         * Pets which are roaming the house, one for each pet stored
         */
        private val Player.housePets: MutableList<NPC>
            get() = get("house_pet_npcs") ?: mutableListOf<NPC>().also { set("house_pet_npcs", it) }

        /**
         * The pet item a roaming pet came from, found by going back through the pet's menagerie version to its normal npc
         */
        private fun NPC.petItem(): String? {
            val pet = Tables.get("menagerie_pets").rows().firstOrNull { it.npcOrNull("roaming") == id } ?: return null
            val row = petRowForNpc(pet.rowId) ?: return null
            return row.stageForNpc(pet.rowId)?.let { row.itemFor(it) }
        }

        /**
         * Spawns all the pets stored in the pet house into the menagerie of the house starting at [base]
         */
        fun Player.spawnHousePets(base: Zone) {
            despawnHousePets()
            for (pet in petHouse.items) {
                if (pet.isNotEmpty()) {
                    spawnHousePet(pet.id, base)
                }
            }
        }

        private fun Player.spawnHousePet(item: String, base: Zone? = houseBase()) {
            if (base == null || !inOwnHouse() || get("house_build_mode", false)) {
                return
            }
            val row = petRowForItem(item) ?: return
            val npc = row.stageForItem(item)?.let { row.npcFor(it) } ?: return
            val roaming = Rows.getOrNull("menagerie_pets.$npc")?.npcOrNull("roaming") ?: return
            val rooms = houseRoomIds.indices.filter { houseRoomIds[it] == "menagerie" }.map { roomZone(base, houseRoomPositions[it]) }
            val tiles = rooms.flatMap { room -> room.toCuboid().filter { !Collisions.check(it, BLOCKED) } }
            val tile = tiles.randomOrNull() ?: return
            val spawned = NPCs.add(roaming, tile)
            spawned.mode = Wander(spawned, tile)
            housePets.add(spawned)
        }

        private fun Player.despawnHousePet(item: String) {
            val pets = housePets
            val index = pets.indexOfFirst { it.petItem() == item }
            if (index == -1) {
                return
            }
            NPCs.remove(pets.removeAt(index))
        }

        /**
         * Removes every pet roaming the house
         */
        fun Player.despawnHousePets() {
            val pets: MutableList<NPC> = remove("house_pet_npcs") ?: return
            for (npc in pets) {
                NPCs.remove(npc)
            }
        }

        private const val BLOCKED = CollisionFlag.FLOOR or CollisionFlag.FLOOR_DECORATION or CollisionFlag.OBJECT
        private const val PET_HOUSES = "oak_pet_house,teak_pet_house,mahogany_pet_house,consecrated_pet_house,desecrated_pet_house,natural_pet_house"
        private const val PET_FEEDERS = "oak_pet_feeder,teak_pet_feeder,mahogany_pet_feeder"

        // Most pets which can be stored
        private const val CAPACITY = 15
    }
}
