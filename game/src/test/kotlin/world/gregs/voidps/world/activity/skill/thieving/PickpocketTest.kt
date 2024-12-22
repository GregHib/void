package world.gregs.voidps.world.activity.skill.thieving

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.interact.entity.effect.stunned
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.npcOption
import kotlin.random.Random
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

}