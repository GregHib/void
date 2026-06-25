package content.activity.shooting_star

import WorldTest
import content.entity.player.bank.bank
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

internal class ShootingStarTest : WorldTest() {

    /**
     * Regression test for https://github.com/GregHib/void/issues/1043
     *
     * A player holding the maximum amount of stardust (>= 200) used to make `addOre` return -1,
     * which short-circuited `deplete()` in the mining loop so the star's layer never advanced and
     * the shared `totalCollected` counter grew without bound - an infinite star with a negative
     * "% left of this layer". A maxed-out mine must still count towards depleting the star.
     */
    @Test
    fun `Mining a star with maximum stardust still depletes a layer`() {
        setRandom(Random)
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Mining, 99)
        player.inventory.add("rune_pickaxe")
        // Maxed out on stardust so every successful mine goes through the >= 200 branch of addOre.
        player.bank.add("stardust", 200)

        val tile = emptyTile.addY(1)
        val star = createObject("crashed_star_tier_9", tile) // collect_for_next_layer = 15
        ShootingStarHandler.currentStarTile = tile
        ShootingStarHandler.currentActiveObject = star
        ShootingStarHandler.totalCollected = 14 // one successful mine away from the next layer

        player.objectOption(star, "Mine")
        // The very next stardust mined by the maxed player must advance the star to the next tier.
        tickIf(limit = 200) {
            GameObjects.getLayer(tile, ObjectLayer.GROUND)?.id == "crashed_star_tier_9"
        }

        assertEquals("crashed_star_tier_8", GameObjects.getLayer(tile, ObjectLayer.GROUND)?.id)
        assertEquals(0, ShootingStarHandler.totalCollected) // counter reset, never runs away negative
        assertEquals(0, player.inventory.count("stardust")) // none carried while maxed out
    }
}
