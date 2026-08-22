package content.area.karamja.musa_point

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BananaTreesTest : WorldTest() {

    @Test
    fun `Pick a banana`() {
        val player = createPlayer(Tile(2908, 3161))
        val tree = GameObjects.find(TREE, "karamja_banana_tree_5")

        player.objectOption(tree, "Pick")
        tick()

        assertNotNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_4"))
        assertEquals(1, player.inventory.count("banana"))
    }

    @Test
    fun `Pick every banana from a tree`() {
        val player = createPlayer(Tile(2908, 3161))

        for (remaining in 5 downTo 1) {
            val tree = GameObjects.find(TREE, "karamja_banana_tree_$remaining")
            player.objectOption(tree, "Pick")
            tick()
        }

        assertNotNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_0"))
        assertEquals(5, player.inventory.count("banana"))
        assertTrue(player["five_a_day_task", false])
    }

    @Test
    fun `Can't pick from an empty tree`() {
        val player = createPlayer(Tile(2908, 3161))

        for (remaining in 5 downTo 1) {
            val tree = GameObjects.find(TREE, "karamja_banana_tree_$remaining")
            player.objectOption(tree, "Pick")
            tick()
        }
        val empty = GameObjects.find(TREE, "karamja_banana_tree_0")
        player.objectOption(empty, "Search")
        tick()

        assertEquals(5, player.inventory.count("banana"))
        assertNotNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_0"))
    }

    @Test
    fun `Tree regrows all of its bananas`() {
        val player = createPlayer(Tile(2908, 3161))

        for (remaining in 5 downTo 3) {
            val tree = GameObjects.find(TREE, "karamja_banana_tree_$remaining")
            player.objectOption(tree, "Pick")
            tick()
        }
        assertNotNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_2"))

        tick(300)

        assertNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_2"))
        assertNotNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_5"))
    }

    @Test
    fun `Can't pick with a full inventory`() {
        val player = createPlayer(Tile(2908, 3161))
        player.inventory.add("bronze_bar", 28)
        val tree = GameObjects.find(TREE, "karamja_banana_tree_5")

        player.objectOption(tree, "Pick")
        tick()

        assertFalse(player.inventory.contains("banana"))
        assertNotNull(GameObjects.findOrNull(TREE, "karamja_banana_tree_5"))
    }

    companion object {
        private val TREE = Tile(2909, 3161)
    }
}
