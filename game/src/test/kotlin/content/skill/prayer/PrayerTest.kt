package content.skill.prayer

import WorldTest
import interfaceOption
import itemOnObject
import itemOption
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class PrayerTest : WorldTest() {

    @Test
    fun `Active prayers drain prayer points`() {
        val player = createPlayer()
        player.experience.set(Skill.Prayer, Experience.MAXIMUM_EXPERIENCE)

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 27)
        tick()
        assertTrue(player.praying("piety"))
        tickIf(limit = 1000) { player.levels.get(Skill.Prayer) > 0 }

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertFalse(player.praying("piety"))
    }

    @Test
    fun `Active curses drain prayer points`() {
        val player = createPlayer()
        player.experience.set(Skill.Prayer, Experience.MAXIMUM_EXPERIENCE)
        player[PrayerConfigs.PRAYERS] = "curses"

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 19)
        tick()
        assertTrue(player.praying("turmoil"))
        tickIf(limit = 1000) { player.levels.get(Skill.Prayer) > 0 }

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertFalse(player.praying("turmoil"))
    }

    private val bones = listOf(
        "bones",
        "wolf_bones",
        "big_bones",
        "dragon_bones",
        "babydragon_bones",
        "frost_dragon_bones",
        "wyvern_bones",
    )

    @TestFactory
    fun `Bury bone grants prayer experience`() = bones.map { bone ->
        dynamicTest("Bury ${bone.toSentenceCase()}") {
            val player = createPlayer()
            player.inventory.add(bone)

            player.itemOption("Bury", bone)
            tick()

            assertNotEquals(0.0, player.experience.get(Skill.Prayer))
            assertFalse(player.inventory.contains(bone))
        }
    }

    @TestFactory
    fun `Offering bones on altar grants prayer experience`() = bones.map { bone ->
        dynamicTest("Offer ${bone.toSentenceCase()}") {
            val player = createPlayer(Tile(3244, 3207))
            player.inventory.add(bone)
            val altar = GameObjects.find(Tile(3243, 3206), "prayer_altar_lumbridge")

            player.itemOnObject(altar, 0)
            tick(2)

            assertNotEquals(0.0, player.experience.get(Skill.Prayer))
            assertFalse(player.inventory.contains(bone))
        }
    }

    @Test
    fun `Offering bones at the Chaos Temple altar grants bonus experience`() {
        val player = createPlayer(Tile(3239, 3607))
        player.inventory.add("bones")
        val altar = GameObjects.find(Tile(3239, 3608), "prayer_altar_chaos_wilderness")

        player.itemOnObject(altar, 0)
        tick(2)

        assertEquals(15.7, player.experience.get(Skill.Prayer))
    }

    @Test
    fun `Offering bones at the Chaos Temple hut altar grants bonus experience`() {
        val player = createPlayer(Tile(2948, 3820))
        player.inventory.add("bones")
        val altar = GameObjects.find(Tile(2947, 3820), "prayer_altar_chaos")

        player.itemOnObject(altar, 0)
        tick(2)

        assertEquals(15.7, player.experience.get(Skill.Prayer))
    }

    @Test
    fun `Offering bones at a chaos altar outside the Wilderness grants no bonus experience`() {
        val player = createPlayer(Tile(2454, 3232))
        player.inventory.add("bones")
        val altar = GameObjects.find(Tile(2454, 3231), "prayer_altar_chaos")

        player.itemOnObject(altar, 0)
        tick(2)

        assertEquals(4.5, player.experience.get(Skill.Prayer))
    }
}
