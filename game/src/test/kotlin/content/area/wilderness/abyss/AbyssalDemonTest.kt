package content.area.wilderness.abyss

import FakeRandom
import WorldTest
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class AbyssalDemonTest : WorldTest() {

    @Test
    fun `Abyssal demon teleports target around in combat`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 85)

        val demon = NPCs.add("abyssal_demon", emptyTile.addY(1))
        tick()

        player.npcOption(demon, "Attack")

        tick(3)

        assertNotEquals(emptyTile.addY(1), demon.tile)
    }

    @Test
    fun `Abyssal demon teleports around in combat`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 6) 6 else 0
            override fun nextInt(from: Int, until: Int) = until - 1
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 85)

        val demon = NPCs.add("abyssal_demon", emptyTile.addY(1))
        tick()

        player.npcOption(demon, "Attack")

        tick(3)

        assertNotEquals(emptyTile, player.tile)
    }

    @Test
    fun `Abyssal demon can't teleport target outside slayer tower walls`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 6) 1 else 0
        })
        // Demon against the west wall of the top floor, open air on the other side
        val player = createPlayer(Tile(3406, 3572, 2))
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 85)

        val demon = NPCs.add("abyssal_demon", Tile(3405, 3572, 2))
        tick()

        player.npcOption(demon, "Attack")

        tick(3)

        assertNotEquals(Tile(3406, 3572, 2), player.tile)
        assertTrue(player.tile.x >= 3405)
        assertTrue(Collisions.check(player.tile, CollisionFlag.ROOF))
        assertEquals(1, player.tile.distanceTo(demon.tile))
    }
}
