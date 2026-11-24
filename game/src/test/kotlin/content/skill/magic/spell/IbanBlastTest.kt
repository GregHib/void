package content.skill.magic.spell

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class IbanBlastTest : WorldTest() {

    @Test
    fun `Casting iban blast uses a staff charge`() {
        val player = createPlayer(Tile(3222, 3218))
        player.levels.set(Skill.Magic, 50)
        player.equipment.set(EquipSlot.Weapon.index, "ibans_staff")
        player["ibans_staff_charges"] = 2
        player.inventory.add("fire_rune", 5)
        player.inventory.add("death_rune", 1)

        assertTrue(player.removeSpellItems("iban_blast"))

        assertEquals(1, player["ibans_staff_charges", 0])
        assertEquals(0, player.inventory.count("death_rune"))
    }

    @Test
    fun `A new iban staff starts with full charges`() {
        val player = createPlayer(Tile(3222, 3218))
        player.levels.set(Skill.Magic, 50)
        player.equipment.set(EquipSlot.Weapon.index, "ibans_staff")
        player.inventory.add("fire_rune", 5)
        player.inventory.add("death_rune", 1)

        assertTrue(player.removeSpellItems("iban_blast"))

        assertEquals(119, player["ibans_staff_charges", 0])
    }

    @Test
    fun `Checking for iban blast runes doesn't use a charge`() {
        val player = createPlayer(Tile(3222, 3218))
        player.levels.set(Skill.Magic, 50)
        player.equipment.set(EquipSlot.Weapon.index, "ibans_staff")
        player["ibans_staff_charges"] = 2
        player.inventory.add("fire_rune", 5)
        player.inventory.add("death_rune", 1)

        assertTrue(player.hasSpellItems("iban_blast"))

        assertEquals(2, player["ibans_staff_charges", 0])
    }

    @Test
    fun `Can't cast iban blast without the staff`() {
        val player = createPlayer(Tile(3222, 3218))
        player.levels.set(Skill.Magic, 50)
        player.inventory.add("fire_rune", 5)
        player.inventory.add("death_rune", 1)

        assertFalse(player.removeSpellItems("iban_blast"))
    }

    @Test
    fun `Dark mage repairs a broken iban staff`() {
        val player = createPlayer(Tile(2457, 3310))
        player.inventory.add("ibans_staff_broken")
        player.inventory.add("coins", 200_000)
        val mage = createNPC("dark_mage_underground_pass", Tile(2457, 3309))

        player.npcOption(mage, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("ibans_staff"))
        assertEquals(0, player.inventory.count("ibans_staff_broken"))
        assertEquals(0, player.inventory.count("coins"))
    }
}
