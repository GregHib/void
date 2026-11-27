package content.bot.behaviour.loadout

import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.loadout.LoadoutFixtures.gear
import content.bot.behaviour.loadout.LoadoutFixtures.level
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

class LoadoutBindTest {

    private lateinit var player: Player

    private val weapons = gear("weapons", "rune_scimitar", "steel_scimitar", "bronze_sword")
    private val twoHanders = gear("two_handers", "dragon_2h_sword", "iron_2h_sword")
    private val mixed = gear("mixed", "dragon_2h_sword", "rune_scimitar", "bronze_sword")
    private val shields = gear("shields", "rune_kiteshield", "wooden_shield")
    private val helms = gear("helms", "rune_full_helm", "iron_full_helm")

    private val never = { _: String, _: Int -> false }

    @BeforeEach
    fun setup() {
        LoadoutFixtures.define()
        player = LoadoutFixtures.player()
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
    }

    private fun loadout(vararg slots: Pair<EquipSlot, LoadoutSlot>, inventory: List<BotItem> = emptyList()) = Loadout("test", linkedMapOf(*slots), inventory)

    private fun Map<EquipSlot, BotItem>.ids(slot: EquipSlot) = getValue(slot).ids

    @Test
    fun `Best owned item wins over a better unowned one`() {
        level(player, Skill.Attack, 99)
        player.inventory.add("steel_scimitar")
        val bound = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player) { _, _ -> true }
        assertEquals(setOf("steel_scimitar"), bound.ids(EquipSlot.Weapon))
    }

    @Test
    fun `Best owned item is picked from several owned`() {
        level(player, Skill.Attack, 99)
        player.bank.add("bronze_sword")
        player.bank.add("rune_scimitar")
        val bound = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player, never)
        assertEquals(setOf("rune_scimitar"), bound.ids(EquipSlot.Weapon))
    }

    @Test
    fun `Worn items count as owned`() {
        level(player, Skill.Attack, 99)
        player.equipment.set(EquipSlot.Weapon.index, "steel_scimitar")
        val bound = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player) { _, _ -> true }
        assertEquals(setOf("steel_scimitar"), bound.ids(EquipSlot.Weapon))
    }

    @Test
    fun `Items above the bots levels are skipped`() {
        level(player, Skill.Attack, 10)
        player.inventory.add("rune_scimitar")
        player.inventory.add("steel_scimitar")
        val bound = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player, never)
        assertEquals(setOf("steel_scimitar"), bound.ids(EquipSlot.Weapon))
    }

    @Test
    fun `Required slot with nothing owned takes the first obtainable item`() {
        level(player, Skill.Attack, 99)
        val asked = mutableListOf<String>()
        val bound = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player) { id, _ ->
            asked.add(id)
            id != "rune_scimitar"
        }
        assertEquals(setOf("steel_scimitar"), bound.ids(EquipSlot.Weapon))
        assertEquals(listOf("rune_scimitar", "steel_scimitar"), asked.take(2))
    }

    @Test
    fun `Obtain is not asked about items the bot cannot equip`() {
        level(player, Skill.Attack, 10)
        val asked = mutableListOf<String>()
        loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player) { id, _ ->
            asked.add(id)
            false
        }
        assertFalse(asked.contains("rune_scimitar"))
    }

    @Test
    fun `Required slot with nothing owned or obtainable takes the best equippable item`() {
        level(player, Skill.Attack, 10)
        val bound = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons)).bind(player, never)
        assertEquals(setOf("steel_scimitar"), bound.ids(EquipSlot.Weapon))
    }

    @Test
    fun `Required slot amount is passed to obtain`() {
        level(player, Skill.Attack, 99)
        var amount = 0
        loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons, min = 3)).bind(player) { _, a ->
            amount = a
            false
        }
        assertEquals(3, amount)
    }

    @Test
    fun `Optional slot with nothing owned is dropped even if obtainable`() {
        level(player, Skill.Attack, 99)
        level(player, Skill.Defence, 99)
        val bound = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = weapons),
            EquipSlot.Shield to LoadoutSlot(gear = shields, optional = true),
        ).bind(player) { _, _ -> true }
        assertTrue(bound.containsKey(EquipSlot.Weapon))
        assertFalse(bound.containsKey(EquipSlot.Shield))
    }

    @Test
    fun `Optional slot with an owned item is kept`() {
        level(player, Skill.Defence, 99)
        player.bank.add("wooden_shield")
        val bound = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = weapons),
            EquipSlot.Shield to LoadoutSlot(gear = shields, optional = true),
        ).bind(player, never)
        assertEquals(setOf("wooden_shield"), bound.ids(EquipSlot.Shield))
    }

    @Test
    fun `Two-handed weapon drops an optional shield`() {
        level(player, Skill.Attack, 99)
        level(player, Skill.Defence, 99)
        player.inventory.add("dragon_2h_sword")
        player.inventory.add("wooden_shield")
        val bound = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = twoHanders),
            EquipSlot.Shield to LoadoutSlot(gear = shields, optional = true),
        ).bind(player, never)
        assertEquals(setOf("dragon_2h_sword"), bound.ids(EquipSlot.Weapon))
        assertFalse(bound.containsKey(EquipSlot.Shield))
    }

    @Test
    fun `Two-handed weapon drops shield regardless of slot order`() {
        level(player, Skill.Attack, 99)
        player.inventory.add("dragon_2h_sword")
        player.inventory.add("wooden_shield")
        val bound = loadout(
            EquipSlot.Shield to LoadoutSlot(gear = shields, optional = true),
            EquipSlot.Weapon to LoadoutSlot(gear = twoHanders),
        ).bind(player, never)
        assertFalse(bound.containsKey(EquipSlot.Shield))
    }

    @Test
    fun `One-handed weapon keeps an optional shield`() {
        level(player, Skill.Attack, 99)
        player.inventory.add("rune_scimitar")
        player.inventory.add("wooden_shield")
        val bound = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = weapons),
            EquipSlot.Shield to LoadoutSlot(gear = shields, optional = true),
        ).bind(player, never)
        assertEquals(setOf("wooden_shield"), bound.ids(EquipSlot.Shield))
    }

    @Test
    fun `Required shield skips two-handed weapons`() {
        level(player, Skill.Attack, 99)
        level(player, Skill.Defence, 99)
        player.inventory.add("dragon_2h_sword")
        player.inventory.add("rune_scimitar")
        val bound = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = mixed),
            EquipSlot.Shield to LoadoutSlot(gear = shields),
        ).bind(player, never)
        assertEquals(setOf("rune_scimitar"), bound.ids(EquipSlot.Weapon))
        assertTrue(bound.containsKey(EquipSlot.Shield))
    }

    @Test
    fun `Explicit ids pass through unchanged`() {
        val bound = loadout(EquipSlot.Cape to LoadoutSlot(ids = setOf("fire_cape", "obsidian_cape"), min = 1)).bind(player, never)
        assertEquals(setOf("fire_cape", "obsidian_cape"), bound.ids(EquipSlot.Cape))
        assertEquals(1, bound.getValue(EquipSlot.Cape).min)
    }

    @Test
    fun `Required slot with nothing equippable keeps all candidates`() {
        val bound = loadout(EquipSlot.Hat to LoadoutSlot(gear = helms)).bind(player, never)
        assertEquals(setOf("rune_full_helm", "iron_full_helm"), bound.ids(EquipSlot.Hat))
    }

    @Test
    fun `Qualifies when every required slot has an equippable item`() {
        val loadout = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons), EquipSlot.Hat to LoadoutSlot(gear = helms))
        assertFalse(loadout.qualifies(player))
        level(player, Skill.Defence, 10)
        assertTrue(loadout.qualifies(player))
    }

    @Test
    fun `Does not qualify when no item in a required slot is equippable`() {
        val loadout = loadout(EquipSlot.Weapon to LoadoutSlot(gear = gear("high", "rune_scimitar", "dragon_2h_sword")))
        assertFalse(loadout.qualifies(player))
        level(player, Skill.Attack, 40)
        assertTrue(loadout.qualifies(player))
    }

    @Test
    fun `Optional slots do not affect qualifying`() {
        val loadout = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons), EquipSlot.Hat to LoadoutSlot(gear = helms, optional = true))
        assertTrue(loadout.qualifies(player))
    }

    @Test
    fun `Slot containing an item without requirements is always qualified`() {
        val loadout = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons))
        assertTrue(loadout.qualifies(player))
        assertTrue(loadout.skills.isEmpty())
    }

    @Test
    fun `Skills lists those gating required slots`() {
        val loadout = loadout(EquipSlot.Weapon to LoadoutSlot(gear = gear("w", "rune_scimitar")), EquipSlot.Hat to LoadoutSlot(gear = helms))
        assertEquals(setOf(Skill.Attack, Skill.Defence), loadout.skills)
    }

    @Test
    fun `Grant levels raises the bot to the easiest requirement`() {
        val loadout = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = gear("w", "rune_scimitar", "steel_scimitar")),
            EquipSlot.Hat to LoadoutSlot(gear = helms),
        )
        assertFalse(loadout.qualifies(player))
        loadout.grantLevels(player)
        assertEquals(5, player.levels.getMax(Skill.Attack))
        assertEquals(10, player.levels.getMax(Skill.Defence))
        assertTrue(loadout.qualifies(player))
    }

    @Test
    fun `Grant levels does not lower existing levels`() {
        level(player, Skill.Attack, 50)
        loadout(EquipSlot.Weapon to LoadoutSlot(gear = gear("w", "rune_scimitar"))).grantLevels(player)
        assertEquals(50, player.levels.getMax(Skill.Attack))
    }

    @Test
    fun `Grant puts best equippable items for all slots plus inventory into the bank`() {
        val loadout = loadout(
            EquipSlot.Weapon to LoadoutSlot(gear = weapons),
            EquipSlot.Shield to LoadoutSlot(gear = shields, optional = true),
            inventory = listOf(BotItem(setOf("shark"), min = 3)),
        )
        level(player, Skill.Attack, 5)
        loadout.grant(player)
        assertEquals(1, player.bank.count("steel_scimitar"))
        assertEquals(0, player.bank.count("rune_scimitar"))
        assertEquals(1, player.bank.count("wooden_shield"))
        assertEquals(3, player.bank.count("shark"))
    }

    @Test
    fun `Equipment and extra inventory expose every candidate`() {
        val loadout = loadout(EquipSlot.Weapon to LoadoutSlot(gear = weapons), inventory = listOf(BotItem(setOf("shark"), min = 3)))
        assertEquals(setOf("rune_scimitar", "steel_scimitar", "bronze_sword"), loadout.equipment.items.getValue(EquipSlot.Weapon).ids)
        assertEquals(listOf(BotItem(setOf("shark"), min = 3)), loadout.extraInventory!!.items)
    }
}
