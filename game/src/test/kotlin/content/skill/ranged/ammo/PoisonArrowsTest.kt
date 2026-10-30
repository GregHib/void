package content.skill.ranged.ammo

import FakeRandom
import WorldTest
import interfaceOption
import npcOption
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

class PoisonArrowsTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 100
        })
    }

    @ParameterizedTest
    @ValueSource(strings = ["rune_arrow", "bronze_arrow_p", "iron_arrow_p+", "steel_arrow_p++", "mithril_arrow_p", "adamant_arrow_p+", "rune_arrow_p++", "dragon_arrow_p"])
    fun `Fire poisoned arrows from a bow`(arrows: String) {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))
        npc.huntMode = ""
        player.experience.set(Skill.Ranged, Level.experience(99))
        player.levels.set(Skill.Ranged, 99)
        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, arrows, 100)

        player.interfaceOption("combat_styles", "style3", "Select")
        player.npcOption(npc, "Attack")
        tick(5)

        assertTrue(player.equipment[EquipSlot.Ammo.index].amount < 100)
    }
}
