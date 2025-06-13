package content.skill.magic

import WorldTest
import dialogueOption
import interfaceOption
import itemOnItem
import itemOption
import npcOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

abstract class DungeoneeringBoxTest : WorldTest() {

    abstract val spell: String
    abstract val runes: List<Item>
    abstract val box: String
    abstract val mode: Boolean

    @Test
    fun `Charge with runes in inventory`() {
        val player = createPlayer(emptyTile)
        player["${box}_mode"] = mode
        player.inventory.set(0, box, 0)
        player.inventory.add(runes)

        player.itemOption("Charge", box)

        for (rune in runes) {
            assertFalse(player.inventory.contains(rune.id, rune.amount))
        }
        assertEquals(1, player.inventory.charges(player, 0))
    }

    @Test
    fun `Charge using runes on box`() {
        val player = createPlayer(emptyTile)
        player["${box}_mode"] = mode
        player.inventory.set(0, box, 0)
        player.inventory.add(runes)

        player.itemOnItem(1, 0)
        println(player.inventory.items)

        for (rune in runes) {
            assertFalse("Expected no $rune in inventory, ${player.inventory.count(rune.id)} found.") {
                player.inventory.contains(rune.id, rune.amount)
            }
        }
        assertEquals(1, player.inventory.charges(player, 0))
    }

    @Test
    fun `Switch box mode`() {
        val player = createPlayer(emptyTile)
        player["${box}_mode"] = mode
        player.inventory.set(0, box, 0)

        player.itemOption("Check/Empty", box)
        player.dialogueOption("line2")

        assertEquals(!mode, player["${box}_mode", false])
    }

    @Test
    fun `Can't charge with not enough runes`() {
        val player = createPlayer(emptyTile)
        player["${box}_mode"] = mode
        player.inventory.set(0, box, 0)

        player.itemOption("Charge", box)

        for (rune in runes) {
            assertFalse(player.inventory.contains(rune.id, rune.amount))
        }
        assertEquals(0, player.inventory.charges(player, 0))
    }

    @ParameterizedTest
    @ValueSource(strings = ["line1", "line2"])
    fun `Empty runes from box`(option: String) {
        val player = createPlayer(emptyTile)
        player["${box}_mode"] = mode
        player.inventory.set(0, box, 2)

        player.itemOption("Check/Empty", box)
        player.dialogueOption(option)

        for (rune in runes) {
            assertTrue("Expected $rune in inventory, ${player.inventory.count(rune.id)} found.") {
                player.inventory.contains(rune.id, rune.amount * 2)
            }
        }
        assertEquals(0, player.inventory.charges(player, 0))
    }

    @ParameterizedTest
    @ValueSource(strings = ["line1", "line2"])
    fun `Can't empty with no free space`(option: String) {
        val player = createPlayer(emptyTile)
        player["${box}_mode"] = mode
        player.inventory.set(0, box, 2)
        player.inventory.add("shark", 27)

        player.itemOption("Check/Empty", box)
        player.dialogueOption(option)

        for (rune in runes) {
            assertFalse(player.inventory.contains(rune.id))
        }
        assertEquals(2, player.inventory.charges(player, 0))
    }

    @Test
    fun `Doesn't use spells if in inventory`() {
        setRandom(Random)
        val player = createPlayer(emptyTile)
        val npc = createNPC("rat", emptyTile.addY(1))
        player["${box}_mode"] = mode
        player.inventory.set(EquipSlot.Shield.index, box, 10)
        player.levels.set(Skill.Magic, 99)
        for (rune in runes) {
            player.inventory.add(rune.id, rune.amount * 10)
        }

        player.interfaceOption("modern_spellbook", "wind_$spell", option = "Autocast")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }

        assertNotEquals(0.0, player.experience.get(Skill.Magic))
        for (rune in runes) {
            assertTrue(player.inventory.count(rune.id) < rune.amount * 10)
        }
        assertEquals(10, player.inventory.charges(player, EquipSlot.Shield.index))
    }

    @Test
    fun `Uses charges in combat`() {
        setRandom(Random)
        val player = createPlayer(emptyTile)
        val npc = createNPC("rat", emptyTile.addY(1))
        player["${box}_mode"] = mode
        player.equipment.set(EquipSlot.Shield.index, box, 10)
        player.levels.set(Skill.Magic, 99)
        for (rune in runes) {
            player.inventory.add(rune.id, rune.amount * 10)
        }
        player.inventory.add("fire_rune", 100)

        player.interfaceOption("modern_spellbook", "fire_$spell", option = "Autocast")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }

        assertNotEquals(0.0, player.experience.get(Skill.Magic))
        for (rune in runes) {
            assertTrue("Expected $rune in inventory, ${player.inventory.count(rune.id)} found.") {
                player.inventory.contains(rune.id, rune.amount * 10)
            }
        }
        assertNotEquals(100, player.inventory.count("fire_rune"))
        assertNotEquals(10, player.equipment.charges(player, EquipSlot.Shield.index))
    }
}
