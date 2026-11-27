package content.bot.behaviour.loadout

import FakeRandom
import content.bot.Bot
import content.bot.BotManager
import content.bot.FakeWorld
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotLoadout
import content.bot.behaviour.loadout.LoadoutFixtures.gear
import content.bot.behaviour.loadout.LoadoutFixtures.level
import content.bot.behaviour.setup.DynamicResolvers
import content.entity.player.bank.bank
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom

class LoadoutInventoryGearTest {

    private lateinit var player: Player

    private val pickaxes = gear("pickaxes", "rune_pickaxe", "iron_pickaxe", "bronze_pickaxe")
    private val mining = Loadout("mining", emptyMap(), inventory = listOf(LoadoutSlot(gear = pickaxes, min = 1, usable = true)))
    private val never = { _: String, _: Int -> false }

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        LoadoutFixtures.define()
        player = LoadoutFixtures.player()
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
        DynamicResolvers.shopItems.clear()
        DynamicResolvers.sampleItems.clear()
    }

    private fun ids(items: List<BotItem>) = items.map { it.ids }

    @Test
    fun `Tools are picked by the level to use them, not to wield them`() {
        level(player, Skill.Mining, 50)
        assertEquals(listOf(setOf("rune_pickaxe")), ids(mining.bindInventory(player, never)))
    }

    @Test
    fun `Tools above the bots use level are skipped`() {
        level(player, Skill.Mining, 10)
        assertEquals(listOf(setOf("iron_pickaxe")), ids(mining.bindInventory(player, never)))
    }

    @Test
    fun `Owned tool wins over a better unowned one`() {
        level(player, Skill.Mining, 50)
        player.bank.add("bronze_pickaxe")
        assertEquals(listOf(setOf("bronze_pickaxe")), ids(mining.bindInventory(player, never)))
    }

    @Test
    fun `Obtainable tool is picked when none are owned`() {
        level(player, Skill.Mining, 50)
        val bound = mining.bindInventory(player) { id, _ -> id == "iron_pickaxe" }
        assertEquals(listOf(setOf("iron_pickaxe")), ids(bound))
    }

    @Test
    fun `Bound tools keep the usable flag and come after explicit items`() {
        val loadout = mining.copy(inventory = listOf(LoadoutSlot(setOf("shark"), min = 2)) + mining.inventory)
        val bound = loadout.bindInventory(player, never)
        assertEquals(listOf(setOf("shark"), setOf("iron_pickaxe")), ids(bound))
        assertTrue(bound.last().usable)
    }

    @Test
    fun `Use requirements decide qualifying and granted levels`() {
        val rune = Loadout("rune", emptyMap(), inventory = listOf(LoadoutSlot(gear = gear("rune", "rune_pickaxe"), usable = true)))
        assertFalse(rune.qualifies(player))
        assertEquals(setOf(Skill.Mining), rune.skills)
        rune.grantLevels(player)
        assertEquals(41, player.levels.getMax(Skill.Mining))
        assertEquals(1, player.levels.getMax(Skill.Attack))
        assertTrue(rune.qualifies(player))
    }

    @Test
    fun `Equip adds the best usable tool to the inventory`() {
        level(player, Skill.Mining, 50)
        assertTrue(mining.equip(player))
        assertEquals(1, player.inventory.count("rune_pickaxe"))
    }

    @Test
    fun `Grant puts the best usable tool in the bank`() {
        level(player, Skill.Mining, 10)
        mining.grant(player)
        assertEquals(1, player.bank.count("iron_pickaxe"))
    }

    @Test
    fun `Loadout check passes while carrying any of the tools`() {
        val requirement = BotLoadout(listOf(mining to 1))
        assertFalse(requirement.check(player))
        player.inventory.add("bronze_pickaxe")
        assertTrue(requirement.check(player))
        assertTrue("item:rune_pickaxe" in requirement.keys())
    }

    @Test
    fun `Parse keeps gear and item inventory entries in order`() {
        val loadout = Loadout.parse(
            "mining",
            mapOf("inventory" to listOf(mapOf("gear" to "pickaxes", "usable" to true), mapOf("id" to "shark", "min" to 2))),
        ) { if (it == "pickaxes") pickaxes else null }
        val (tool, food) = loadout.inventory
        assertEquals(pickaxes, tool.gear)
        assertTrue(tool.usable)
        assertEquals(setOf("shark"), food.ids)
        assertEquals(2, food.min)
    }

    @Test
    fun `Binding an activity merges the tool into its inventory requirement`() {
        level(player, Skill.Mining, 50)
        val manager = BotManager(world = FakeWorld())
        val empty = BotInventorySetup(listOf(BotItem(setOf("empty"), min = 27)))
        val activity = BotActivity(id = "mine", setup = listOf(BotLoadout(listOf(mining to 1)), empty))
        val bound = manager.bindLoadout(Bot(player), activity)
        val inventory = bound.setup.filterIsInstance<BotInventorySetup>().single()
        assertEquals(listOf(setOf("empty"), setOf("rune_pickaxe")), ids(inventory.items))
    }
}
