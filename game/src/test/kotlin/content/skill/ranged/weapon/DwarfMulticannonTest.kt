package content.skill.ranged.weapon

import FakeRandom
import WorldTest
import containsMessage
import content.entity.player.bank.bank
import itemOption
import objectOption
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DwarfMulticannonTest : WorldTest() {

    private val open = Tile(3210, 3229)
    private val corner = open.add(-2, -3)

    private fun cannoneer(tile: Tile = open, balls: Int = 0): Player {
        val player = createPlayer(tile)
        player["dwarf_cannon"] = "completed"
        for (part in listOf("cannon_base", "cannon_stand", "cannon_barrels", "cannon_furnace")) {
            player.inventory.add(part)
        }
        if (balls > 0) {
            player.inventory.add("cannonball", balls)
        }
        return player
    }

    private fun Player.setUp(): Player {
        itemOption("Set-up", "cannon_base")
        tick(10)
        return this
    }

    @Test
    fun `Set up a cannon through all four stages`() {
        val player = cannoneer().setUp()

        assertNotNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertEquals(0, player.inventory.count("cannon_base"))
        assertEquals(0, player.inventory.count("cannon_furnace"))
        assertEquals(corner.id, player["cannon_tile", -1])
    }

    @Test
    fun `Setting up requires the quest`() {
        val player = createPlayer(open)
        for (part in listOf("cannon_base", "cannon_stand", "cannon_barrels", "cannon_furnace")) {
            player.inventory.add(part)
        }

        player.itemOption("Set-up", "cannon_base")
        tick(10)

        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertTrue(player.containsMessage("You have no idea how to operate this machine."))
    }

    @Test
    fun `Setting up requires all four parts`() {
        val player = createPlayer(open)
        player["dwarf_cannon"] = "completed"
        player.inventory.add("cannon_base")

        player.itemOption("Set-up", "cannon_base")
        tick(10)

        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertTrue(player.containsMessage("You don't have all of the cannon parts."))
    }

    @Test
    fun `Only one cannon can be set up at a time`() {
        val player = cannoneer().setUp()
        for (part in listOf("cannon_base", "cannon_stand", "cannon_barrels", "cannon_furnace")) {
            player.inventory.add(part)
        }

        player.itemOption("Set-up", "cannon_base")
        tick(10)

        assertTrue(player.containsMessage("You can only have one cannon set up at a time."))
        assertEquals(1, player.inventory.count("cannon_base"))
    }

    @Test
    fun `Cannot set up a cannon in a banned area`() {
        val player = cannoneer(Tile(3164, 3490))

        player.itemOption("Set-up", "cannon_base")
        tick(10)

        assertTrue(player.containsMessage("heavy artillery"))
        assertEquals(1, player.inventory.count("cannon_base"))
    }

    @Test
    fun `Firing loads up to thirty cannonballs`() {
        val player = cannoneer(balls = 50).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")

        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }

        assertEquals(30, player["cannon_balls", 0])
        assertEquals(20, player.inventory.count("cannonball"))
    }

    @Test
    fun `Firing without cannonballs warns`() {
        val player = cannoneer().setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")

        player.objectOption(cannon, "Fire")
        tick(40)

        assertTrue(player.containsMessage("You need to load your cannon with cannon balls before firing it!"))
    }

    @Test
    fun `Another player cannot fire your cannon`() {
        cannoneer().setUp()
        val other = createPlayer(open.add(6, 0))
        val cannon = GameObjects.find(corner, "dwarf_multicannon")

        other.objectOption(cannon, "Fire")
        tick(40)

        assertTrue(other.containsMessage("This is not your cannon."))
    }

    @Test
    fun `Cannon rotates one octant per tick`() {
        val player = cannoneer(balls = 10).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        assertEquals(10, player["cannon_balls", 0])

        val first = player["cannon_direction", -1]
        tick()
        val second = player["cannon_direction", -1]

        assertEquals((first + 1) % 8, second)
    }

    @Test
    fun `Cannon damages a nearby npc and consumes a cannonball`() {
        val player = cannoneer(balls = 10).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        val npc = createNPC("rat", corner.add(1, 6))
        val before = npc.levels.get(Skill.Constitution)

        tickIf(20) { npc.levels.get(Skill.Constitution) == before && player["cannon_balls", 0] == 10 }

        assertTrue(player["cannon_balls", 0] < 10)
    }

    @Test
    fun `Cannon completes a revolution every eight ticks`() {
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        val start = player["cannon_direction", -1]

        val seen = mutableSetOf<Int>()
        repeat(8) {
            tick()
            seen.add(player["cannon_direction", -1])
        }

        assertEquals(8, seen.size)
        assertEquals(start, player["cannon_direction", -1])
    }

    @Test
    fun `Cannon never fires more than one cannonball per tick`() {
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        createNPC("rat", corner.add(3, 5))
        var previous = player["cannon_balls", 0]

        repeat(16) {
            tick()
            val remaining = player["cannon_balls", 0]
            assertTrue(previous - remaining <= 1, "fired more than one cannonball in a tick")
            previous = remaining
        }
    }

    @Test
    fun `A target between two wedges is fired on twice per revolution`() {
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        // 63 degrees from the cannon, inside both the north (90) and north-east (45) wedges.
        val spot = corner.add(3, 5)
        val rat = createNPC("rat", spot)
        // Direction.clockwise indexes north at 0 and north-east at 1, so parking the barrel on the
        // last octant makes the next two ticks sweep both wedges the target sits in.
        player["cannon_direction"] = 7

        val first = player["cannon_balls", 0]
        tick()
        assertEquals(1, first - player["cannon_balls", 0], "north wedge should have fired")

        // Hold the rat still - once hit it retaliates and walks, which would carry it out of the
        // second wedge before the barrel gets there.
        rat.tile = spot
        val second = player["cannon_balls", 0]
        tick()
        assertEquals(1, second - player["cannon_balls", 0], "north-east wedge should have fired")
    }

    @Test
    fun `Cannon ignores npcs immune to cannons`() {
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        val npc = createNPC("turoth", corner.add(1, 5))
        val before = npc.levels.get(Skill.Constitution)

        tick(16)

        assertEquals(before, npc.levels.get(Skill.Constitution))
    }

    @Test
    fun `Cannon ignores npcs beyond its range`() {
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        val npc = createNPC("rat", corner.add(1, 20))
        val before = npc.levels.get(Skill.Constitution)

        tick(16)

        assertEquals(before, npc.levels.get(Skill.Constitution))
        assertEquals(30, player["cannon_balls", 0])
    }

    @Test
    fun `Picking up returns the parts and remaining ammo`() {
        val player = cannoneer(balls = 5).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }

        player.objectOption(cannon, "Pick-up")
        tick(40)

        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertEquals(1, player.inventory.count("cannon_base"))
        assertEquals(1, player.inventory.count("cannon_furnace"))
        assertFalse(player.contains("cannon_tile"))
    }

    @Test
    fun `Cannon decays and returns the parts`() {
        val player = cannoneer().setUp()

        tick(2501)

        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertTrue(player.containsMessage("Your cannon has decayed"))
        assertEquals(1, player.inventory.count("cannon_base"))
        assertFalse(player.contains("cannon_tile"))
    }

    @Test
    fun `An interrupted assembly still returns the parts`() {
        val player = cannoneer()

        player.itemOption("Set-up", "cannon_base")
        tick(3)
        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        player.softTimers.stop("cannon")
        tick()

        assertEquals(1, player.inventory.count("cannon_base"))
        assertEquals(1, player.inventory.count("cannon_furnace"))
        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon_base"))
        assertFalse(player.contains("cannon_tile"))
    }

    @Test
    fun `Cannon damage grants ranged experience but no constitution experience`() {
        // WorldTest pins randomness to zero, which would make every cannonball roll 0 damage.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until / 2
        })
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }
        createNPC("rat", corner.add(1, 6))
        val ranged = player.experience.get(Skill.Ranged)
        val constitution = player.experience.get(Skill.Constitution)

        tickIf(60) { player.experience.get(Skill.Ranged) == ranged }

        assertTrue(player.experience.get(Skill.Ranged) > ranged)
        assertEquals(constitution, player.experience.get(Skill.Constitution))
    }

    @Test
    fun `Logging out loses the cannon and owes a replacement`() {
        val player = cannoneer(balls = 30).setUp()
        val cannon = GameObjects.find(corner, "dwarf_multicannon")
        player.objectOption(cannon, "Fire")
        tickIf(50) { player["cannon_balls", 0] == 0 }

        player.softTimers.stopAll()
        tick()

        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertEquals(0, player.inventory.count("cannon_base"), "parts should not come back on logout")
        assertEquals(4, player["cannon_lost_parts", 0])
        assertEquals(30, player["cannon_lost_balls", 0])
        assertFalse(player.contains("cannon_tile"))
    }

    @Test
    fun `A decayed cannon goes to the bank when the inventory is full`() {
        val player = cannoneer().setUp()
        repeat(28) { player.inventory.add("iron_bar") }

        tick(2501)

        assertNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
        assertEquals(0, player.inventory.count("cannon_base"))
        assertEquals(1, player.bank.count("cannon_base"))
        assertEquals(1, player.bank.count("cannon_furnace"))
        assertTrue(player.containsMessage("sent to your bank"))
    }

    @Test
    fun `Cannon warns before decaying`() {
        val player = cannoneer().setUp()

        tick(2001)

        assertTrue(player.containsMessage("Your cannon is starting to decay."))
        assertNotNull(GameObjects.findOrNull(corner, "dwarf_multicannon"))
    }
}
