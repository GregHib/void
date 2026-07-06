package content.skill.summoning

import WorldTest
import containsMessage
import itemOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class SummoningLevelGateTest : WorldTest() {

    @Test
    fun `Drained summoning points don't block summoning a familiar`() {
        val player = createPlayer(Tile(2523, 3056))
        player.experience.set(Skill.Summoning, 14_000_000.0) // real level 99
        player.levels.set(Skill.Summoning, 30) // points pool drained below the level-95 requirement
        player.inventory.add("iron_titan_pouch")

        player.itemOption("Summon", "iron_titan_pouch")
        tick(3)

        assertFalse(player.containsMessage("not high enough"), "the requirement checks the real level, not remaining points")
        assertNotNull(player.follower, "the familiar is summoned")
    }

    @Test
    fun `The real summoning level still gates the pouch`() {
        val player = createPlayer(Tile(2523, 3056))
        player.levels.set(Skill.Summoning, 99) // a full points pool can't stand in for the level
        player.inventory.add("iron_titan_pouch")

        player.itemOption("Summon", "iron_titan_pouch")
        tick(3)

        assertTrue(player.containsMessage("not high enough"), "the owner is told why nothing happened")
        assertNull(player.follower, "no familiar is summoned")
    }
}
