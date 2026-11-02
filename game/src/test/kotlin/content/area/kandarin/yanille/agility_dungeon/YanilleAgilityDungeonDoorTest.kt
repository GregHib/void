package content.area.kandarin.yanille.agility_dungeon

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class YanilleAgilityDungeonDoorTest : WorldTest() {
    private val doorTile = Tile(2601, 9482)
    private val outside = Tile(2601, 9481)

    @Test
    fun `Open cannot bypass the locked entrance`() {
        val player = createPlayer(outside)
        player.experience.set(Skill.Thieving, Level.experience(99))
        player.inventory.add("lockpick")
        player.objectOption(GameObjects.find(doorTile, "door_70_closed"), "Open")
        tick(8)
        assertEquals(outside, player.tile)
        assertTrue(player.containsMessage("The door is locked."))
    }

    @Test
    fun `Picking requires level 82 and a lockpick`() {
        val player = createPlayer(outside)
        player.inventory.add("lockpick")
        player.objectOption(GameObjects.find(doorTile, "door_70_closed"), "Pick-lock")
        tick(8)
        assertEquals(outside, player.tile)
        player.experience.set(Skill.Thieving, Level.experience(82))
        player.inventory.clear()
        player.objectOption(GameObjects.find(doorTile, "door_70_closed"), "Pick-lock")
        tick(8)
        assertEquals(outside, player.tile)
        assertTrue(player.containsMessage("You need a lockpick for this lock."))
    }

    @Test
    fun `Boosted thieving allows passage grants experience and closes door`() {
        val player = createPlayer(outside)
        player.experience.set(Skill.Thieving, Level.experience(80))
        player.levels.set(Skill.Thieving, 82)
        player.inventory.add("lockpick")
        val before = player.experience.get(Skill.Thieving)
        player.objectOption(GameObjects.find(doorTile, "door_70_closed"), "Pick-lock")
        tick(8)
        assertEquals(doorTile, player.tile)
        assertEquals(before + 50.0, player.experience.get(Skill.Thieving))
        assertTrue(player.inventory.contains("lockpick"))
        assertNotNull(GameObjects.findOrNull(doorTile, "door_70_closed"))
    }

    @Test
    fun `Exit needs neither lockpick nor thieving`() {
        val player = createPlayer(doorTile)
        player.objectOption(GameObjects.find(doorTile, "door_70_closed"), "Open")
        tick(8)
        assertEquals(outside, player.tile)
        assertEquals(0.0, player.experience.get(Skill.Thieving))
        assertNotNull(GameObjects.findOrNull(doorTile, "door_70_closed"))
    }

    @Test
    fun `Picking from the north reports that the door is already unlocked`() {
        val player = createPlayer(Tile(2601, 9483))
        player.objectOption(GameObjects.find(doorTile, "door_70_closed"), "Pick-lock")
        tick(8)
        assertTrue(player.containsMessage("The door is already unlocked."))
    }
}
