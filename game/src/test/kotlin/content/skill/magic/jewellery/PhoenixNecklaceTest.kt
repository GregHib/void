package content.skill.magic.jewellery

import WorldTest
import content.entity.combat.hit.damage
import content.entity.combat.hit.directHit
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.praying
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class PhoenixNecklaceTest : WorldTest() {

    @Test
    fun `Not triggered hit reduces doesn't reduce hp under 20 percent`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Amulet.index, "phoenix_necklace")
        player.setLevel(Skill.Constitution, 500)

        player.damage(100)
        tick()

        assertEquals("phoenix_necklace", player.equipped(EquipSlot.Amulet).id)
        assertEquals(400, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Triggered when hit reduces hp to exactly 20 percent`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Amulet.index, "phoenix_necklace")
        player.setLevel(Skill.Constitution, 250)
        player.directHit(175)

        player.damage(25)
        tick()

        assertNotEquals("phoenix_necklace", player.equipped(EquipSlot.Amulet).id)
        assertEquals(125, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Hit remaining hp, over 20 percent doesn't kill player`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Amulet.index, "phoenix_necklace")
        player.setLevel(Skill.Constitution, 250)
        player.directHit(175)

        player.damage(75)
        tick()

        assertNotEquals("phoenix_necklace", player.equipped(EquipSlot.Amulet).id)
        assertEquals(75, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Phoenix necklace triggers before ring of life and redemption`() {
        val player = createPlayer(Tile(3000, 3000))
        player.equipment.set(EquipSlot.Amulet.index, "phoenix_necklace")
        player.equipment.set(EquipSlot.Ring.index, "ring_of_life")
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "redemption")
        player.setLevel(Skill.Constitution, 250)
        player.setLevel(Skill.Prayer, 50)
        player.directHit(175)

        player.damage(25)
        tick()

        assertNotEquals("phoenix_necklace", player.equipped(EquipSlot.Amulet).id)
        assertEquals("ring_of_life", player.equipped(EquipSlot.Ring).id)
        assertTrue(player.praying("redemption"))
        assertEquals(125, player.levels.get(Skill.Constitution))
        assertEquals(Tile(3000, 3000), player.tile)
    }

    private fun Player.setLevel(skill: Skill, level: Int) {
        levels.set(skill, level)
        experience.set(skill, Level.experience(skill, level))
    }
}
