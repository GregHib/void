package content.bot

import FakeRandom
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotLoadout
import content.bot.behaviour.condition.BotLoadoutQualified
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.loadout.Loadout
import content.bot.behaviour.loadout.LoadoutFixtures
import content.bot.behaviour.loadout.LoadoutFixtures.gear
import content.bot.behaviour.loadout.LoadoutFixtures.level
import content.bot.behaviour.loadout.LoadoutSlot
import content.bot.behaviour.setup.DynamicResolvers
import content.bot.behaviour.withLoadoutRequirement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

class BotManagerLoadoutTest {

    private lateinit var bot: Bot
    private lateinit var manager: BotManager

    private val loadout = Loadout(
        "melee",
        mapOf(
            EquipSlot.Weapon to LoadoutSlot(gear = gear("weapons", "rune_scimitar", "steel_scimitar", "bronze_sword")),
            EquipSlot.Shield to LoadoutSlot(gear = gear("shields", "rune_kiteshield", "wooden_shield"), optional = true),
        ),
        inventory = listOf(BotItem(setOf("shark"), min = 5)),
    )

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        DynamicResolvers.shopItems.clear()
        DynamicResolvers.sampleItems.clear()
        LoadoutFixtures.define()
        bot = Bot(LoadoutFixtures.player())
        manager = BotManager(world = FakeWorld())
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
        DynamicResolvers.shopItems.clear()
        DynamicResolvers.sampleItems.clear()
    }

    private fun activity(vararg setup: content.bot.behaviour.condition.Condition) = BotActivity(id = "fight", setup = setup.toList())

    @Test
    fun `Bind replaces the loadout row with concrete equipment and sets boundLoadout`() {
        level(bot.player, Skill.Attack, 99)
        bot.player.inventory.add("steel_scimitar")
        val bound = manager.bind(bot, activity(BotLoadout(listOf(loadout to 1))))

        assertEquals("melee", bound.boundLoadout)
        assertTrue(bound.setup.none { it is BotLoadout })
        val equipment = bound.setup.filterIsInstance<BotEquipmentSetup>().single()
        assertEquals(setOf("steel_scimitar"), equipment.items.getValue(EquipSlot.Weapon).ids)
        assertFalse(equipment.items.containsKey(EquipSlot.Shield))
        assertNull(bound.loadout)
    }

    @Test
    fun `Loadout inventory merges into an existing inventory row`() {
        val existing = BotInventorySetup(listOf(BotItem(setOf("bronze_sword"), min = 1)))
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1)), existing))

        val inventory = bound.setup.filterIsInstance<BotInventorySetup>().single()
        assertEquals(existing.items + loadout.inventory, inventory.items)
    }

    @Test
    fun `Loadout inventory creates an inventory row when there is none`() {
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1))))
        assertEquals(loadout.inventory, bound.setup.filterIsInstance<BotInventorySetup>().single().items)
    }

    @Test
    fun `Explicit activity equipment slot overrides the loadout`() {
        level(bot.player, Skill.Attack, 99)
        bot.player.inventory.add("rune_scimitar")
        val explicit = BotEquipmentSetup(mapOf(EquipSlot.Weapon to BotItem(setOf("bronze_sword"), min = 1)))
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1)), explicit))

        val equipment = bound.setup.filterIsInstance<BotEquipmentSetup>().single()
        assertEquals(setOf("bronze_sword"), equipment.items.getValue(EquipSlot.Weapon).ids)
    }

    @Test
    fun `Loadout slots merge with other explicit equipment slots`() {
        level(bot.player, Skill.Attack, 99)
        bot.player.inventory.add("rune_scimitar")
        val explicit = BotEquipmentSetup(mapOf(EquipSlot.Cape to BotItem(setOf("fire_cape"), min = 1)))
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1)), explicit))

        val equipment = bound.setup.filterIsInstance<BotEquipmentSetup>().single()
        assertEquals(setOf(EquipSlot.Weapon, EquipSlot.Cape), equipment.items.keys)
    }

    @Test
    fun `Binding an already bound activity returns it unchanged`() {
        val bound = manager.bind(bot, activity(BotLoadout(listOf(loadout to 1))))
        assertSame(bound, manager.bind(bot, bound))
        assertSame(bound, manager.bindLoadout(bot, bound))
    }

    @Test
    fun `Activity without a loadout is returned unchanged`() {
        val plain = activity(BotSkillLevel(Skill.Attack, min = 1))
        assertSame(plain, manager.bindLoadout(bot, plain))
    }

    @Test
    fun `Unqualified bot still binds the best equippable items`() {
        val gated = Loadout("gated", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("w", "rune_scimitar", "dragon_2h_sword"))))
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(gated to 1))))
        assertEquals("gated", bound.boundLoadout)
        assertEquals(setOf("rune_scimitar", "dragon_2h_sword"), bound.setup.filterIsInstance<BotEquipmentSetup>().single().items.getValue(EquipSlot.Weapon).ids)
    }

    @Test
    fun `Obtainable items are bound when nothing is owned`() {
        level(bot.player, Skill.Attack, 99)
        DynamicResolvers.sampleItems["steel_scimitar"] = mutableListOf("somewhere" to "shopkeeper")
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1))))
        assertEquals(setOf("steel_scimitar"), bound.setup.filterIsInstance<BotEquipmentSetup>().single().items.getValue(EquipSlot.Weapon).ids)
    }

    @Test
    fun `With loadout requirement adds the qualified condition to requires`() {
        val activity = activity(BotLoadout(listOf(loadout to 1))).withLoadoutRequirement()
        val requirement = activity.requires.filterIsInstance<BotLoadoutQualified>().single()
        assertEquals(listOf(loadout), requirement.loadouts)
    }

    @Test
    fun `With loadout requirement leaves activities without loadouts alone`() {
        val plain = activity(BotSkillLevel(Skill.Attack, min = 1))
        assertSame(plain, plain.withLoadoutRequirement())
    }

    @Test
    fun `With loadout requirement rejects multiple loadout setups`() {
        val activity = activity(BotLoadout(listOf(loadout to 1)), BotLoadout(listOf(loadout to 1)))
        assertThrows(IllegalArgumentException::class.java) { activity.withLoadoutRequirement() }
    }

    private val alternative = Loadout("alternative", mapOf(EquipSlot.Weapon to LoadoutSlot(ids = setOf("bronze_sword"))))
    private val gated = Loadout("gated", mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("gated_weapons", "dragon_2h_sword"))))

    @Test
    fun `Bind uses the bots loadout when it is an option and qualifies`() {
        bot.loadout = "alternative"
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1, alternative to 1))))

        assertEquals("alternative", bound.boundLoadout)
        assertEquals(setOf("bronze_sword"), bound.setup.filterIsInstance<BotEquipmentSetup>().single().items.getValue(EquipSlot.Weapon).ids)
    }

    @Test
    fun `Bind falls back to the weighted pick when the bots loadout is not an option`() {
        bot.loadout = "missing"
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(loadout to 1, alternative to 1))))

        assertEquals("melee", bound.boundLoadout)
    }

    @Test
    fun `Bind falls back to the weighted pick when the bots loadout does not qualify`() {
        bot.loadout = "gated"
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(gated to 1, alternative to 1))))

        assertEquals("alternative", bound.boundLoadout)
    }

    @Test
    fun `Bind without a preferred loadout uses the weighted pick`() {
        val bound = manager.bindLoadout(bot, activity(BotLoadout(listOf(alternative to 1, loadout to 1))))
        assertEquals("alternative", bound.boundLoadout)
    }
}
