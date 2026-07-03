package content.skill.hunter

import FakeRandom
import WorldTest
import itemOption
import messages
import npcOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

class ImplingTest : WorldTest() {
    @Test
    fun `Catch impling with net`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 17)
        player.inventory.add("impling_jar")
        player.equipment.set(EquipSlot.Weapon.index, "butterfly_net")
        val impling = createNPC("baby_impling", player.tile.addY(1))

        player.npcOption(impling, "Catch")
        tick(3)

        assertEquals(0, player.inventory.count("impling_jar"))
        assertEquals(1, player.inventory.count("baby_impling_jar"))
        assertEquals(25.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Catch impling with bare-hands`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 84)
        player.inventory.add("impling_jar")
        val impling = createNPC("ninja_impling", player.tile.addY(1))

        player.npcOption(impling, "Catch")
        tick(3)

        println(player.messages)
        assertEquals(0, player.inventory.count("impling_jar"))
        assertEquals(1, player.inventory.count("ninja_impling_jar"))
        assertEquals(481.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Fail to catch impling`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 17)
        player.inventory.add("impling_jar")
        player.equipment.set(EquipSlot.Weapon.index, "butterfly_net")
        val impling = createNPC("baby_impling", player.tile.addY(1))

        player.npcOption(impling, "Catch")
        tick(3)

        assertEquals(1, player.inventory.count("impling_jar"))
        assertEquals(0, player.inventory.count("baby_impling_jar"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't catch impling without hunter level`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 16)
        player.inventory.add("impling_jar")
        player.equipment.set(EquipSlot.Weapon.index, "impling_net")
        val impling = createNPC("baby_impling", player.tile.addY(1))

        player.npcOption(impling, "Catch")
        tick(3)

        assertEquals(1, player.inventory.count("impling_jar"))
        assertEquals(0, player.inventory.count("baby_impling_jar"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't catch impling with bare hands without hunter level`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 26)
        player.inventory.add("impling_jar")
        val impling = createNPC("baby_impling", player.tile.addY(1))

        player.npcOption(impling, "Catch")
        tick(3)

        assertEquals(1, player.inventory.count("impling_jar"))
        assertEquals(0, player.inventory.count("baby_impling_jar"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "baby_impling_jar",
        "young_impling_jar",
        "gourmet_impling_jar",
        "earth_impling_jar",
        "essence_impling_jar",
        "eclectic_impling_jar",
        "nature_impling_jar",
        "magpie_impling_jar",
        "ninja_impling_jar",
        "dragon_impling_jar",
        "pirate_impling_jar",
        "zombie_impling_jar",
        "kingly_impling_jar",
    ])
    fun `Loot impling`(id: String) {
        val player = createPlayer()
        player.inventory.add(id)

        player.itemOption("Loot", id)

        assertEquals(0, player.inventory.count(id))
        assertEquals(1, player.inventory.count("impling_jar"))
        assertTrue(player.inventory.count > 1)
    }
}
