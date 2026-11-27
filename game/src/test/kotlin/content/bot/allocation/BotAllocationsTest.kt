package content.bot.allocation

import content.bot.Bot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.area.Rectangle
import java.io.File

class BotAllocationsTest {

    @TempDir
    lateinit var dir: File

    private val activities = setOf("fight", "mine", "cook")

    private val header = """
        [bot_allocations]
        row_id = "string"
        mode = "string"
        mode_default = "capacity"
        count = "int"
        count_default = 0
        count_setting = "string"
        spawn_seconds = "int"
        spawn_seconds_default = 60
        spawn_seconds_setting = "string"
        spawn_areas = "list<string>"
        refill = "boolean"
        refill_default = false
        saved = "boolean"
        saved_default = false
        starter_items = "list<pair<string, int>>"
        restock_areas = "list<string>"
        roles = "list<row>"

        [bot_roles]
        row_id = "string"
        activity = "string"
        weight = "int"
        weight_default = 1
        min = "int"
        min_default = 0
        max = "int"
        builds = "list<pair<string, int>>"
        combat_style = "string"

        [bot_builds]
        row_id = "string"
        attack = "range"
        strength = "range"
        constitution = "range"
        mining = "range"
    """.trimIndent()

    @BeforeEach
    fun setup() {
        val areas = listOf("lumbridge", "varrock").associateWith { AreaDefinition(it, Rectangle(0, 0, 10, 10)) }
        Areas.set(areas)
    }

    @AfterEach
    fun teardown() {
        BotAllocations.clear()
        Tables.clear()
        Rows.clear()
        Areas.clear()
    }

    private fun load(body: String, exists: (String) -> Boolean = { it in activities }): BotAllocations {
        val file = File(dir, "test.tables.toml")
        file.writeText(header + "\n\n" + body.trimIndent())
        Tables.load(listOf(file.path))
        return BotAllocations.load(exists)
    }

    private fun member(role: String?) = Bot(world.gregs.voidps.engine.entity.character.player.Player()).also { it.role = role }

    @Test
    fun `Parse a capacity row`() {
        load(
            """
            [bot_allocations.world]
            count = 50
            count_setting = "bots.count"
            spawn_seconds = 30
            spawn_seconds_setting = "bots.spawnSeconds"
            spawn_areas = ["lumbridge", "varrock"]
            refill = true
            saved = true
            starter_items = [["coins", 10000], ["bread", 2]]
            """,
        )

        val allocator = BotAllocations.get("world")
        assertTrue(allocator is CapacityAllocator)
        val config = allocator.config
        assertEquals("world", config.id)
        assertEquals(50, config.count)
        assertEquals("bots.count", config.countSetting)
        assertEquals(30, config.spawnSeconds)
        assertEquals("bots.spawnSeconds", config.spawnSecondsSetting)
        assertEquals(listOf("lumbridge", "varrock"), config.spawnAreas)
        assertTrue(config.refill)
        assertTrue(config.saved)
        assertEquals(listOf("coins" to 10000, "bread" to 2), config.starterItems)
    }

    @Test
    fun `Parse applies column defaults`() {
        load(
            """
            [bot_allocations.minimal]
            spawn_areas = ["lumbridge"]
            """,
        )

        val config = BotAllocations.get("minimal").config
        assertTrue(BotAllocations.get("minimal") is CapacityAllocator)
        assertEquals(0, config.count)
        assertEquals(60, config.spawnSeconds)
        assertFalse(config.refill)
        assertFalse(config.saved)
        assertTrue(config.starterItems.isEmpty())
        assertNull(config.countSetting)
        assertNull(config.spawnSecondsSetting)
    }

    @Test
    fun `Parse a proportional row with roles and builds`() {
        load(
            """
            [bot_builds.zerker]
            attack = [60, 75]
            strength = [80, 99]
            constitution = [70, 80]

            [bot_builds.miner]
            mining = [50, 50]

            [bot_roles.fighter]
            activity = "fight"
            weight = 3
            min = 2
            max = 10
            builds = [["bot_builds.zerker", 4], ["bot_builds.miner", 1]]
            combat_style = "aggressive"

            [bot_roles.digger]
            activity = "mine"

            [bot_allocations.arena]
            mode = "weighted"
            count = 20
            spawn_areas = ["varrock"]
            roles = ["bot_roles.fighter", "bot_roles.digger"]
            """,
        )

        val allocator = BotAllocations.get("arena") as WeightedAllocator
        assertEquals(20, allocator.count)
        assertEquals(2, allocator.roles.size)

        val fighter = allocator.role("fighter")!!
        assertEquals("fight", fighter.activity)
        assertEquals(3, fighter.weight)
        assertEquals(2, fighter.min)
        assertEquals(10, fighter.max)
        assertEquals("aggressive", fighter.combatStyle)
        assertEquals(listOf("zerker", "miner"), fighter.builds.map { it.first.id })
        assertEquals(listOf(4, 1), fighter.builds.map { it.second })
        val zerker = fighter.builds[0].first
        assertEquals(mapOf(Skill.Attack to 60..75, Skill.Strength to 80..99, Skill.Constitution to 70..80), zerker.levels)
        assertEquals(mapOf(Skill.Mining to 50..50), fighter.builds[1].first.levels)

        val digger = allocator.role("digger")!!
        assertEquals("mine", digger.activity)
        assertEquals(1, digger.weight)
        assertEquals(0, digger.min)
        assertNull(digger.max)
        assertNull(digger.combatStyle)
        assertTrue(digger.builds.isEmpty())
    }

    @Test
    fun `Role order follows the roles list`() {
        load(
            """
            [bot_roles.a]
            activity = "fight"

            [bot_roles.b]
            activity = "mine"

            [bot_allocations.arena]
            mode = "weighted"
            spawn_areas = ["varrock"]
            roles = ["bot_roles.b", "bot_roles.a"]
            """,
        )

        assertEquals(listOf("b", "a"), (BotAllocations.get("arena") as WeightedAllocator).roles.map { it.id })
    }

    @Test
    fun `Multiple allocations are loaded`() {
        load(
            """
            [bot_allocations.one]
            spawn_areas = ["lumbridge"]

            [bot_allocations.two]
            spawn_areas = ["varrock"]
            """,
        )

        assertEquals(setOf("one", "two"), BotAllocations.ids)
        assertEquals(2, BotAllocations.all.size)
        assertNotNull(BotAllocations.getOrNull("one"))
        assertNull(BotAllocations.getOrNull("three"))
    }

    @Test
    fun `Get an unknown allocation throws`() {
        assertThrows(IllegalStateException::class.java) { BotAllocations.get("missing") }
    }

    @Test
    fun `Missing table loads nothing`() {
        Tables.set(emptyMap())
        BotAllocations.load { true }

        assertTrue(BotAllocations.all.isEmpty())
    }

    @Test
    fun `Loading again replaces previous allocations`() {
        BotAllocations.set(listOf(CapacityAllocator(AllocationConfig("old"))))
        Tables.set(emptyMap())
        BotAllocations.load { true }

        assertTrue(BotAllocations.ids.isEmpty())
    }

    @Test
    fun `Unknown mode is an error`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            load(
                """
                [bot_allocations.bad]
                mode = "random"
                spawn_areas = ["lumbridge"]
                """,
            )
        }
        assertTrue(exception.message!!.contains("random"))
    }

    @Test
    fun `No spawn areas is an error`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            load(
                """
                [bot_allocations.bad]
                count = 5
                """,
            )
        }
        assertTrue(exception.message!!.contains("no spawn areas"))
    }

    @Test
    fun `Unknown spawn area is an error`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            load(
                """
                [bot_allocations.bad]
                spawn_areas = ["lumbridge", "atlantis"]
                """,
            )
        }
        assertTrue(exception.message!!.contains("atlantis"))
    }

    @Test
    fun `Unknown role activity is an error`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            load(
                """
                [bot_roles.lost]
                activity = "unknown_activity"

                [bot_allocations.bad]
                mode = "weighted"
                spawn_areas = ["lumbridge"]
                roles = ["bot_roles.lost"]
                """,
            )
        }
        assertTrue(exception.message!!.contains("unknown_activity"))
    }

    @Test
    fun `Proportional row without roles is an error`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            load(
                """
                [bot_allocations.bad]
                mode = "weighted"
                spawn_areas = ["lumbridge"]
                """,
            )
        }
        assertTrue(exception.message!!.contains("no roles"))
    }

    @Test
    fun `Unknown build row is an error`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            load(
                """
                [bot_roles.r]
                activity = "fight"
                builds = [["bot_builds.missing", 1]]

                [bot_allocations.bad]
                mode = "weighted"
                spawn_areas = ["lumbridge"]
                roles = ["bot_roles.r"]
                """,
            )
        }
        assertTrue(exception.message!!.contains("bot_builds.missing"))
    }

    @Test
    fun `Parse restock areas`() {
        load(
            """
            [bot_allocations.world]
            spawn_areas = ["lumbridge"]
            restock_areas = ["varrock"]

            [bot_allocations.other]
            spawn_areas = ["lumbridge"]
            """,
        )

        assertEquals(setOf("varrock"), BotAllocations.get("world").config.restockAreas)
        assertTrue(BotAllocations.get("other").config.restockAreas.isEmpty())
    }

    @Test
    fun `Unknown restock area is an error`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            load(
                """
                [bot_allocations.bad]
                spawn_areas = ["lumbridge"]
                restock_areas = ["nowhere"]
                """,
            )
        }
        assertTrue(exception.message!!.contains("nowhere"))
    }
}
