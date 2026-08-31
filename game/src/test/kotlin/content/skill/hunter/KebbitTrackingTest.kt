package content.skill.hunter

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KebbitTrackingTest : WorldTest() {

    // With FakeRandom the common kebbit trail is always burrow -> trail_0 -> trail_3 -> trail_4,
    // triggered at (2358,3599) then (2352,3603), ending at (2349,3604)
    private fun startTrail(player: Player): GameObject {
        val burrow = createObject("common_kebbit_burrow", Tile(2354, 3595))
        player.objectOption(burrow, "Inspect")
        tick(5)
        return burrow
    }

    @Test
    fun `Track and catch a common kebbit`() {
        val player = createPlayer(Tile(2353, 3595))
        player.inventory.add("noose_wand")
        player.levels.set(Skill.Hunter, 3)
        startTrail(player)
        assertEquals(4, player["common_kebbit_trail_0", 0])

        val first = createObject("kebbit_tracks_plant_0", Tile(2358, 3599))
        player.objectOption(first, "Inspect")
        tick(10)
        assertEquals(4, player["common_kebbit_trail_3", 0])

        val second = createObject("kebbit_tracks_plant_1", Tile(2352, 3603))
        player.objectOption(second, "Inspect")
        tick(10)
        assertEquals(4, player["common_kebbit_trail_4", 0])

        val bush = createObject("kebbit_bush", Tile(2349, 3604))
        player.objectOption(bush, "Search")
        tick(10)
        assertTrue(player.containsMessage("something is moving around"))

        player.objectOption(bush, "Attack")
        tick(5)
        assertEquals(1, player.inventory.count("common_kebbit_fur"))
        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.inventory.count("raw_beast_meat"))
        assertEquals(36.0, player.experience.get(Skill.Hunter))
        assertEquals(0, player["common_kebbit_trail_0", 0])
        assertEquals(0, player["common_kebbit_trail_3", 0])
    }

    @Test
    fun `Wrong trail object reveals nothing`() {
        val player = createPlayer(Tile(2353, 3595))
        player.levels.set(Skill.Hunter, 3)
        startTrail(player)

        val wrong = createObject("kebbit_tracks_plant_2", Tile(2352, 3603))
        player.objectOption(wrong, "Inspect")
        tick(15)
        assertEquals(0, player["common_kebbit_trail_3", 0])
        assertEquals(0, player["common_kebbit_trail_4", 0])
        assertTrue(player.containsMessage("You search but find nothing of interest"))
    }

    @Test
    fun `Can't catch without a noose wand`() {
        val player = createPlayer(Tile(2353, 3595))
        player.levels.set(Skill.Hunter, 3)
        startTrail(player)

        val bush = createObject("kebbit_bush", Tile(2349, 3604))
        player.objectOption(bush, "Attack")
        tick(15)
        assertTrue(player.containsMessage("You need a noose wand"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Attacking the wrong spot fails`() {
        val player = createPlayer(Tile(2353, 3595))
        player.inventory.add("noose_wand")
        player.levels.set(Skill.Hunter, 3)
        startTrail(player)

        val bush = createObject("kebbit_bush", Tile(2349, 3604))
        player.objectOption(bush, "Attack")
        tick(15)
        assertEquals(0, player.inventory.count("common_kebbit_fur"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
        assertEquals(4, player["common_kebbit_trail_0", 0])
    }

    @Test
    fun `Can't track a common kebbit below level 3`() {
        val player = createPlayer(Tile(2353, 3595))
        startTrail(player)
        assertEquals(0, player["common_kebbit_trail_0", 0])
    }
}
