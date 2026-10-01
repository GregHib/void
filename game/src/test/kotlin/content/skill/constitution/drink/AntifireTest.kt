package content.skill.constitution.drink

import WorldTest
import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import itemOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class AntifireTest : WorldTest() {

    @Test
    fun `Antifire potion wears off`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("antifire_4")

        player.itemOption("Drink", "antifire_4")
        assertTrue(player.antifire)

        tick(601)

        assertFalse(player.antifire)
    }

    @Test
    fun `Super antifire wearing off leaves antifire running`() {
        val player = createPlayer(emptyTile)
        player.antifire(6)
        player.superAntifire(1)

        tick(150)

        assertFalse(player.superAntifire)
        assertTrue(player.antifire)
    }

    @Test
    fun `Dying clears dragonfire resistance`() {
        val player = createPlayer(emptyTile)
        player.antifire(6)
        player.superAntifire(6)

        player.levels.set(Skill.Constitution, 0)
        tick(12)

        assertFalse(player.antifire)
        assertFalse(player.superAntifire)
    }

    @Test
    fun `Dying clears an overload`() {
        val player = createPlayer(emptyTile)
        player["overload_refreshes_remaining"] = 20

        player.levels.set(Skill.Constitution, 0)
        tick(12)

        assertFalse(player.get("overload_refreshes_remaining", 0) > 0)
    }
}
