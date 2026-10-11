package content.area.kandarin.yanille

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MagicGuildDoorTest : WorldTest() {
    @Test
    fun `Qualified player crosses and both guild doors close`() {
        val tile = Tile(4000, 4000)
        val player = createPlayer(tile.addX(-1))
        player.experience.set(Skill.Magic, Level.experience(66))
        val door = createObject("magic_guild_door_closed", tile, ObjectShape.WALL_STRAIGHT, 0)
        createObject("magic_guild_door_2_closed", tile.addY(1), ObjectShape.WALL_STRAIGHT, 0)
        player.objectOption(door, "Open")
        tick(8)
        assertEquals(tile, player.tile)
        assertNotNull(GameObjects.findOrNull(tile, "magic_guild_door_closed"))
        assertNotNull(GameObjects.findOrNull(tile.addY(1), "magic_guild_door_2_closed"))
    }

    @Test
    fun `Insufficient Magic leaves player outside and doors closed`() {
        val tile = Tile(4000, 4000)
        val start = tile.addX(-1)
        val player = createPlayer(start)
        val door = createObject("magic_guild_door_closed", tile, ObjectShape.WALL_STRAIGHT, 0)
        createObject("magic_guild_door_2_closed", tile.addY(1), ObjectShape.WALL_STRAIGHT, 0)
        player.objectOption(door, "Open")
        tick(8)
        assertEquals(start, player.tile)
        assertNotNull(GameObjects.findOrNull(tile, "magic_guild_door_closed"))
        assertNotNull(GameObjects.findOrNull(tile.addY(1), "magic_guild_door_2_closed"))
    }
}
