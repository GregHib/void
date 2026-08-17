package content.skill.thieving

import FakeRandom
import WorldTest
import content.entity.effect.stunned
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class PickpocketingTest : WorldTest() {

    @Test
    fun `Successfully pickpocket`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(3, player.inventory.count("coins"))
        assertEquals(8.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }

    @Test
    fun `Successfully pickpocket multiple loot`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Thieving, 11)
        player.levels.set(Skill.Agility, 1)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(6, player.inventory.count("coins"))
        assertEquals(8.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }

    @Test
    fun `Fail to pickpocket`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer(emptyTile)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertTrue(player.stunned)
    }

    @Test
    fun `Can't pickpocket with full inventory`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("cheese", 28)
        val man = createNPC("man", emptyTile.addY(1))

        player.npcOption(man, "Pickpocket")
        tick(4)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertFalse(player.stunned)
    }
}
