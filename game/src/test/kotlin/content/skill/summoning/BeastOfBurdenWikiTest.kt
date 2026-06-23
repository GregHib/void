package content.skill.summoning

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class BeastOfBurdenWikiTest : WorldTest() {

    @Test
    fun `untradeable items cannot be stored`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.inventory.add("attack_cape", 1) // untradeable
        player.openBeastOfBurden()

        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("attack_cape"), slot = 0)

        assertEquals(0, player.beastOfBurden.count("attack_cape"))
        assertEquals(1, player.inventory.count("attack_cape"))
        assertTrue(player.containsMessage("Your familiar can't carry that item."))
    }

    @Test
    fun `tradeable items can still be stored`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.inventory.add("coins", 100)
        player.openBeastOfBurden()

        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("coins"), slot = 0)

        assertEquals(100, player.beastOfBurden.count("coins"))
    }

    @Test
    fun `unstackable item worth over 5m cannot be stored`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.inventory.add("red_partyhat", 1) // tradeable but worth ~1.9b
        player.openBeastOfBurden()

        player.interfaceOption("summoning_side", "inventory", "Store-1", item = Item("red_partyhat"), slot = 0)

        assertEquals(0, player.beastOfBurden.count("red_partyhat"))
        assertEquals(1, player.inventory.count("red_partyhat"))
        assertTrue(player.containsMessage("Your familiar can't carry items that valuable."))
    }

    @Test
    fun `stack worth more than 5m cannot be stored`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.inventory.add("coins", 5_000_001)
        player.openBeastOfBurden()

        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("coins"), slot = 0)

        assertEquals(0, player.beastOfBurden.count("coins"))
        assertEquals(5_000_001, player.inventory.count("coins"))
        assertTrue(player.containsMessage("Your familiar can't carry items that valuable."))
    }

    @Test
    fun `stack worth exactly 5m can be stored`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.inventory.add("coins", 5_000_000)
        player.openBeastOfBurden()

        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("coins"), slot = 0)

        assertEquals(5_000_000, player.beastOfBurden.count("coins"))
    }

    @Test
    fun `cannot open familiar inventory while in combat`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.start("under_attack", 8)

        player.openBeastOfBurden()

        assertFalse(player.hasOpen("beast_of_burden"))
        assertTrue(player.containsMessage("You can't do that in combat."))
    }

    @Test
    fun `dismissing drops stored items under the familiar`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.beastOfBurden.add("coins", 250)
        val familiarTile = Tile(3205, 3205)
        player.follower!!.tele(familiarTile)

        player.dismissFamiliar()
        tick(1)

        assertTrue(player.beastOfBurden.isEmpty())
        assertNotNull(FloorItems.firstOrNull(familiarTile, "coins"))
        assertNull(FloorItems.firstOrNull(player.tile, "coins"))
        assertTrue(player.containsMessage("Your familiar has dropped all the items it was holding."))
    }

    @Test
    fun `familiar death drops stored items under the familiar and clears follower`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.beastOfBurden.add("coins", 250)
        val familiar = player.follower!!
        val familiarTile = Tile(3205, 3205)
        familiar.tele(familiarTile)

        familiar.levels.set(Skill.Constitution, 0)
        tick(2)

        assertNull(player.follower)
        assertTrue(player.beastOfBurden.isEmpty())
        assertNotNull(FloorItems.firstOrNull(familiarTile, "coins"))
        assertNull(FloorItems.firstOrNull(player.tile, "coins"))
    }
}
