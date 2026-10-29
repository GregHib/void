package content.quest.member.horror_from_the_deep

import WorldTest
import containsMessage
import dialogueOption
import itemOnItem
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class GodBooksTest : WorldTest() {

    @Test
    fun `Bind four pages into a damaged book`() {
        val player = createPlayer(Tile(3222, 3218))
        player.inventory.add("damaged_book_saradomin")
        for (page in 1..4) {
            player.inventory.add("saradomin_page_$page")
        }

        player.itemOnItem(1, 0)
        tick()

        assertEquals(1, player.inventory.count("saradomins_book_of_wisdom"))
        assertEquals(0, player.inventory.count("damaged_book_saradomin"))
        assertTrue(player["saradomin_book_completed", false])
    }

    @Test
    fun `Can't bind without all four pages`() {
        val player = createPlayer(Tile(3222, 3218))
        player.inventory.add("damaged_book_zamorak")
        player.inventory.add("zamorak_page_1")

        player.itemOnItem(1, 0)
        tick()

        assertEquals(1, player.inventory.count("damaged_book_zamorak"))
        assertTrue(player.containsMessage("You need all four pages to create this book."))
    }

    @Test
    fun `Bless a holy symbol with a book`() {
        val player = createPlayer(Tile(3222, 3218))
        player.levels.set(Skill.Prayer, 50)
        player.inventory.add("saradomins_book_of_wisdom")
        player.inventory.add("unblessed_symbol")

        player.itemOnItem(1, 0)
        tick()

        assertEquals(1, player.inventory.count("holy_symbol"))
        assertEquals(46, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Buy a damaged book from Jossik`() {
        val player = createPlayer(Tile(2509, 3638, 1))
        player.inventory.add("coins", 5000)
        val jossik = createNPC("jossik", Tile(2509, 3639, 1))

        player.npcOption(jossik, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        tick()
        player.skipDialogues()
        player.dialogueOption(3)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("damaged_book_guthix"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Jossik returns lost completed books`() {
        val player = createPlayer(Tile(2509, 3638, 1))
        player["zamorak_book_completed"] = true
        val jossik = createNPC("jossik", Tile(2509, 3639, 1))

        player.npcOption(jossik, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("zamoraks_book_of_chaos"))
    }
}
