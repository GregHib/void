package content.skill.hunter

import FakeRandom
import WorldTest
import containsMessage
import itemOption
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BirdSnareTest : WorldTest() {
    @ParameterizedTest
    @ValueSource(strings = ["crimson_swift", "golden_warbler", "copper_longtail", "cerulean_twitch", "tropical_wagtail", "wimpy_bird"])
    fun `Catch a bird`(bird: String) {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("bird_snare")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "bird_snare")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "bird_snare" })
        createNPC(bird, player.tile.addY(2))

        tick(6)

        val trap = GameObjects.at(start).firstOrNull { it.id == "snare_${bird}" }
        assertNotNull(trap)

        player.objectOption(trap, "Check")
        tick(3)
        assertEquals(1, player.inventory.count("bird_snare"))
        assertEquals(1, player.inventory.count("raw_bird_meat"))
        assertEquals(1, player.inventory.count("bones"))
        assertTrue(player.inventory.items.any { it.id.contains("feather") })
    }

    @Test
    fun `Can't catch without hunter level`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("bird_snare")

        player.itemOption("Lay", "bird_snare")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "bird_snare" })
        createNPC("golden_warbler", player.tile.addY(2))

        tick(6)

        val trap = GameObjects.at(start).firstOrNull { it.id == "snare_golden_warbler" }
        assertNull(trap)
    }

    @Test
    fun `Fail to catch`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 4) 0 else until - 1
        })
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("bird_snare")
        player.levels.set(Skill.Hunter, 10)

        player.itemOption("Lay", "bird_snare")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "bird_snare" })
        createNPC("golden_warbler", player.tile.addY(2))

        tick(6)

        val trap = GameObjects.at(start).firstOrNull { it.id == "bird_snare_fail" }
        assertNotNull(trap)

        player.objectOption(trap, "Dismantle")
        tick(3)
        assertEquals(1, player.inventory.count("bird_snare"))
    }

    @Test
    fun `Can't place more than one trap at a time at level 1`() {
        val player = createPlayer()
        var start = player.tile
        player.inventory.add("bird_snare", 2)

        player.itemOption("Lay", "bird_snare")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "bird_snare" })

        start = player.tile

        player.itemOption("Lay", "bird_snare")
        tick(3)
        assertFalse(GameObjects.at(start).any { it.id == "bird_snare" })
        assertTrue(player.containsMessage("only 1 trap at a time"))
    }

}