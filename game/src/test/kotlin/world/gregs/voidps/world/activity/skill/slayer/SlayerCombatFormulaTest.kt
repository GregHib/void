package world.gregs.voidps.world.activity.skill.slayer

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.skill.melee.CombatFormulaTest
import kotlin.test.assertEquals

class SlayerCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Salve amulet boost melee on undead target`() {
        val player = createPlayer(Skill.Attack to 50, Skill.Strength to 50)
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet")
        val npc = createNPC("zombie")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(4554, offensiveRating)
        assertEquals(1216, defensiveRating)
        assertEquals(73, maxHit)
        assertEquals(0.8663, chance, 0.0001)
    }

    @Test
    fun `Salve amulet no boost melee on regular target`() {
        val player = createPlayer(Skill.Attack to 50, Skill.Strength to 50)
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet_e")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(3904, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(63, maxHit)
        assertEquals(0.3388, chance, 0.0001)
    }

    @Test
    fun `Salve amulet no boost with magic or ranged`() {
        val player = createPlayer(Skill.Magic to 50)
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet")
        val npc = createNPC("zombie")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "wind_strike")

        assertEquals(3776, offensiveRating)
        assertEquals(640, defensiveRating)
        assertEquals(20, maxHit)
        assertEquals(0.915, chance, 0.0001)
    }

    @Test
    fun `Salve amulet e overrides slayer helmet boost`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet")
        player["slayer_task"] = true
        player["slayer_type"] = "zombie"
        val npc = createNPC("zombie")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(6421, offensiveRating)
        assertEquals(1216, defensiveRating)
        assertEquals(130, maxHit)
        assertEquals(0.9052, chance, 0.0001)
    }


    @Test
    fun `Slayer helmet not on task`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.4777, chance, 0.0001)
    }

    @Test
    fun `Slayer helmet ranged on slayer task`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        player["slayer_task"] = true
        player["slayer_type"] = "demon"
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "ranged")

        assertEquals(5504, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.4777, chance, 0.0001)
    }

    @Test
    fun `Slayer helmet on slayer task`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        player["slayer_task"] = true
        player["slayer_type"] = "demon"
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(6421, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(130, maxHit)
        assertEquals(0.5513, chance, 0.0001)
    }

    @Test
    fun `Full slayer helmet ranged on slayer task`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Ranged to 99)
        player.equipment.set(EquipSlot.Hat.index, "full_slayer_helmet")
        player["slayer_task"] = true
        player["slayer_type"] = "demon"
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range")

        assertEquals(8475, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(132, maxHit)
        assertEquals(0.66, chance, 0.0001)
    }

    @Test
    fun `Slayer dart max hit`() {
        val player = createPlayer(Skill.Magic to 75)
        player.equipment.set(EquipSlot.Weapon.index, "slayers_staff")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "magic_dart")

        assertEquals(6384, offensiveRating)
        assertEquals(540, defensiveRating)
        assertEquals(175, maxHit)
        assertEquals(0.9576, chance, 0.0001)
    }
}