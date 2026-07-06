package content.skill.hunter

import WorldTest
import content.entity.combat.attacker
import content.entity.combat.inCombat
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse

class PitfallTest : WorldTest() {

    @Test
    fun `Catch a horned graahk`() {
        val player = createPlayer(Tile(2777, 3003))
        val pit = GameObjects.find(Tile(2777, 3001), "pitfall_horned_graahk_14")
        player.inventory.add("knife")
        player.inventory.add("logs")
        player.inventory.add("teasing_stick")
        player.levels.set(Skill.Hunter, 99)

        player.objectOption(pit, "Trap")
        tick(2)
        assertEquals("spiked", player["pitfall_horned_graahk_14", "empty"])

        val graahk = createNPC("horned_graahk", Tile(2777, 3004))

        player.npcOption(graahk, "Tease")
        tick(2)

        player.objectOption(pit, "Jump")
        tick(10)
        assertEquals(Tile(2777, 3000), player.tile)
        assertEquals("inverse", player["pitfall_horned_graahk_14", "empty"])

        player.objectOption(pit, "Dismantle")
        tick(2)

        assertEquals(1, player.inventory.count("big_bones"))
        assertEquals(1, player.inventory.count("tatty_graahk_fur"))
        assertNotEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't tease without hunter level`() {
        val player = createPlayer(Tile(2777, 3003))
        val graahk = createNPC("horned_graahk", Tile(2777, 3004))
        player.inventory.add("teasing_stick")
        player.levels.set(Skill.Hunter, 40)
        player.npcOption(graahk, "Tease")
        tick(2)

        assertFalse(graahk.inCombat)
        assertNull(player.attacker)
    }

    @Test
    fun `Can't setup pitfall trap without hunter level`() {
        val player = createPlayer(Tile(2777, 3003))
        val pit = GameObjects.find(Tile(2777, 3001), "pitfall_horned_graahk_14")
        player.inventory.add("knife")
        player.inventory.add("logs")
        player.inventory.add("teasing_stick")
        player.levels.set(Skill.Hunter, 40)

        player.objectOption(pit, "Trap")
        tick(2)
        assertEquals("empty", player["pitfall_horned_graahk_14", "empty"])
    }
}
