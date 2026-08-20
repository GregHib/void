package content.skill.smithing

import WorldTest
import dialogueContinue
import itemOnObject
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpiritShieldSigilsTest : WorldTest() {

    @TestFactory
    fun `Attach each sigil to a blessed spirit shield`() = SpiritShieldSigils.SIGILS.map { sigil ->
        DynamicTest.dynamicTest(sigil) {
            val player = createPlayer(emptyTile)
            val anvil = createObject("anvil", emptyTile.addY(1))
            player.qualified()
            player.inventory.add("blessed_spirit_shield", sigil, "hammer")

            player.attach(anvil, sigil)

            assertTrue(player.inventory.contains(SpiritShieldSigils.shield(sigil)))
            assertFalse(player.inventory.contains("blessed_spirit_shield"))
            assertFalse(player.inventory.contains(sigil))
            assertEquals(Level.experience(85) + 1800.0, player.experience.get(Skill.Smithing))
        }
    }

    @Test
    fun `Attach a sigil by using the shield on the anvil`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.qualified()
        player.inventory.add("blessed_spirit_shield", "divine_sigil", "hammer")

        player.attach(anvil, "blessed_spirit_shield")

        assertTrue(player.inventory.contains("divine_spirit_shield"))
    }

    @Test
    fun `Attaching a sigil needs a blessed spirit shield`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.qualified()
        player.inventory.add("divine_sigil", "hammer")

        player.attach(anvil, "divine_sigil")

        assertFalse(player.inventory.contains("divine_spirit_shield"))
        assertTrue(player.inventory.contains("divine_sigil"))
    }

    @Test
    fun `Attaching a sigil needs a hammer`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.qualified()
        player.inventory.add("blessed_spirit_shield", "divine_sigil")

        player.attach(anvil, "divine_sigil")

        assertFalse(player.inventory.contains("divine_spirit_shield"))
        assertTrue(player.inventory.contains("blessed_spirit_shield"))
        assertTrue(player.inventory.contains("divine_sigil"))
    }

    @Test
    fun `Attaching a sigil needs ninety prayer`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.qualified(prayer = 89)
        player.inventory.add("blessed_spirit_shield", "divine_sigil", "hammer")

        player.attach(anvil, "divine_sigil")

        assertFalse(player.inventory.contains("divine_spirit_shield"))
        assertTrue(player.inventory.contains("blessed_spirit_shield"))
        assertTrue(player.inventory.contains("divine_sigil"))
    }

    @Test
    fun `Attaching a sigil needs eighty five smithing`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.qualified(smithing = 84)
        player.inventory.add("blessed_spirit_shield", "divine_sigil", "hammer")

        player.attach(anvil, "divine_sigil")

        assertFalse(player.inventory.contains("divine_spirit_shield"))
        assertTrue(player.inventory.contains("blessed_spirit_shield"))
        assertTrue(player.inventory.contains("divine_sigil"))
    }

    @Test
    fun `Spent prayer points don't block attaching a sigil`() {
        val player = createPlayer(emptyTile)
        val anvil = createObject("anvil", emptyTile.addY(1))
        player.qualified()
        player.levels.set(Skill.Prayer, 0)
        player.inventory.add("blessed_spirit_shield", "divine_sigil", "hammer")

        player.attach(anvil, "divine_sigil")

        assertTrue(player.inventory.contains("divine_spirit_shield"))
    }

    private fun Player.qualified(prayer: Int = 90, smithing: Int = 85) {
        experience.set(Skill.Prayer, Level.experience(prayer))
        experience.set(Skill.Smithing, Level.experience(smithing))
    }

    private fun Player.attach(anvil: GameObject, used: String) {
        itemOnObject(anvil, inventory.indexOf(used))
        tick(6)
        dialogueContinue()
    }

    private companion object {
        private val emptyTile = Tile(3200, 3200)
    }
}
