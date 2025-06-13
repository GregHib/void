package content.skill.ranged.ammo

import FakeRandom
import content.entity.combat.hit.hit
import content.skill.melee.CombatFormulaTest
import content.skill.ranged.ammo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import kotlin.test.assertNotEquals

internal class GodArrowsEffectTest : CombatFormulaTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = when (bitCount) {
                in 26..27 -> 0 // nextDouble()
                32 -> 25 // nextInt()
                else -> super.nextBits(bitCount)
            }
        })
    }

    @ParameterizedTest
    @ValueSource(strings = ["saradomin_arrows", "guthix_arrows", "zamorak_arrows"])
    fun `God arrows have chance of hitting extra damage`(arrows: String) {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("magic_shortbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, arrows)
        player.ammo = arrows
        val target = createPlayer(Skill.Constitution to 990)

        player.hit(target, weapon, "range", damage = 10)
        tick(4)

        assertNotEquals(990, target.levels.get(Skill.Constitution))
        assertNotEquals(980, target.levels.get(Skill.Constitution))
    }
}
