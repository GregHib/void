package content.skill.thieving

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import FakeRandom
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import content.entity.effect.stunned
import WorldTest
import npcOption
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class PickpocketTest : WorldTest() {

    @Test
    fun `Successfully pickpocket`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer("thief", emptyTile)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(player.inventory.count("coins"), 3)
        assertEquals(player.experience.get(Skill.Thieving), 8.0)
        assertFalse(player.stunned)
    }

    @Test
    fun `Fail to pickpocket`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("thief", emptyTile)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(player.inventory.count("coins"), 0)
        assertEquals(player.experience.get(Skill.Thieving), 0.0)
        assertTrue(player.stunned)
    }

    @Test
    fun `Can't pickpocket with full inventory`() {
        val player = createPlayer("thief", emptyTile)
        player.inventory.add("cheese", 28)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(player.inventory.count("coins"), 0)
        assertEquals(player.experience.get(Skill.Thieving), 0.0)
        assertFalse(player.stunned)
    }

}