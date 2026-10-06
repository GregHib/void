package content.skill.construction

import content.quest.questCompleted
import content.skill.construction.House.Companion.inOwnHouse
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.contains

/**
 * Bookcases, built in parlours, studies and quest halls, hold every book the player has unlocked, which mostly come from completing quests.
 * The client builds the list of books itself from the unlocked books bit set sent in [BOOK_VARCS] with a Take and Examine option for each.
 */
class HouseBookcase : Script {
    init {
        objectOperate("Search", BOOKCASES) {
            if (!inOwnHouse()) {
                message("You can only do that in your own house.") // TODO proper message
                return@objectOperate
            }
            open("poh_bookcase")
        }

        interfaceOpened("poh_bookcase") { id ->
            interfaces.sendText(id, "title", "Bookcase")
            interfaceOptions.send(id, "books")
            interfaceOptions.unlockAll(id, "books", 0 until BOOK_SLOTS)
            sendBooks()
        }

        interfaceOption("Close", "poh_bookcase:close") {
            closeMenu()
        }

        interfaceOption("Take", "poh_bookcase:books") { (item, itemSlot) ->
            // The client builds the list itself so the book is either sent as the item or by its position in the book enum
            val book = (if (item.isEmpty()) books[itemSlot] else item.id) ?: return@interfaceOption
            val slot = slots[book] ?: return@interfaceOption
            if (!isUnlocked(slot)) {
                return@interfaceOption
            }
            if (inventory.contains(book)) {
                message("You already have that book.") // TODO proper message
                return@interfaceOption
            }
            if (!inventory.add(book)) {
                inventoryFull()
            }
        }
    }

    /**
     * The book at each position in the book enum
     */
    private val books: Map<Int, String> by lazy { slots.entries.associate { (book, slot) -> slot to book } }

    /**
     * Quests which have no definition of their own to look up the name of
     */
    private val unnamedQuests = mapOf(147 to "unstable_foundations")

    /**
     * Quest each book slot is unlocked by, books without a quest are always unlocked
     */
    private val requirements: Map<Int, String?> by lazy {
        val quests = get<QuestDefinitions>()
        val questIds = mutableMapOf<Int, Int>()
        for ((quest, struct) in EnumDefinitions.get(QUEST_STRUCT_ENUM).map ?: emptyMap()) {
            questIds[struct as Int] = quest as Int
        }
        val requirements = mutableMapOf<Int, String?>()
        for ((slot, struct) in EnumDefinitions.get(BOOK_ENUM).map ?: return@lazy requirements) {
            val definition = StructDefinitions.get(struct as Int)
            val questStruct = definition.getOrNull<Int>(BOOK_QUEST_PARAM) ?: -1
            if (questStruct == -1) {
                requirements[slot as Int] = null
                continue
            }
            val id = questIds[questStruct] ?: continue
            val quest = unnamedQuests[id] ?: quests.getOrNull(id)?.stringId ?: continue
            requirements[slot as Int] = quest
        }
        requirements
    }

    private fun Player.isUnlocked(slot: Int): Boolean {
        if (!requirements.containsKey(slot)) {
            return false
        }
        val quest = requirements[slot] ?: return true
        return questCompleted(quest)
    }

    /**
     * Sends which books are unlocked so the client can rebuild the list
     */
    private fun Player.sendBooks() {
        val bits = IntArray(BOOK_VARCS.size)
        for (slot in requirements.keys) {
            if (isUnlocked(slot)) {
                bits[slot / Int.SIZE_BITS] = bits[slot / Int.SIZE_BITS] or (1 shl (slot % Int.SIZE_BITS))
            }
        }
        for ((index, varc) in BOOK_VARCS.withIndex()) {
            set(varc, bits[index])
        }
    }

    /**
     * Position of each book in the book enum, which is the bit the client reads to show the book
     */
    private val slots: Map<String, Int> by lazy {
        val slots = mutableMapOf<String, Int>()
        for ((slot, struct) in EnumDefinitions.get(BOOK_ENUM).map ?: return@lazy slots) {
            val item = StructDefinitions.get(struct as Int).getOrNull<Int>(BOOK_ITEM_PARAM) ?: continue
            slots[ItemDefinitions.get(item).stringId] = slot as Int
        }
        slots
    }

    companion object {
        private const val BOOKCASES = "wooden_bookcase,oak_bookcase,mahogany_bookcase"
        private val BOOK_VARCS = listOf("house_books_1", "house_books_2", "house_books_3")
        private const val BOOK_ENUM = 845
        private const val BOOK_SLOTS = 96 // Three varcs of 32 books
        private const val BOOK_ITEM_PARAM = 813
        private const val BOOK_QUEST_PARAM = 923
        private const val QUEST_STRUCT_ENUM = 2252
    }
}
