package content.skill.magic.spell

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MagicDartRequirementTest : WorldTest() {

    @Test
    fun `Magic dart requires 55 slayer`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 99)
        player.levels.set(Skill.Slayer, 54)
        player.equipment.set(EquipSlot.Weapon.index, "slayers_staff")
        player.inventory.add("death_rune", 5)
        player.inventory.add("mind_rune", 20)

        assertFalse(player.hasSpellItems("magic_dart", message = false))

        player.levels.set(Skill.Slayer, 55)

        assertTrue(player.hasSpellItems("magic_dart", message = false))
    }
}
