package content.bot.behaviour.condition

import FakeRandom
import content.bot.behaviour.loadout.Loadout
import content.bot.behaviour.loadout.LoadoutFixtures
import content.bot.behaviour.loadout.LoadoutFixtures.gear
import content.bot.behaviour.loadout.LoadoutFixtures.level
import content.bot.behaviour.loadout.LoadoutSlot
import content.bot.weightedRandom
import content.entity.player.bank.bank
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import set
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

class BotLoadoutTest {

    private lateinit var player: Player
    private var roll = 0

    private val weapons = gear("weapons", "rune_scimitar", "steel_scimitar", "bronze_sword")
    private val loadoutA = Loadout("a", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("w_a", "rune_scimitar"))))
    private val loadoutB = Loadout("b", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("w_b", "bronze_sword"))))
    private val high = Loadout("high", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("w_h", "dragon_2h_sword"))))

    @BeforeEach
    fun setup() {
        roll = 0
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = roll
        })
        LoadoutFixtures.define()
        player = LoadoutFixtures.player()
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
    }

    @Test
    fun `Weighted with a single option returns it`() {
        assertEquals(loadoutA, listOf(loadoutA to 5).weightedRandom())
    }

    @Test
    fun `Weighted with no options returns null`() {
        assertNull(emptyList<Pair<Loadout, Int>>().weightedRandom())
    }

    @Test
    fun `Weighted picks by roll`() {
        val options = listOf(loadoutA to 1, loadoutB to 3)
        roll = 0
        assertEquals(loadoutA, options.weightedRandom())
        roll = 1
        assertEquals(loadoutB, options.weightedRandom())
        roll = 3
        assertEquals(loadoutB, options.weightedRandom())
    }

    @Test
    fun `Weighted ignores zero weight options`() {
        roll = 0
        assertEquals(loadoutB, listOf(loadoutA to 0, loadoutB to 2).weightedRandom())
    }

    @Test
    fun `Weighted with no positive weights returns the first`() {
        assertEquals(loadoutA, listOf(loadoutA to 0, loadoutB to 0).weightedRandom())
    }

    @Test
    fun `Pick filters by qualifies`() {
        val condition = BotLoadout(listOf(loadoutA to 1, loadoutB to 1))
        roll = 0
        assertEquals(loadoutB, condition.pick(player))
        level(player, Skill.Attack, 40)
        assertEquals(loadoutA, condition.pick(player))
        roll = 1
        assertEquals(loadoutB, condition.pick(player))
    }

    @Test
    fun `Pick returns null when none qualify`() {
        assertNull(BotLoadout(listOf(high to 1)).pick(player))
    }

    @Test
    fun `Pick with any falls back to all loadouts when none qualify`() {
        val other = Loadout("other", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("w_o", "rune_scimitar", "dragon_2h_sword"))))
        val condition = BotLoadout(listOf(high to 1, other to 1))
        assertNull(condition.pick(player))
        roll = 1
        assertEquals(other, condition.pick(player, any = true))
    }

    @Test
    fun `Unbound check is true when wearing a candidate for every required slot`() {
        val loadout = Loadout("l", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = weapons), EquipSlot.Shield to LoadoutSlot(gear = gear("s", "wooden_shield"), optional = true)))
        val condition = BotLoadout(listOf(loadout to 1))
        assertFalse(condition.check(player))
        player.equipment.set(EquipSlot.Weapon.index, "steel_scimitar")
        assertTrue(condition.check(player))
    }

    @Test
    fun `Unbound check is false when wearing a non candidate`() {
        val condition = BotLoadout(listOf(loadoutA to 1))
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        assertFalse(condition.check(player))
    }

    @Test
    fun `Unbound check requires every required slot`() {
        val loadout = Loadout("l", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = weapons), EquipSlot.Shield to LoadoutSlot(gear = gear("s", "wooden_shield"))))
        val condition = BotLoadout(listOf(loadout to 1))
        player.equipment.set(EquipSlot.Weapon.index, "steel_scimitar")
        assertFalse(condition.check(player))
        player.equipment.set(EquipSlot.Shield.index, "wooden_shield")
        assertTrue(condition.check(player))
    }

    @Test
    fun `Unbound check accepts any of the options`() {
        val condition = BotLoadout(listOf(loadoutA to 1, loadoutB to 1))
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        assertTrue(condition.check(player))
    }

    @Test
    fun `Unbound check includes loadout inventory`() {
        val loadout = Loadout("l", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = weapons)), inventory = listOf(BotItem(setOf("shark"), min = 1)))
        val condition = BotLoadout(listOf(loadout to 1))
        player.equipment.set(EquipSlot.Weapon.index, "steel_scimitar")
        assertFalse(condition.check(player))
        player.inventory.add("shark")
        assertTrue(condition.check(player))
    }

    @Test
    fun `Keys list candidate items`() {
        assertEquals(setOf("item:rune_scimitar", "item:bronze_sword"), BotLoadout(listOf(loadoutA to 1, loadoutB to 1)).keys())
    }

    @Test
    fun `Qualified events contain skill of gated weapons`() {
        val condition = BotLoadoutQualified(listOf(Loadout("l", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("w", "rune_scimitar", "steel_scimitar"))))))
        assertTrue(condition.events().contains("skill:attack"))
        assertTrue(condition.keys().contains("skill:attack"))
    }

    @Test
    fun `Qualified has no events when ungated`() {
        assertTrue(BotLoadoutQualified(listOf(loadoutB)).events().isEmpty())
    }

    @Test
    fun `Qualified check is true when any loadout qualifies`() {
        val condition = BotLoadoutQualified(listOf(high, loadoutA))
        assertFalse(condition.check(player))
        level(player, Skill.Attack, 40)
        assertTrue(condition.check(player))
    }

    @Test
    fun `Grant for qualified condition raises levels`() {
        Condition.grant(player, BotLoadoutQualified(listOf(loadoutA)))
        assertEquals(40, player.levels.getMax(Skill.Attack))
    }

    @Test
    fun `Grant for loadout condition banks the loadout`() {
        val loadout = Loadout("l", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = weapons)), inventory = listOf(BotItem(setOf("shark"), min = 2)))
        level(player, Skill.Attack, 5)
        Condition.grant(player, BotLoadout(listOf(loadout to 1)))
        assertEquals(1, player.bank.count("steel_scimitar"))
        assertEquals(2, player.bank.count("shark"))
    }
}
