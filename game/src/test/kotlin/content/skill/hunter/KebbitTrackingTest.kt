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

    // With FakeRandom the desert trail is always burrow -> trail_7 -> inv trail_3 -> trail_4 -> trail_5 -> trail_6,
    // advanced at sand (3393,3122), rockslide (3405,3122), cactus (3409,3121), ending at sand (3414,3121)
    @Test
    fun `Track and catch a desert devil`() {
        val player = createPlayer(Tile(3395, 3106))
        player.inventory.add("noose_wand")
        player.inventory.add("waterskin_4", 3)
        player.levels.set(Skill.Hunter, 13)
        val burrow = createObject("desert_devil_burrow", Tile(3396, 3106))
        player.objectOption(burrow, "Inspect")
        tick(5)
        assertEquals(4, player["desert_devil_trail_7", 0])

        val sand = createObject("disturbed_sand", Tile(3393, 3122))
        player.objectOption(sand, "Search")
        tick(20)
        assertEquals(5, player["desert_devil_trail_3", 0])

        val rockslide = createObject("desert_rockslide_1", Tile(3405, 3122))
        player.objectOption(rockslide, "Inspect")
        tick(20)
        assertEquals(4, player["desert_devil_trail_4", 0])

        val cactus = createObject("desert_cactus_4", Tile(3409, 3121))
        player.objectOption(cactus, "Inspect")
        tick(15)
        assertEquals(4, player["desert_devil_trail_5", 0])

        val last = createObject("disturbed_sand", Tile(3414, 3121))
        player.objectOption(last, "Search")
        tick(15)
        assertEquals(4, player["desert_devil_trail_6", 0])
        player.objectOption(last, "Search")
        tick(5)
        assertTrue(player.containsMessage("something is moving around"))

        player.objectOption(last, "Attack")
        tick(5)
        assertEquals(1, player.inventory.count("desert_devil_fur"))
        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.inventory.count("raw_beast_meat"))
        assertEquals(66.0, player.experience.get(Skill.Hunter))
        assertEquals(0, player["desert_devil_trail_7", 0])
    }

    // The second desert segment runs inverted (sand at 3393,3122 back to the cactus at 3396,3121),
    // so following the revealed tracks to the cactus must advance the trail too
    @Test
    fun `Inspecting the object at the end of an inverted segment advances the trail`() {
        val player = createPlayer(Tile(3395, 3106))
        player.inventory.add("waterskin_4", 3)
        player.levels.set(Skill.Hunter, 13)
        val burrow = createObject("desert_devil_burrow", Tile(3396, 3106))
        player.objectOption(burrow, "Inspect")
        tick(5)
        assertEquals(4, player["desert_devil_trail_7", 0])

        val cactus = createObject("desert_cactus_2", Tile(3396, 3121))
        player.objectOption(cactus, "Inspect")
        tick(20)
        assertEquals(5, player["desert_devil_trail_3", 0])
        assertTrue(player.containsMessage("You discover some tracks nearby"))
    }

    @Test
    fun `False trail digs up an old boot`() {
        val player = createPlayer(Tile(3395, 3106))
        player.inventory.add("noose_wand")
        player.inventory.add("waterskin_4", 3)
        player.levels.set(Skill.Hunter, 13)
        val burrow = createObject("desert_devil_burrow", Tile(3396, 3106))
        player.objectOption(burrow, "Inspect")
        tick(5)

        val wrong = createObject("disturbed_sand", Tile(3400, 3114))
        player.objectOption(wrong, "Attack")
        tick(15)
        assertEquals(1, player.inventory.count("old_boot"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
        assertEquals(4, player["desert_devil_trail_7", 0])
    }

    @Test
    fun `Can't track a desert devil below level 13`() {
        val player = createPlayer(Tile(3395, 3106))
        player.inventory.add("waterskin_4", 3)
        val burrow = createObject("desert_devil_burrow", Tile(3396, 3106))
        player.objectOption(burrow, "Inspect")
        tick(5)
        assertEquals(0, player["desert_devil_trail_7", 0])
    }

    // With FakeRandom the feldip trail is always burrow -> trail_0 -> trail_4 -> trail_5,
    // advanced at the plant (2533,2882), ending at the bush (2533,2885)
    @Test
    fun `Track and catch a feldip weasel`() {
        val player = createPlayer(Tile(2524, 2889))
        player.inventory.add("noose_wand")
        player.levels.set(Skill.Hunter, 7)
        val burrow = createObject("feldip_weasel_burrow", Tile(2525, 2889))
        player.objectOption(burrow, "Inspect")
        tick(5)
        assertEquals(4, player["feldip_weasel_trail_0", 0])

        val plant = createObject("feldip_plant_5", Tile(2533, 2882))
        player.objectOption(plant, "Inspect")
        tick(15)
        assertEquals(4, player["feldip_weasel_trail_4", 0])

        val bush = createObject("weasel_bush", Tile(2533, 2885))
        player.objectOption(bush, "Search")
        tick(10)
        assertEquals(4, player["feldip_weasel_trail_5", 0])
        player.objectOption(bush, "Search")
        tick(5)
        assertTrue(player.containsMessage("something is moving around"))

        player.objectOption(bush, "Attack")
        tick(5)
        assertEquals(1, player.inventory.count("feldip_weasel_fur"))
        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.inventory.count("raw_beast_meat"))
        assertEquals(48.0, player.experience.get(Skill.Hunter))
        assertEquals(0, player["feldip_weasel_trail_0", 0])
    }

    @Test
    fun `Can't track a feldip weasel below level 7`() {
        val player = createPlayer(Tile(2524, 2889))
        val burrow = createObject("feldip_weasel_burrow", Tile(2525, 2889))
        player.objectOption(burrow, "Inspect")
        tick(5)
        assertEquals(0, player["feldip_weasel_trail_0", 0])
    }

    @Test
    fun `Can't track a common kebbit below level 3`() {
        val player = createPlayer(Tile(2353, 3595))
        startTrail(player)
        assertEquals(0, player["common_kebbit_trail_0", 0])
    }
}
