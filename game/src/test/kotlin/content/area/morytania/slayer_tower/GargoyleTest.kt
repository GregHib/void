package content.area.morytania.slayer_tower

import WorldTest
import containsMessage
import content.entity.combat.attacker
import content.entity.combat.dead
import itemOnNpc
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GargoyleTest : WorldTest() {

    @Test
    fun `Smash gargoyle with item on item`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 75)
        player.inventory.add("rock_hammer")
        val gargoyle = npcs.add("gargoyle", emptyTile.addY(1))
        tick()
        gargoyle.levels.set(Skill.Constitution, 50)

        player.itemOnNpc(gargoyle, 0)

        tick(2)

        assertTrue(gargoyle.dead)
    }

    @Test
    fun `Smash gargoyle with right click`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 75)
        player.inventory.add("rock_hammer")
        val gargoyle = npcs.add("gargoyle", emptyTile.addY(1))
        tick()
        gargoyle.levels.set(Skill.Constitution, 50)

        player.npcOption(gargoyle, "Smash")

        tick(2)

        assertTrue(gargoyle.dead)
    }

    @Test
    fun `Smash gargoyle with killing blow`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 75)
        player.inventory.add("rock_hammer")
        player["killing_blow"] = true
        val gargoyle = npcs.add("gargoyle", emptyTile.addY(1))
        tick()
        gargoyle.levels.set(Skill.Constitution, 50)

        player.npcOption(gargoyle, "Attack")

        tick(3)

        assertTrue(gargoyle.dead)
    }

    @Test
    fun `Can't smash gargoyle with without a rock hammer`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 75)
        player["killing_blow"] = true
        val gargoyle = npcs.add("gargoyle", emptyTile.addY(1))
        tick()
        gargoyle.levels.set(Skill.Constitution, 50)

        player.npcOption(gargoyle, "Attack")

        tick(3)

        assertTrue(player.containsMessage("You need a rock hammer"))
        assertFalse(gargoyle.dead)
    }

    @Test
    fun `Can't smash gargoyle with high health`() {
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 75)
        player.inventory.add("rock_hammer")
        val gargoyle = npcs.add("gargoyle", emptyTile.addY(1))
        tick()

        player.npcOption(gargoyle, "Smash")

        tick(3)

        assertTrue(player.containsMessage("The gargoyle isn't weak enough"))
        assertFalse(gargoyle.dead)
    }

    @Test
    fun `Can't smash someone else's gargoyle`() {
        val player = createPlayer(emptyTile)
        val other = createPlayer(emptyTile)
        player.levels.set(Skill.Constitution, 50)
        player.levels.set(Skill.Slayer, 75)
        player.inventory.add("rock_hammer")
        val gargoyle = npcs.add("gargoyle", emptyTile.addY(1))
        tick()
        gargoyle.levels.set(Skill.Constitution, 50)

        gargoyle.start("under_attack", 100)
        gargoyle.attacker = other

        player.npcOption(gargoyle, "Smash")

        tick(3)

        assertTrue(player.containsMessage("Someone else is fighting that."))
    }
}
