package content.skill.constitution.drink

import FakeRandom
import WorldTest
import content.entity.player.bank.bank
import itemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom

internal class JujuTest : WorldTest() {

    @Test
    fun `Juju mining potion starts a five minute effect`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("juju_mining_potion_4")

        player.itemOption("Drink", "juju_mining_potion_4")

        assertTrue(player.inventory.contains("juju_mining_potion_3"))
        assertTrue(player.jujuActive("juju_mining"))
    }

    @Test
    fun `Last dose of a juju potion leaves a juju vial`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("juju_woodcutting_potion_1")

        player.itemOption("Drink", "juju_woodcutting_potion_1")

        assertTrue(player.inventory.contains("juju_vial"))
        assertTrue(player.jujuActive("juju_woodcutting"))
    }

    @Test
    fun `Juju hunter potion is not drinkable`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("juju_hunter_potion_4")

        player.itemOption("Drink", "juju_hunter_potion_4")

        assertTrue(player.inventory.contains("juju_hunter_potion_4"))
        assertFalse(player.jujuActive("juju_hunter"))
    }

    @Test
    fun `God blessings start their own effects`() {
        for (blessing in listOf("saradomins_blessing", "guthixs_gift", "zamoraks_favour")) {
            val player = createPlayer(emptyTile)
            player.inventory.add("${blessing}_4")

            player.itemOption("Drink", "${blessing}_4")

            assertTrue(player.inventory.contains("${blessing}_3"), blessing)
            assertTrue(player.jujuActive(blessing), blessing)
        }
    }

    @Test
    fun `Juju mining potion sends ore to the bank`() {
        setRandom(
            object : FakeRandom() {
                override fun nextInt(from: Int, until: Int): Int = if (until == 100) 0 else from
            },
        )
        val player = createPlayer(emptyTile)
        player.experience.set(Skill.Mining, Level.experience(99))
        player.levels.set(Skill.Mining, 99)
        player.startJuju("juju_mining")

        val banked = player.jujuBank("juju_mining", "copper_ore", 1)

        assertTrue(banked)
        assertEquals(1, player.bank.count("copper_ore"))
        assertFalse(player.inventory.contains("copper_ore"))
    }

    @Test
    fun `Ore is not banked without the potion`() {
        setRandom(
            object : FakeRandom() {
                override fun nextInt(from: Int, until: Int): Int = if (until == 100) 0 else from
            },
        )
        val player = createPlayer(emptyTile)

        val banked = player.jujuBank("juju_mining", "copper_ore", 1)

        assertFalse(banked)
        assertEquals(0, player.bank.count("copper_ore"))
    }
}
