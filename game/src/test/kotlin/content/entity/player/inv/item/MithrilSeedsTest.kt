package content.entity.player.inv.item

import WorldTest
import dialogueOption
import itemOption
import messages
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MithrilSeedsTest : WorldTest() {
    private val plot get() = emptyTile

    @Test
    fun `Plant consumes one seed steps west and picking gives matching flowers`() {
        val player = createPlayer(plot)
        player.inventory.add("mithril_seeds", 2)
        player.itemOption("Plant", "mithril_seeds")
        tick(6)
        val flowers = assertNotNull(GameObjects.getLayer(plot, ObjectLayer.GROUND), "${player.messages}")
        assertTrue(flowers.id.startsWith("planted_"))
        assertEquals(1, player.inventory.count("mithril_seeds"))
        assertEquals(plot.add(Direction.WEST), player.tile)
        assertNotNull(player.dialogue)
        player.dialogueOption(1)
        tick()
        assertTrue(player.inventory.contains(flowers.id.removePrefix("planted_")))
        assertNull(GameObjects.getLayer(plot, ObjectLayer.GROUND))
    }

    @Test
    fun `Leaving flowers keeps them planted until five minute expiry`() {
        val player = createPlayer(plot)
        player.inventory.add("mithril_seeds")
        player.itemOption("Plant", "mithril_seeds")
        tick(6)
        player.dialogueOption(2)
        tick()
        assertNotNull(GameObjects.getLayer(plot, ObjectLayer.GROUND))
        assertEquals(0, player.inventory.count("mithril_seeds"))
        tick(500)
        assertNull(GameObjects.getLayer(plot, ObjectLayer.GROUND))
    }

    @Test
    fun `Occupied ground keeps seeds and original object`() {
        val player = createPlayer(plot)
        val existing = GameObjects.add("planted_red_flowers", plot)
        player.inventory.add("mithril_seeds")
        player.itemOption("Plant", "mithril_seeds")
        tick(6)
        assertEquals(1, player.inventory.count("mithril_seeds"))
        assertEquals(existing, GameObjects.getLayer(plot, ObjectLayer.GROUND))
        assertNull(player.dialogue)
    }

    @Test
    fun `Frozen player cannot plant or move`() {
        val player = createPlayer(plot)
        player.inventory.add("mithril_seeds")
        player.start("movement_delay", 20)
        player.itemOption("Plant", "mithril_seeds")
        tick(6)
        assertEquals(1, player.inventory.count("mithril_seeds"))
        assertEquals(plot, player.tile)
        assertNull(GameObjects.getLayer(plot, ObjectLayer.GROUND))
    }

    @Test
    fun `Full inventory leaves flowers instead of losing them on picking`() {
        val player = createPlayer(plot)
        player.inventory.add("mithril_seeds", 2)
        player.inventory.add("bronze_dagger", 27)
        player.itemOption("Plant", "mithril_seeds")
        tick(6)
        val flowers = assertNotNull(GameObjects.getLayer(plot, ObjectLayer.GROUND))
        player.dialogueOption(1)
        tick()
        assertEquals(flowers, GameObjects.getLayer(plot, ObjectLayer.GROUND))
        assertTrue(player.messages.any { it.contains("enough inventory space") })
        assertEquals(1, player.inventory.count("mithril_seeds"))
    }
}
