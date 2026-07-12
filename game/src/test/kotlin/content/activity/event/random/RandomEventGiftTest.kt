package content.activity.event.random

import WorldTest
import equipItem
import interfaceOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RandomEventGiftTest : WorldTest() {

    private fun openGift(name: String): Player {
        val player = createPlayer(Tile(3221, 3218), name)
        player.inventory.add("random_event_gift")
        player.equipItem("random_event_gift", option = "Open")
        tick()
        return player
    }

    private val Player.gift
        get() = inventories.inventory("random_event_gift")

    private fun Player.select(slot: Int) {
        // The grid packs seven sub-components per reward, like the client sends
        interfaceOption("random_event_gift_select", "rewards", "Select", item = gift[slot], slot = slot * 7)
        tick()
    }

    @Test
    fun `Opening a gift deals a reward for every category`() {
        val player = openGift("gift_open")

        assertTrue(player.interfaces.contains("random_event_gift_select"))
        assertEquals("coins", player.gift[0].id)
        assertTrue(player.gift[0].amount >= 30)
        assertEquals("coal", player.gift[2].id)
        assertTrue(player.gift[3].id.endsWith("essence"))
        assertEquals("lamp", player.gift[10].id)
        assertEquals("mystery_box", player.gift[25].id)
        assertTrue(player.gift[24].isNotEmpty(), "Expected an oddment reward")
    }

    @Test
    fun `Reopening an unclaimed gift keeps the same choices`() {
        val player = openGift("gift_reopen")
        val rewards = player.gift.items.toList()

        player.interfaceOption("random_event_gift_select", "close", "Close")
        tick()
        player.equipItem("random_event_gift", option = "Open")
        tick()

        assertEquals(rewards, player.gift.items.toList())
    }

    @Test
    fun `Selecting with a bare grid click like the client sends works`() {
        val player = openGift("gift_bare")
        val amount = player.gift[2].amount

        // The client sends the packed sub-component slot with no item attached
        player.interfaceOption("random_event_gift_select", "rewards", "Select", slot = 2 * 7)
        tick()
        player.interfaceOption("random_event_gift_select", "confirm", "Confirm")
        tick()

        assertEquals(amount, player.inventory.count("coal_noted"), "Expected unstackable coal granted noted")
        assertFalse(player.inventory.contains("random_event_gift"))
    }

    @Test
    fun `Choosing the emote unlocks a random event emote`() {
        val player = openGift("gift_emote")

        // The emote cell is drawn by the interface; its container slot is empty
        player.interfaceOption("random_event_gift_select", "rewards", "Select", slot = 26 * 7)
        tick()
        player.interfaceOption("random_event_gift_select", "confirm", "Confirm")
        tick()

        assertTrue(player.get("unlocked_emote_idea", false), "Expected an emote unlocked")
        assertFalse(player.inventory.contains("random_event_gift"))
    }

    @Test
    fun `Confirming without choosing keeps the gift`() {
        val player = openGift("gift_none")

        player.interfaceOption("random_event_gift_select", "confirm", "Confirm")
        tick()

        assertTrue(player.inventory.contains("random_event_gift"))
        assertFalse(player.inventory.contains("coins"))
    }

    @Test
    fun `Choosing coins and confirming consumes the gift`() {
        val player = openGift("gift_coins")
        val amount = player.gift[0].amount

        player.select(0)
        player.interfaceOption("random_event_gift_select", "confirm", "Confirm")
        tick()

        assertEquals(amount, player.inventory.count("coins"))
        assertFalse(player.inventory.contains("random_event_gift"))
        assertFalse(player.interfaces.contains("random_event_gift_select"))
        assertTrue(player.gift.isEmpty(), "Expected the unclaimed choices cleared")
    }

    @Test
    fun `Rubbing the lamp grants ten experience per level in the chosen skill`() {
        val player = createPlayer(Tile(3221, 3218), "gift_lamp")
        player.inventory.add("lamp")

        player.equipItem("lamp", option = "Rub")
        tick()
        player.interfaceOption("skill_stat_advance", "thieving", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        tick()
        player.skipDialogues()

        assertEquals(10.0, player.experience.get(Skill.Thieving))
        assertFalse(player.inventory.contains("lamp"))
    }

    @Test
    fun `Constitution lamp experience uses the real level not hitpoints`() {
        val player = createPlayer(Tile(3221, 3218), "gift_lamp_con")
        player.inventory.add("lamp")
        val before = player.experience.get(Skill.Constitution)

        player.equipItem("lamp", option = "Rub")
        tick()
        player.interfaceOption("skill_stat_advance", "constitution", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        tick()
        player.skipDialogues()

        // Level 10 Constitution (stored as 100 internally) grants 100 xp, not 1000
        assertEquals(100.0, player.experience.get(Skill.Constitution) - before)
        assertFalse(player.inventory.contains("lamp"))
    }

    @Test
    fun `Opening a mystery box rewards a random item`() {
        val player = createPlayer(Tile(3221, 3218), "gift_mystery")
        player.inventory.add("mystery_box")

        player.equipItem("mystery_box", option = "Open")
        tick(2)

        assertFalse(player.inventory.contains("mystery_box"))
        assertFalse(player.inventory.isEmpty(), "Expected something inside the box")
        assertTrue(player["messages", emptyList<String>()].any { it.startsWith("Inside the box you find") })
    }

    @Test
    fun `Saving up for a costume banks a costume point`() {
        val player = openGift("gift_costume")

        // The costume cell is drawn by the interface; its container slot is empty
        player.interfaceOption("random_event_gift_select", "rewards", "Select", slot = 27 * 7)
        tick()
        player.interfaceOption("random_event_gift_select", "confirm", "Confirm")
        tick()

        assertEquals(1, player.get("costume_points", 0))
        assertFalse(player.inventory.contains("random_event_gift"))
    }
}
