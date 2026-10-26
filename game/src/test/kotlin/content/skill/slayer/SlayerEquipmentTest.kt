package content.skill.slayer

import FakeRandom
import WorldTest
import content.entity.combat.Target
import content.entity.player.equip.Equipment
import content.skill.ranged.ammo
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SlayerEquipmentTest : WorldTest() {

    @Test
    fun `Broad arrows damage turoths`() {
        val player = createPlayer(Tile(3200, 3200))
        player.ammo = "broad_arrow"
        val turoth = createNPC("turoth", player.tile.addY(1))

        assertEquals(10, Target.damageLimitModifiers(player, turoth, 10, "range"))

        player.ammo = "rune_arrow"
        assertEquals(0, Target.damageLimitModifiers(player, turoth, 10, "range"))
    }

    @Test
    fun `Masked earmuffs protect against dust devils`() {
        assertTrue(Equipment.isFaceMask("masked_earmuffs"))
        assertTrue(Equipment.isFaceMask("full_slayer_helmet"))
    }

    @Test
    fun `Slayer helmets protect against wall beasts`() {
        assertTrue(Equipment.isSpinyHelmet("spiny_helmet"))
        assertTrue(Equipment.isSpinyHelmet("slayer_helmet"))
        assertTrue(Equipment.isSpinyHelmet("full_slayer_helmet"))
        assertFalse(Equipment.isSpinyHelmet("earmuffs"))
    }

    @Test
    fun `Insulated boots stop killerwatts electrocuting`() {
        val player = createPlayer(Tile(3200, 3200))
        val killerwatt = createNPC("killerwatt", player.tile.addY(1))

        assertTrue(CombatApi.condition(killerwatt, player, "not_insulated"))
        assertFalse(CombatApi.condition(killerwatt, player, "insulated"))

        player.equipment.set(EquipSlot.Feet.index, "insulated_boots")
        assertFalse(CombatApi.condition(killerwatt, player, "not_insulated"))
        assertTrue(CombatApi.condition(killerwatt, player, "insulated"))
    }

    @Test
    fun `Ice strykewyrms need a fire cape or the reward`() {
        val player = createPlayer(Tile(3200, 3200))
        player.slayerTask = "ice_strykewyrm"
        val mound = createNPC("mound_ice_strykewyrm", player.tile.addY(1))

        assertFalse(CombatApi.canAttack(player, mound))

        player.equipment.set(EquipSlot.Cape.index, "fire_cape")
        assertTrue(CombatApi.canAttack(player, mound))

        player.equipment.clear(EquipSlot.Cape.index)
        player["strike_wyrms"] = true
        assertTrue(CombatApi.canAttack(player, mound))
    }

    @Test
    fun `Ice strykewyrm tasks are only assigned to players with a fire cape or the reward`() {
        val player = maxedPlayer()
        assertFalse("ice_strykewyrm" in player.possibleTasks("kuradal"))

        player.inventory.add("fire_cape")
        assertTrue("ice_strykewyrm" in player.possibleTasks("kuradal"))

        player.inventory.clear()
        player["strike_wyrms"] = true
        assertTrue("ice_strykewyrm" in player.possibleTasks("kuradal"))
    }

    private fun Player.possibleTasks(master: String): Set<String> {
        val tasks = mutableSetOf<String>()
        for (i in 0 until 500) {
            setRandom(object : FakeRandom() {
                override fun nextInt(until: Int): Int = i % until
            })
            slayerTask = "nothing"
            slayerLastTask = "nothing"
            tasks.add(assignTask(this, master)?.first ?: continue)
        }
        return tasks
    }

    private fun maxedPlayer(): Player {
        val player = createPlayer(Tile(3200, 3200))
        for (skill in Skill.entries) {
            player.levels.set(skill, skill.maximum())
            player.experience.set(skill, Level.experience(skill, skill.maximum()))
        }
        return player
    }
}
