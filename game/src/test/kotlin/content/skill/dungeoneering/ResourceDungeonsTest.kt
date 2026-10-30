package content.skill.dungeoneering

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Tile

class ResourceDungeonsTest : WorldTest() {

    @Test
    fun `Resource dungeon entrance teleports inside`() {
        val player = createPlayer(Tile(3132, 9932))
        player.levels.set(Skill.Dungeoneering, 10)

        val entrance = GameObject(52849, Tile(3132, 9933), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(entrance)
        player.objectOption(entrance, "Enter")
        tick(4)

        assertEquals(Tile(991, 4585), player.tile)
        assertEquals(1100.0, player.experience.get(Skill.Dungeoneering))
        assertTrue(player.containsMessage("You receive 1100 Dungeoneering experience for uncovering a new dungeon for the first time."))
    }

    @Test
    fun `Resource dungeon exit teleports outside`() {
        val player = createPlayer(Tile(991, 4584))
        player.levels.set(Skill.Dungeoneering, 10)

        val exit = GameObject(52867, Tile(991, 4585), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(exit)
        player.objectOption(exit, "Exit")
        tick(4)

        assertEquals(Tile(3132, 9933), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Dungeoneering))
    }

    @Test
    fun `Resource dungeon paired enter entrance teleports inside`() {
        val player = createPlayer(Tile(3498, 3632))
        player.levels.set(Skill.Dungeoneering, 30)

        val entrance = GameObject(52862, Tile(3498, 3633), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(entrance)
        player.objectOption(entrance, "Enter")
        tick(4)

        assertEquals(Tile(3513, 3666), player.tile)
        assertEquals(2400.0, player.experience.get(Skill.Dungeoneering))
        assertTrue(player.containsMessage("You receive 2400 Dungeoneering experience for uncovering a new dungeon for the first time."))
    }

    @Test
    fun `Resource dungeon paired return entrance teleports outside`() {
        val player = createPlayer(Tile(3513, 3665))
        player.levels.set(Skill.Dungeoneering, 30)

        val returnEntrance = GameObject(52861, Tile(3513, 3666), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(returnEntrance)
        player.objectOption(returnEntrance, "Enter")
        tick(4)

        assertEquals(Tile(3498, 3633), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Dungeoneering))
    }

    @Test
    fun `Resource dungeon entrance requires dungeoneering level`() {
        val player = createPlayer(Tile(3132, 9932))
        val requiredLevel = 10

        val entrance = GameObject(52849, Tile(3132, 9933), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(entrance)
        player.objectOption(entrance, "Enter")
        tick(2)

        assertEquals(Tile(3132, 9932), player.tile)
        assertTrue(player.containsMessage("You need a Dungeoneering level of $requiredLevel to venture in there."))
    }

    @Test
    fun `Resource dungeon xp is only granted the first time`() {
        val player = createPlayer(Tile(3132, 9932))
        player.levels.set(Skill.Dungeoneering, 10)

        val entrance = GameObject(52849, Tile(3132, 9933), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        val exit = GameObject(52867, Tile(991, 4585), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(entrance)
        GameObjects.add(exit)

        player.objectOption(entrance, "Enter")
        tick(4)
        assertEquals(1100.0, player.experience.get(Skill.Dungeoneering))
        assertTrue(player.containsMessage("You receive 1100 Dungeoneering experience for uncovering a new dungeon for the first time."))

        player.objectOption(exit, "Exit")
        tick(4)
        assertEquals(1100.0, player.experience.get(Skill.Dungeoneering))

        player.objectOption(entrance, "Enter")
        tick(4)
        assertEquals(1100.0, player.experience.get(Skill.Dungeoneering))
    }

    @Test
    fun `Resource dungeon repeated click does not restart teleport`() {
        val player = createPlayer(Tile(3132, 9932))
        player.levels.set(Skill.Dungeoneering, 10)

        val entrance = GameObject(52849, Tile(3132, 9933), ObjectShape.CENTRE_PIECE_STRAIGHT, 0)
        GameObjects.add(entrance)

        player.objectOption(entrance, "Enter")
        tick()
        assertTrue(player.hasClock("resource_dungeon_teleporting"))
        val remaining = player.remaining("resource_dungeon_teleporting")

        player.objectOption(entrance, "Enter")
        assertEquals(remaining, player.remaining("resource_dungeon_teleporting"))

        tick(3)

        assertEquals(Tile(991, 4585), player.tile)
        assertEquals(1100.0, player.experience.get(Skill.Dungeoneering))
    }
}
