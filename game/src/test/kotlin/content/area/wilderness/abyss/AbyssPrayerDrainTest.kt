package content.area.wilderness.abyss

import FakeRandom
import WorldTest
import content.skill.summoning.dismissFamiliar
import content.skill.summoning.summonFamiliar
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AbyssPrayerDrainTest : WorldTest() {

    private fun createPlayerWithPrayer(tile: Tile): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Prayer, 99)
        return player
    }

    private fun Player.summonParasite() {
        levels.set(Skill.Summoning, 99)
        summonFamiliar(NPCDefinitions.get("abyssal_parasite_familiar"), restart = false)
        tick(2) // let the summon queue assign the follower
    }

    private fun Player.mageTeleport() {
        this["enter_the_abyss"] = "completed"
        val mage = createNPC("mage_of_zamorak_wilderness", tile.addY(1))
        // Force the random abyss landing spot onto a known walkable outer-ring tile
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int): Int = when (from) {
                3008 -> 3026
                4800 -> 4812
                else -> from
            }
        })
        npcOption(mage, "Teleport")
        tick(8)
    }

    @Test
    fun `Entering the Abyss drains all prayer points`() {
        val player = createPlayerWithPrayer(Tile(3102, 3556))

        player.mageTeleport()

        assertTrue(player.tile in Areas["abyss_multi_area"])
        assertEquals(0, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Abyssal parasite prevents the full drain on entry`() {
        val player = createPlayerWithPrayer(Tile(3102, 3556))
        player.summonParasite()

        player.mageTeleport()

        assertTrue(player.tile in Areas["abyss_multi_area"])
        assertEquals(99, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Parasite turns the drain into a gradual leak`() {
        val player = createPlayerWithPrayer(Tile(3102, 3556))
        player.summonParasite()

        player.tele(Tile(3026, 4812))
        tick(8)
        assertEquals(89, player.levels.get(Skill.Prayer))
        tick(8)
        assertEquals(79, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Leak stops after leaving the Abyss`() {
        val player = createPlayerWithPrayer(Tile(3102, 3556))
        player.summonParasite()

        player.tele(Tile(3026, 4812))
        tick(8)
        assertEquals(89, player.levels.get(Skill.Prayer))

        player.tele(Tile(3102, 3556))
        tick(16)
        assertEquals(89, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Losing the parasite inside the Abyss drains the rest`() {
        val player = createPlayerWithPrayer(Tile(3102, 3556))
        player.summonParasite()

        player.tele(Tile(3026, 4812))
        tick(8)
        assertEquals(89, player.levels.get(Skill.Prayer))

        player.dismissFamiliar()
        tick(8)
        assertEquals(0, player.levels.get(Skill.Prayer))
    }
}
