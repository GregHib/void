package content.skill.magic.jewellery

import WorldTest
import content.entity.combat.hit.damage
import content.entity.combat.hit.directHit
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RingOfLifeTest : WorldTest() {

    private fun Player.setLevel(skill: Skill, level: Int) {
        levels.set(skill, level)
        experience.set(skill, Level.experience(skill, level))
    }

    @Test
    fun `Hit under 10 percent remaining won't trigger rol`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Ring.index, "ring_of_life")
        player.setLevel(Skill.Constitution, 500)

        player.damage(100)
        tick()

        assertEquals("ring_of_life", player.equipped(EquipSlot.Ring).id)
        assertEquals(400, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Hit with exactly 10 percent remaining will trigger rol`() {
        val player = createPlayer(Tile(3000, 3400))
        player.equipment.set(EquipSlot.Ring.index, "ring_of_life")
        player.setLevel(Skill.Constitution, 250)
        player.directHit(200)

        player.damage(25)
        tick(6)

        assertNotEquals("ring_of_life", player.equipped(EquipSlot.Ring).id)
        assertEquals(25, player.levels.get(Skill.Constitution))
        assertEquals(Tile(3221, 3219), player.tile)
    }

    @Test
    fun `Hit for over 10 percent of life will kill player`() {
        val player = createPlayer(Tile(3000, 3400))
        player.equipment.set(EquipSlot.Ring.index, "ring_of_life")
        player.setLevel(Skill.Constitution, 250)
        player.directHit(200)

        player.damage(100)
        tick(7)

        assertNotEquals("ring_of_life", player.equipped(EquipSlot.Ring).id)
        assertEquals(250, player.levels.get(Skill.Constitution))
        assertEquals(Tile(3221, 3219), player.tile)
    }
}
