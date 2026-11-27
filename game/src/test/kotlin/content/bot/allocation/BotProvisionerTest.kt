package content.bot.allocation

import FakeRandom
import content.bot.Bot
import content.bot.behaviour.activity.BotActivity
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.BotLoadout
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.loadout.Loadout
import content.bot.behaviour.loadout.LoadoutFixtures
import content.bot.behaviour.loadout.LoadoutFixtures.gear
import content.bot.behaviour.loadout.LoadoutSlot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

class BotProvisionerTest {

    private lateinit var bot: Bot
    private val activities = mutableMapOf<String, BotActivity>()
    private val provisioner = BotProvisioner { activities[it] }

    private val melee = Loadout(
        "melee",
        mapOf(
            EquipSlot.Weapon to LoadoutSlot(gear = gear("weapons", "rune_scimitar", "steel_scimitar", "bronze_sword")),
            EquipSlot.Shield to LoadoutSlot(gear = gear("shields", "rune_kiteshield", "wooden_shield"), optional = true),
        ),
        inventory = listOf(LoadoutSlot(setOf("shark"), min = 5)),
    )

    private val basic = Loadout(
        "basic",
        mapOf(EquipSlot.Weapon to LoadoutSlot(ids = setOf("bronze_sword"))),
    )

    private val twoHander = Loadout(
        "two_hander",
        mapOf(EquipSlot.Weapon to LoadoutSlot(gear = gear("two_handers", "dragon_2h_sword"))),
    )

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        LoadoutFixtures.define()
        bot = Bot(LoadoutFixtures.player())
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
    }

    private fun activity(id: String = "fight", vararg setup: Condition) = BotActivity(id = id, setup = setup.toList()).also { activities[id] = it }

    private fun build(vararg levels: Pair<Skill, IntRange>) = Build("build", mapOf(*levels))

    private fun role(builds: List<Pair<Build, Int>> = emptyList(), combatStyle: String? = null, activity: String = "fight") = BotRole("fighter", activity, builds = builds, combatStyle = combatStyle)

    private fun inventoryIds() = (0 until 28).map { bot.player.inventory[it].id }.filter { it.isNotEmpty() }

    private fun worn(slot: EquipSlot) = bot.player.equipped(slot).id

    @Test
    fun `Build levels are applied`() {
        activity("fight", BotLoadout(listOf(melee to 1)))
        val role = role(listOf(build(Skill.Attack to 60..60, Skill.Constitution to 70..70, Skill.Strength to 75..75) to 1))

        provisioner.provision(bot, role)

        val player = bot.player
        assertEquals(60, player.levels.getMax(Skill.Attack))
        assertEquals(75, player.levels.getMax(Skill.Strength))
        assertEquals(60, player.levels.get(Skill.Attack))
        assertEquals(Level.experience(Skill.Attack, 60), player.experience.get(Skill.Attack), 0.1)
    }

    @Test
    fun `Constitution is stored times ten`() {
        activity("fight")
        val role = role(listOf(build(Skill.Constitution to 70..70) to 1))

        provisioner.provision(bot, role)

        val player = bot.player
        assertEquals(700, player.levels.getMax(Skill.Constitution))
        assertEquals(700, player.levels.get(Skill.Constitution))
        assertEquals(Level.experience(Skill.Constitution, 700), player.experience.get(Skill.Constitution), 0.1)
        assertEquals(Level.experience(70), player.experience.get(Skill.Constitution), 0.1)
    }

    @Test
    fun `Provisioning restores drained stats`() {
        activity("fight")
        bot.player.levels.set(Skill.Prayer, 0)
        bot.player.levels.set(Skill.Constitution, 10)

        provisioner.provision(bot, role(listOf(build(Skill.Constitution to 50..50) to 1)))

        assertEquals(500, bot.player.levels.get(Skill.Constitution))
        assertEquals(bot.player.levels.getMax(Skill.Prayer), bot.player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Loadout is worn and its inventory added`() {
        activity("fight", BotLoadout(listOf(melee to 1)))

        provisioner.provision(bot, role(listOf(build(Skill.Attack to 60..60) to 1)))

        assertEquals("rune_scimitar", worn(EquipSlot.Weapon))
        assertEquals(5, inventoryIds().count { it == "shark" })
        assertEquals("wooden_shield", worn(EquipSlot.Shield))
    }

    @Test
    fun `Optional slots are filled with the best equippable item`() {
        activity("fight", BotLoadout(listOf(melee to 1)))

        provisioner.provision(bot, role(listOf(build(Skill.Attack to 60..60, Skill.Defence to 40..40) to 1)))

        assertEquals("rune_kiteshield", worn(EquipSlot.Shield))
    }

    @Test
    fun `Loadout levels are granted when the build is too low`() {
        activity("fight", BotLoadout(listOf(twoHander to 1)))

        provisioner.provision(bot, role())

        assertEquals(60, bot.player.levels.getMax(Skill.Attack))
        assertEquals("dragon_2h_sword", worn(EquipSlot.Weapon))
    }

    @Test
    fun `Previous items are cleared and starter items added`() {
        activity("fight", BotLoadout(listOf(melee to 1)))
        bot.player.inventory.add("iron_full_helm")
        bot.player.inventory.add("bronze_sword")
        bot.player.equipment.transaction { set(EquipSlot.Hat.index, Item("rune_full_helm")) }

        provisioner.provision(bot, role(listOf(build(Skill.Attack to 60..60) to 1)), listOf("iron_2h_sword" to 1))

        val items = inventoryIds()
        assertFalse(items.contains("iron_full_helm"))
        assertFalse(items.contains("bronze_sword"))
        assertTrue(items.contains("iron_2h_sword"))
        assertEquals(5, items.count { it == "shark" })
        assertEquals("", worn(EquipSlot.Hat))
        assertEquals("rune_scimitar", worn(EquipSlot.Weapon))
    }

    @Test
    fun `Bot role loadout and build levels are set`() {
        activity("fight", BotLoadout(listOf(melee to 1)))
        val role = role(listOf(build(Skill.Attack to 60..60, Skill.Constitution to 70..70) to 1))

        provisioner.provision(bot, role)

        assertEquals("fighter", bot.role)
        assertEquals("melee", bot.loadout)
        assertEquals(mapOf(Skill.Attack to 60, Skill.Constitution to 70), bot.buildLevels)
    }

    @Test
    fun `Role without builds has no build levels`() {
        activity("fight", BotLoadout(listOf(basic to 1)))
        bot.buildLevels = mapOf(Skill.Attack to 99)

        provisioner.provision(bot, role())

        assertTrue(bot.buildLevels.isEmpty())
    }

    @Test
    fun `Role without a loadout leaves the loadout null`() {
        activity("fight")

        provisioner.provision(bot, role())

        assertEquals("fighter", bot.role)
        assertNull(bot.loadout)
    }

    @Test
    fun `Unknown activity still provisions the build and starter items`() {
        val role = role(listOf(build(Skill.Attack to 30..30) to 1), activity = "missing")

        provisioner.provision(bot, role, listOf("iron_2h_sword" to 1))

        assertEquals(30, bot.player.levels.getMax(Skill.Attack))
        assertEquals(listOf("iron_2h_sword"), inventoryIds())
        assertNull(bot.loadout)
    }

    @Test
    fun `Provision picks a new loadout rather than the previous one`() {
        activity("fight", BotLoadout(listOf(basic to 1, melee to 1)))
        bot.loadout = "melee"

        provisioner.provision(bot, role(listOf(build(Skill.Attack to 60..60) to 1)))

        assertEquals("basic", bot.loadout)
    }

    @Test
    fun `Restore keeps the same build levels even when random would roll differently`() {
        activity("fight", BotLoadout(listOf(melee to 1)))
        setRandom(Random(7))
        val role = role(listOf(build(Skill.Attack to 40..99, Skill.Strength to 1..99) to 1))
        provisioner.provision(bot, role)
        val levels = bot.buildLevels
        assertTrue(levels.isNotEmpty())

        setRandom(Random(9999))
        bot.player.levels.set(Skill.Attack, 1)
        provisioner.restore(bot, role)

        assertEquals(levels, bot.buildLevels)
        assertEquals(levels.getValue(Skill.Attack), bot.player.levels.getMax(Skill.Attack))
        assertEquals(levels.getValue(Skill.Attack), bot.player.levels.get(Skill.Attack))
        assertEquals(levels.getValue(Skill.Strength), bot.player.levels.getMax(Skill.Strength))
    }

    @Test
    fun `Restore keeps the same loadout even when random would pick differently`() {
        activity("fight", BotLoadout(listOf(basic to 1, melee to 1)))
        val role = role(listOf(build(Skill.Attack to 60..60) to 1))
        provisioner.provision(bot, role)
        assertEquals("basic", bot.loadout)
        assertEquals("bronze_sword", worn(EquipSlot.Weapon))

        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        provisioner.restore(bot, role)

        assertEquals("basic", bot.loadout)
        assertEquals("bronze_sword", worn(EquipSlot.Weapon))
        assertEquals(0, inventoryIds().count { it == "shark" })
    }

    @Test
    fun `Restore replaces items again without duplicating them`() {
        activity("fight", BotLoadout(listOf(melee to 1)))
        val role = role(listOf(build(Skill.Attack to 60..60) to 1))
        provisioner.provision(bot, role, listOf("iron_2h_sword" to 1))
        bot.player.inventory.add("iron_full_helm")

        provisioner.restore(bot, role, listOf("iron_2h_sword" to 1))

        val items = inventoryIds()
        assertEquals(5, items.count { it == "shark" })
        assertEquals(1, items.count { it == "iron_2h_sword" })
        assertFalse(items.contains("iron_full_helm"))
    }

    @Test
    fun `Restore falls back to another loadout when the preferred one is unavailable`() {
        activity("fight", BotLoadout(listOf(basic to 1)))
        bot.loadout = "gone"

        provisioner.restore(bot, role())

        assertEquals("basic", bot.loadout)
    }

    @Test
    fun `Combat style is set`() {
        activity("fight")

        provisioner.provision(bot, role(combatStyle = "aggressive"))

        assertEquals("aggressive", bot.player["combat_style", ""])
    }

    @Test
    fun `Combat style is untouched without one`() {
        activity("fight")
        bot.player["combat_style"] = "defensive"

        provisioner.provision(bot, role())

        assertEquals("defensive", bot.player["combat_style", ""])
    }

    @Test
    fun `Explicit equipment setup is applied over the loadout`() {
        val explicit = BotEquipmentSetup(mapOf(EquipSlot.Weapon to BotItem(setOf("iron_2h_sword"), min = 1)))
        activity("fight", BotLoadout(listOf(melee to 1)), explicit)

        provisioner.provision(bot, role(listOf(build(Skill.Attack to 60..60) to 1)))

        assertEquals("iron_2h_sword", worn(EquipSlot.Weapon))
        assertEquals(5, inventoryIds().count { it == "shark" })
    }

    @Test
    fun `Explicit equipment setup works without a loadout`() {
        val explicit = BotEquipmentSetup(mapOf(EquipSlot.Hat to BotItem(setOf("iron_full_helm"), min = 1)))
        activity("fight", explicit)

        provisioner.provision(bot, role())

        assertEquals("iron_full_helm", worn(EquipSlot.Hat))
        assertNull(bot.loadout)
    }

    @Test
    fun `Explicit inventory setup is added`() {
        val inventory = BotInventorySetup(listOf(BotItem(setOf("shark"), min = 3)))
        activity("fight", BotLoadout(listOf(melee to 1)), inventory)

        provisioner.provision(bot, role(listOf(build(Skill.Attack to 60..60) to 1)))

        assertEquals(8, inventoryIds().count { it == "shark" })
    }
}
