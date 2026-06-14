package content.skill.cooking

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skillCreation
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class DairyChurnTest : WorldTest() {

    @Test
    fun `Churn milk into cream and get the bucket back`() {
        val player = createPlayer(Tile(3254, 3274))
        player.levels.set(Skill.Cooking, 21)
        player.inventory.add("bucket_of_milk")
        val churn = createObject("dairy_churn_lumbridge", Tile(3254, 3275))

        player.objectOption(churn, "Churn")
        tick()
        player.skillCreation("Pot of cream", 1)
        tick(4)

        assertEquals(0, player.inventory.count("bucket_of_milk"))
        assertEquals(1, player.inventory.count("pot_of_cream"))
        assertEquals(1, player.inventory.count("bucket"))
        assertNotEquals(0.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Churning butter consumes milk before cream`() {
        val player = createPlayer(Tile(3254, 3274))
        player.levels.set(Skill.Cooking, 38)
        player.inventory.add("bucket_of_milk")
        player.inventory.add("pot_of_cream")
        val churn = createObject("dairy_churn_lumbridge", Tile(3254, 3275))

        player.objectOption(churn, "Churn")
        tick()
        player.skillCreation("Pat of butter", 1)
        tick(4)

        assertEquals(0, player.inventory.count("bucket_of_milk"))
        assertEquals(1, player.inventory.count("pot_of_cream"))
        assertEquals(1, player.inventory.count("pat_of_butter"))
        assertEquals(1, player.inventory.count("bucket"))
    }

    @Test
    fun `Churn cheese from butter without bucket back`() {
        val player = createPlayer(Tile(3254, 3274))
        player.levels.set(Skill.Cooking, 48)
        player.inventory.add("pat_of_butter")
        val churn = createObject("dairy_churn_lumbridge", Tile(3254, 3275))

        player.objectOption(churn, "Churn")
        tick()
        player.skillCreation("Cheese", 1)
        tick(4)

        assertEquals(0, player.inventory.count("pat_of_butter"))
        assertEquals(1, player.inventory.count("cheese"))
        assertEquals(0, player.inventory.count("bucket"))
    }

    @Test
    fun `Cannot churn without the required level`() {
        val player = createPlayer(Tile(3254, 3274))
        player.levels.set(Skill.Cooking, 20)
        player.inventory.add("bucket_of_milk")
        val churn = createObject("dairy_churn_lumbridge", Tile(3254, 3275))

        player.objectOption(churn, "Churn")
        tick()
        player.skillCreation("Pot of cream", 1)
        tick(4)

        assertEquals(1, player.inventory.count("bucket_of_milk"))
        assertEquals(0, player.inventory.count("pot_of_cream"))
        assertTrue(player.containsMessage("You need a Cooking level of 21 to make a pot of cream."))
    }

    @Test
    fun `Cannot churn without any dairy items`() {
        val player = createPlayer(Tile(3254, 3274))
        player.levels.set(Skill.Cooking, 99)
        val churn = createObject("dairy_churn_lumbridge", Tile(3254, 3275))

        player.objectOption(churn, "Churn")
        tick()

        assertTrue(player.containsMessage("You need some milk, cream or butter to use in the churn."))
    }

    @Test
    fun `Bucket drops to the floor when inventory is full`() {
        val player = createPlayer(Tile(3254, 3274))
        player.levels.set(Skill.Cooking, 21)
        repeat(28) {
            player.inventory.add("bucket_of_milk")
        }
        val churn = createObject("dairy_churn_lumbridge", Tile(3254, 3275))

        player.objectOption(churn, "Churn")
        tick()
        player.skillCreation("Pot of cream", 1)
        tick(4)

        assertEquals(27, player.inventory.count("bucket_of_milk"))
        assertEquals(1, player.inventory.count("pot_of_cream"))
        assertEquals(0, player.inventory.count("bucket"))
        assertNotEquals(null, FloorItems.firstOrNull(player.tile, "bucket"))
    }
}
