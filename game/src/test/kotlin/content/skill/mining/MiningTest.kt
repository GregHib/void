package content.skill.mining

import FakeRandom
import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

internal class MiningTest : WorldTest() {

    @Test
    fun `Mining gives ore and depletes`() {
        setRandom(Random)
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Mining, 100)
        val tile = emptyTile.addY(1)
        val rocks = createObject("tin_rocks_rock_1", tile)
        player.inventory.add("bronze_pickaxe")

        player.objectOption(rocks, "Mine")
        tickIf { player.inventory.spaces >= 27 }

        assertTrue(player.inventory.contains("tin_ore"))
        assertTrue(player.experience.get(Skill.Mining) > 0)
        assertNotEquals(rocks.id, GameObjects.getLayer(tile, ObjectLayer.GROUND)?.id)
    }

    @Test
    fun `Mining a full yield does not claim the inventory is full`() {
        // Force the yield roll to its maximum, but leave the success and gem rolls to chance.
        val rolls = Random(42)
        setRandom(
            object : FakeRandom() {
                override fun nextInt(from: Int, until: Int): Int = if (until == 5) 4 else rolls.nextInt(from, until)
                override fun nextInt(until: Int): Int = rolls.nextInt(until)
            },
        )
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Mining, 100)
        val deposit = createObject("mineral_deposit_gold", emptyTile.addY(1))
        player.inventory.add("bronze_pickaxe")

        player.objectOption(deposit, "Mine")
        tickIf { !player.inventory.contains("gold_ore") }

        assertTrue(player.containsMessage("You manage to mine four gold ore"))
        assertFalse(player.containsMessage("You don't have enough inventory space."))
    }
}
