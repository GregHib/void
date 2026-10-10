package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendInventoryItems
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

/**
 * Costume room storage: cape racks, magic wardrobes, toy boxes, treasure chests, fancy dress boxes and armour cases.
 * Each lists the items it can hold, selecting a stored item takes it out and selecting an item in the inventory stores it.
 * https://youtu.be/tOWlmQD0Zks?t=301
 */
class HouseCostumeRoom : Script {
    init {
        objectOperate("Search", CAPE_RACKS) { (target) ->
            openStorage(target.id, "cape_rack")
        }

        objectOperate("Open", "$MAGIC_WARDROBES,$CHESTS") { (target) ->
            if (target.id.endsWith("treasure_chest")) {
                if (!inOwnHouse()) {
                    message("You can only do that in your own house.") // TODO proper message
                    return@objectOperate
                }
                choice("Take which level of Treasure Trail reward?") {
                    for (level in 1..4) {
                        option("Level $level") {
                            openStorage(target.id, "treasure_trail_$level")
                        }
                    }
                }
                return@objectOperate
            }
            val key = STORAGE.first { target.id.endsWith(it) }
            openStorage(target.id, key)
        }

        interfaceOpened("poh_costume_room") { id ->
            interfaces.sendText(id, "title", get("house_costume_furniture", ""))
            val scrollbar = InterfaceDefinitions.getComponent(id, "scrollbar")!!.id
            val scroll = InterfaceDefinitions.getComponent(id, "scroll")!!.id
            sendScript("scrollbar_vertical", scrollbar, scroll)
            sendScript("set_scroll_height", scrollbar, scroll, ROWS * ROW_HEIGHT, 0)
            val items = InterfaceDefinitions.getComponent(id, "items")!!.id
            sendScript("interface_inv_init", items, InventoryDefinitions.get("poh_costume_menu_inv").id, 1, ROWS, 0, -1, "", "", "", "", "")
            refresh()
        }

        interfaceClosed("poh_costume_room") {
            clear("house_costume_furniture")
            clear("house_costume_storage")
            clear("house_costume_page")
        }

        interfaceOption("Close", "poh_costume_room:close") {
            closeMenu()
        }

        interfaceOption("Select", "poh_costume_room:select_*") {
            val items = slots(get("house_costume_storage") ?: return@interfaceOption)
            val page: Int = get("house_costume_page") ?: 0
            val row = it.component.removePrefix("select_").toInt() - 1
            if (row == pageRow(items, page)) {
                set("house_costume_page", if (hasNextPage(items, page)) page + 1 else 0)
                val scrollbar = InterfaceDefinitions.getComponent(it.id, "scrollbar")!!.id
                val scroll = InterfaceDefinitions.getComponent(it.id, "scroll")!!.id
                sendScript("scrollbar_resize", scrollbar, scroll, 0) // Scroll to top
                refresh()
                return@interfaceOption
            }
            val slot = pageItems(items, page).getOrNull(row) ?: return@interfaceOption
            val item = slot.item
            val pieces = pieces(slot)
            if (containsVarbit(slot.source, item)) {
                if (inventory.spaces < pieces.size || !inventory.add(*pieces.toTypedArray())) {
                    inventoryFull()
                    return@interfaceOption
                }
                removeVarbit(slot.source, item)
            } else if (inventory.remove(*pieces.toTypedArray())) {
                addVarbit(slot.source, item)
            } else {
                message(if (pieces.size > 1) "You don't have the full set to store." else "You don't have that item to store.") // TODO proper message
                return@interfaceOption
            }
            refresh()
        }
    }

    private fun Player.openStorage(furniture: String, key: String) {
        if (!inOwnHouse()) {
            message("You can only do that in your own house.") // TODO proper message
            return
        }
        set("house_costume_furniture", ObjectDefinitions.get(furniture).name)
        set("house_costume_storage", key)
        set("house_costume_page", 0)
        open("poh_costume_room")
    }

    /**
     * An item shown on the interface, [real] is the matching item in the treasure trail check inventories.
     */
    private class Slot(val item: String, val real: Int?, val source: String)

    /**
     * The items shown for a type of [storage] in the order of their inventories
     */
    private fun slots(storage: String): List<Slot> {
        val slots = ArrayList<Slot>()
        if (storage == "toy_box") {
            Tables.itemList("house_toy_box.page.items").mapTo(slots) { Slot(it, null, "poh_toy_box") }
            Tables.itemList("house_toy_box_page2.page.items").mapTo(slots) { Slot(it, null, "poh_toy_box_page2") }
        }
        for ((source, check) in SOURCES[storage] ?: emptyList()) {
            val ids = InventoryDefinitions.get(source).ids ?: continue
            val real = check?.let { InventoryDefinitions.get(it).ids }
            for ((index, id) in ids.withIndex()) {
                if (id == -1 || ItemDefinitions.get(id).stringId in PAGE_ITEMS) {
                    continue
                }
                slots.add(Slot(ItemDefinitions.get(id).stringId, real?.getOrNull(index), source))
            }
        }
        return slots
    }

    /**
     * When there are more items than rows the last row is used to switch between pages
     */
    private fun pageItems(items: List<Slot>, page: Int): List<Slot> {
        if (items.size <= ROWS) {
            return items
        }
        return items.drop(page * (ROWS - 1)).take(ROWS - 1)
    }

    /**
     * The row of the "More" or "Back" button, "More" is always last and "Back" follows the final item
     */
    private fun pageRow(items: List<Slot>, page: Int): Int? {
        if (items.size <= ROWS) {
            return null
        }
        return if (hasNextPage(items, page)) ROWS - 1 else pageItems(items, page).size
    }

    /**
     * The items stored and taken for a [slot].
     * Sets list their pieces, then the treasure trail check item is used, otherwise the "_poh" display version of an item stands in for the real one.
     */
    private fun pieces(slot: Slot): List<String> {
        Rows.getOrNull("house_costume_sets.${slot.item}")?.let { return it.itemList("pieces") }
        if (slot.real != null && slot.real != -1) {
            return listOf(ItemDefinitions.get(slot.real).stringId)
        }
        return listOf(slot.item.removeSuffix("_poh"))
    }

    private fun name(item: String): String = Rows.getOrNull("house_costume_sets.$item")?.string("name") ?: ItemDefinitions.get(item).name

    private fun hasNextPage(items: List<Slot>, page: Int) = (page + 1) * (ROWS - 1) < items.size

    private fun Player.refresh() {
        val items = slots(get("house_costume_storage") ?: return)
        val page: Int = get("house_costume_page") ?: 0
        val visible = pageItems(items, page)
        sendInventoryItems(
            inventory = InventoryDefinitions.get("poh_costume_menu_inv").id,
            size = ROWS,
            items = IntArray(ROWS * 2) { index ->
                val row = index.rem(ROWS)
                val item = visible.getOrNull(row)?.item ?: if (row == pageRow(items, page)) (if (hasNextPage(items, page)) "more" else "back") else null
                when {
                    item == null -> if (index < ROWS) -1 else 0
                    index < ROWS -> ItemDefinitions.get(item).id
                    else -> 1
                }
            },
            primary = false,
        )
        for (index in 1..ROWS) {
            val slot = visible.getOrNull(index - 1)
            val item = slot?.item
            val text = when {
                item != null -> name(item)
                index - 1 == pageRow(items, page) -> if (hasNextPage(items, page)) "More..." else "Back"
                else -> ""
            }
            interfaces.sendText("poh_costume_room", "name_$index", text)
            interfaces.sendVisibility("poh_costume_room", "overlay_$index", slot != null && !containsVarbit(slot.source, slot.item))
        }
    }

    companion object {
        private const val ROWS = 30
        private const val ROW_HEIGHT = 62
        private val STORAGE = listOf("magic_wardrobe", "toy_box", "fancy_dress_box", "armour_case")
        private val PAGE_ITEMS = setOf("more", "back")

        /**
         * Inventories listing the items each storage holds, with the inventory of real items for treasure trail rewards
         */
        private val SOURCES = mapOf(
            "cape_rack" to listOf("poh_costume_room_capes_inv" to null, "poh_costume_room_capes_inv_page2" to null),
            "magic_wardrobe" to listOf("poh_costume_room_magic_wardrobe_inv" to null),
            "fancy_dress_box" to listOf("poh_costume_room_ame_inv" to null),
            "armour_case" to listOf("poh_costume_room_armour_inv" to null, "poh_costume_room_armour_inv_page2" to null),
            "treasure_trail_1" to listOf("poh_costume_room_treasure_trail_1_inv" to "poh_costume_room_treasure_trail_1_inv_check", "poh_costume_room_treasure_trail_1a_inv" to "poh_costume_room_treasure_trail_1a_inv_check"),
            "treasure_trail_2" to listOf("poh_costume_room_treasure_trail_2_inv" to "poh_costume_room_treasure_trail_2_inv_check"),
            "treasure_trail_3" to listOf("poh_costume_room_treasure_trail_3_inv" to "poh_costume_room_treasure_trail_3_inv_check", "poh_costume_room_treasure_trail_3a_inv" to "poh_costume_room_treasure_trail_3a_inv_check"),
            "treasure_trail_4" to listOf("poh_costume_room_treasure_trail_4_inv" to "poh_costume_room_treasure_trail_4_inv_check"),
        )
        private const val CAPE_RACKS = "oak_cape_rack,teak_cape_rack,mahogany_cape_rack,gilded_cape_rack,marble_cape_rack,magic_cape_rack"
        private const val MAGIC_WARDROBES = "oak_magic_wardrobe,carved_oak_magic_wardrobe,teak_magic_wardrobe,carved_teak_magic_wardrobe,mahogany_magic_wardrobe,gilded_magic_wardrobe,marble_magic_wardrobe"
        private const val CHESTS = "oak_toy_box,teak_toy_box,mahogany_toy_box,oak_treasure_chest,teak_treasure_chest,mahogany_treasure_chest," +
            "oak_fancy_dress_box,teak_fancy_dress_box,mahogany_fancy_dress_box,oak_armour_case,teak_armour_case,mahogany_armour_case"
    }
}
