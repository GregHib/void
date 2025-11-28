package content.entity.player.dialogue.type

import FakeRandom
import WorldTest
import dialogueContinue
import interfaceOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

class LevelUpInteractTest : WorldTest() {

    @Test
    fun `Level up pauses woodcutting`() {
        setRandom(object : FakeRandom() {
            override fun nextDouble(): Double = 255.0
        })
        val player = createPlayer(emptyTile)
        player.levels.set(Skill.Woodcutting, 15)
        player.experience.set(Skill.Woodcutting, 27400)
        player.clear("skip_level_up")
        player.inventory.add("steel_hatchet")

        val tile = emptyTile.addY(1)
        val tree = createObject("oak", tile)

        player.objectOption(tree, "Chop down")
        tickIf { player.dialogue == null }
        assertEquals("dialogue_level_up", player.dialogue)
        assertEquals(1, player.inventory.count("oak_logs"))
        player.dialogueContinue()
        tick(10)
        assertEquals(3, player.inventory.count("oak_logs"))
    }

    @Test
    fun `Level up pauses magic attack`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int) = until
        })
        val player = createPlayer(emptyTile)
        val npc = createNPC("magic_dummy", emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "staff_of_air")
        player.experience.set(Skill.Magic, 80.0)
        player.inventory.add("mind_rune", 100)
        player.clear("skip_level_up")

        player.interfaceOption("modern_spellbook", "wind_strike", option = "Autocast")
        player.npcOption(npc, "Attack")
        tickIf { player.dialogue == null }
        assertEquals("dialogue_level_up", player.dialogue)
        assertEquals(99, player.inventory.count("mind_rune"))
        player.dialogueContinue()
        tick(10)
        assertEquals(97, player.inventory.count("mind_rune"))
    }
}