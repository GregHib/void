package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Costume room storage: cape racks, magic wardrobes, toy boxes, treasure chests, fancy dress boxes and armour cases.
 * Each lists the items it can hold, selecting a stored item takes it out and selecting an item in the inventory stores it.
 */
class HouseCostumeRoom : Script {
    init {
        objectOperate("Search", CAPE_RACKS) { (target) ->
            openStorage(target.id)
        }

        objectOperate("Open", "$MAGIC_WARDROBES,$CHESTS") { (target) ->
            if (target.id.endsWith("treasure_chest")) {
                if (!inOwnHouse()) {
                    message("You can only do that in your own house.") // TODO proper message
                    return@objectOperate
                }
                choice("Take which level of Treasure Trail reward?") {
                    for (level in 1..TREASURE_TRAIL_LEVELS) {
                        option("Level $level") {
                            openStorage(target.id, "poh_costume_room_treasure_trail_${level}_inv")
                        }
                    }
                }
                return@objectOperate
            }
            openStorage(target.id, if (target.id.endsWith("magic_wardrobe")) "poh_costume_room_magic_wardrobe_inv" else null)
        }

        interfaceOpened("poh_costume_room") { id ->
            interfaces.sendText(id, "title", get("house_costume_furniture", ""))
            refresh()
        }

        interfaceClosed("poh_costume_room") {
            clear("house_costume_furniture")
            clear("house_costume_items")
        }

        interfaceOption("Close", "poh_costume_room:close") {
            closeMenu()
        }

        interfaceOption("Select", "poh_costume_room:select_*") {
            val items: List<String> = get("house_costume_items") ?: return@interfaceOption
            val item = items.getOrNull(it.component.removePrefix("select_").toInt() - 1) ?: return@interfaceOption
            val stored: List<String> = get("house_costumes") ?: emptyList()
            if (item in stored) {
                if (!inventory.add(item)) {
                    inventoryFull()
                    return@interfaceOption
                }
                set("house_costumes", stored - item)
            } else if (inventory.contains(item) && inventory.remove(item)) {
                set("house_costumes", stored + item)
            } else {
                message("You don't have that item to store.") // TODO proper message
                return@interfaceOption
            }
            refresh()
        }
    }

    private fun Player.openStorage(furniture: String, source: String? = null) {
        if (!inOwnHouse()) {
            message("You can only do that in your own house.") // TODO proper message
            return
        }
        set("house_costume_furniture", ObjectDefinitions.get(furniture).name)
        set("house_costume_items", items(source))
        open("poh_costume_room")
    }

    /**
     * The items listed by the client's [source] inventory of items for a type of storage
     * TODO cape racks, toy boxes, fancy dress boxes and armour cases, and the second page of treasure trail levels 1 and 3
     */
    private fun items(source: String?): List<String> {
        if (source == null) {
            return emptyList()
        }
        return InventoryDefinitions.get(source).ids?.filter { it != -1 }?.map { ItemDefinitions.get(it).stringId }?.take(ROWS) ?: emptyList()
    }

    private fun Player.refresh() {
        val items: List<String> = get("house_costume_items") ?: return
        val stored: List<String> = get("house_costumes") ?: emptyList()
        for (index in 1..ROWS) {
            val item = items.getOrNull(index - 1)
            val name = if (item == null) "" else ItemDefinitions.get(item).name
            interfaces.sendText("poh_costume_room", "name_$index", if (item == null || item in stored) name else "<col=808080>$name</col>")
        }
    }

    companion object {
        private const val ROWS = 30
        private const val TREASURE_TRAIL_LEVELS = 4
        private const val CAPE_RACKS = "oak_cape_rack,teak_cape_rack,mahogany_cape_rack,gilded_cape_rack,marble_cape_rack,magic_cape_rack"
        private const val MAGIC_WARDROBES = "oak_magic_wardrobe,carved_oak_magic_wardrobe,teak_magic_wardrobe,carved_teak_magic_wardrobe,mahogany_magic_wardrobe,gilded_magic_wardrobe,marble_magic_wardrobe"
        private const val CHESTS = "oak_toy_box,teak_toy_box,mahogany_toy_box,oak_treasure_chest,teak_treasure_chest,mahogany_treasure_chest," +
            "oak_fancy_dress_box,teak_fancy_dress_box,mahogany_fancy_dress_box,oak_armour_case,teak_armour_case,mahogany_armour_case"
    }
}
