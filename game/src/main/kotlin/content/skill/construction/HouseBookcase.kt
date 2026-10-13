package content.skill.construction

import content.quest.questCompleted
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

/**
 * Bookcases, built in parlours, studies and quest halls, hold every book the player has unlocked, which mostly come from completing quests.
 * The client builds the list of books itself from the unlocked books sent in the house_books varcs with a Take and Examine option for each.
 */
class HouseBookcase : Script {
    init {
        objectOperate("Search", "wooden_bookcase,oak_bookcase,mahogany_bookcase") {
            if (!inOwnHouse()) {
                message("You can only do that in your own house.") // TODO proper message
                return@objectOperate
            }
            open("poh_bookcase")
        }

        /*
            You can only store stuff in your own house.
         */

        interfaceOpened("poh_bookcase") { id ->
            interfaces.sendText(id, "title", "Bookcase")
            interfaceOptions.send(id, "books")
            interfaceOptions.unlockAll(id, "books", 0 until 96)
            sendBooks()
        }

        interfaceOption("Close", "poh_bookcase:close") {
            closeMenu()
        }

        interfaceOption("Take", "poh_bookcase:books") { (item, itemSlot) ->
            val slot = if (item.isEmpty()) itemSlot else slot(item.id) ?: return@interfaceOption
            val book = book(slot) ?: return@interfaceOption
            if (!isUnlocked(slot)) {
                return@interfaceOption
            }
            if (inventory.contains(book)) {
                message("You're already carrying a copy of that book.")
                return@interfaceOption
            }
            if (!inventory.add(book)) {
                inventoryFull("to do that")
            }
        }
    }

    private fun bookStruct(slot: Int): StructDefinition? {
        val struct = EnumDefinitions.get("poh_bookcase").intOrNull(slot) ?: return null
        return StructDefinitions.getOrNull(struct)
    }

    private fun book(slot: Int): String? {
        val item = bookStruct(slot)?.getOrNull<Int>(Params.BOOK_ITEM) ?: return null
        return ItemDefinitions.get(item).stringId
    }

    /**
     * Position of [book] in the book enum, which is the bit the client reads to show the book
     */
    private fun slot(book: String): Int? = slots().firstOrNull { book(it) == book }

    private fun slots(): List<Int> = EnumDefinitions.get("poh_bookcase").map?.keys?.toList() ?: emptyList()

    /**
     * Whether the quest that unlocks the book at [slot] is complete, books without a quest are always unlocked
     */
    private fun Player.isUnlocked(slot: Int): Boolean {
        val struct = bookStruct(slot) ?: return false
        val questStruct = struct.getOrNull(Params.BOOK_QUEST) ?: -1
        if (questStruct == -1) {
            return true
        }
        val id = EnumDefinitions.get("item_info_quests").getKey(questStruct)
        if (id == 147) {
            return questCompleted("unstable_foundations")
        }
        val quest = get<QuestDefinitions>().getOrNull(id)?.stringId ?: return false
        return questCompleted(quest)
    }

    /**
     * Sends which books are unlocked so the client can rebuild the list
     */
    private fun Player.sendBooks() {
        val unlocked = slots().filter { isUnlocked(it) }.mapNotNull { book(it) }
        set("house_books_1", unlocked)
        set("house_books_2", unlocked)
        set("house_books_3", unlocked)
    }
}
