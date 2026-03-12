package content.skill.magic.book.modern

import WorldTest
import containsMessage
import content.entity.player.dialogue.continueDialogue
import continueDialogue
import interfaceOnPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class TeleportOtherTest : WorldTest() {

    @ParameterizedTest
    @ValueSource(strings = ["lumbridge", "falador", "camelot"])
    fun `Teleport other`(location: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        val target = createPlayer(player.tile.addY(1))
        giveRunes(location, player)
        player.interfaceOnPlayer("modern_spellbook", "teleother_$location", target)
        tick(1)
        assertEquals("teleport_other", target.menu)
        target.continueDialogue("teleport_other", "continue", "Continue", 0)
        tick(4)
        assertTrue(target.tile in Areas["${location}_teleport"])
        assertNotEquals(0.0, player.experience.get(Skill.Magic))
        assertEquals(0.0, target.experience.get(Skill.Magic))
    }

    @ParameterizedTest
    @ValueSource(strings = ["lumbridge", "falador", "camelot"])
    fun `Can't teleport other without level`(location: String) {
        val player = createPlayer()
        val target = createPlayer(player.tile.addY(1))
        giveRunes(location, player)
        player.interfaceOnPlayer("modern_spellbook", "teleother_$location", target)
        tick(1)
        assertNull(target.menu)
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertEquals(0.0, target.experience.get(Skill.Magic))
    }

    @ParameterizedTest
    @ValueSource(strings = ["lumbridge", "falador", "camelot"])
    fun `Can't teleport other without runes`(location: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        val target = createPlayer(player.tile.addY(1))
        player.interfaceOnPlayer("modern_spellbook", "teleother_$location", target)
        tick(1)
        assertNull(target.menu)
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertEquals(0.0, target.experience.get(Skill.Magic))
    }

    @ParameterizedTest
    @ValueSource(strings = ["lumbridge", "falador", "camelot"])
    fun `Can't teleport other when busy`(location: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        giveRunes(location, player)
        val target = createPlayer(player.tile.addY(1))
        target.mode = Follow(target, player)
        player.interfaceOnPlayer("modern_spellbook", "teleother_$location", target)
        tick(1)
        assertNull(target.menu)
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertTrue(player.containsMessage("That player is busy"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["lumbridge", "falador", "camelot"])
    fun `Can't teleport other without aid`(location: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        giveRunes(location, player)
        val target = createPlayer(player.tile.addY(1))
        target["accept_aid"] = false
        player.interfaceOnPlayer("modern_spellbook", "teleother_$location", target)
        tick(1)
        assertNull(target.menu)
        assertEquals(0.0, player.experience.get(Skill.Magic))
        assertTrue(player.containsMessage("That player won't let you"))
    }

    private fun giveRunes(location: String, player: Player) {
        when (location) {
            "lumbridge" -> {
                player.inventory.add("soul_rune")
                player.inventory.add("law_rune")
                player.inventory.add("earth_rune")
            }
            "falador" -> {
                player.inventory.add("soul_rune")
                player.inventory.add("law_rune")
                player.inventory.add("water_rune")
            }
            "camelot" -> {
                player.inventory.add("soul_rune", 2)
                player.inventory.add("law_rune")
            }
        }
    }
}
