package content.bot.behaviour.activity

import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.action.BotInteractNpc
import content.bot.behaviour.action.BotInterfaceOption
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotSkillLevel
import content.bot.behaviour.loadBehaviours
import content.bot.behaviour.navigation.NavigationGraph
import content.bot.behaviour.setup.Resolver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.type.Tile
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FishingActivitiesTest {
    @AfterEach
    fun cleanup() {
        Settings.clear()
        Areas.clear()
    }

    @Test
    fun `Fishing activities require correct tackle and levels and bank each catch`() {
        Settings.load(mapOf("bots.templates" to "templates.toml", "bots.definitions" to "bots.toml", "bots.setups" to "setups.toml"))
        val files = ConfigFiles(
            mapOf(
                "templates.toml" to File("../data/bot").listFiles()!!.filter { it.name.endsWith("templates.toml") }.map { it.path },
                "bots.toml" to listOf(
                    "../data/area/kandarin/catherby/catherby.bots.toml",
                    "../data/area/kandarin/fishing_guild/fishing_guild.bots.toml",
                    "../data/area/misthalin/edgeville/edgeville.bots.toml",
                ),
            ),
        )
        val activities = mutableMapOf<String, BotActivity>()
        loadBehaviours(files, activities, mutableMapOf<String, MutableList<Resolver>>())
        val fishing = activities.values.filter { "skill:fishing" in it.produces }
        assertEquals(12, fishing.size)
        for (activity in fishing) {
            assertEquals(3000, activity.timeout)
            val guild = activity.id.startsWith("fishing_guild_")
            val fly = activity.id == "edgeville_fly_fishing"
            val shark = activity.id.contains("shark")
            val cage = activity.id.contains("lobster")
            val net = activity.id.contains("big_net")
            val level = if (shark) {
                76
            } else if (guild) {
                68
            } else if (fly) {
                20
            } else if (cage) {
                40
            } else {
                35
            }
            assertEquals(level, activity.requires.filterIsInstance<BotSkillLevel>().single().min)
            val tackle = activity.setup.filterIsInstance<BotInventorySetup>().single().items
            assertTrue(
                tackle.any {
                    (
                        if (fly) {
                            "fly_fishing_rod"
                        } else if (net) {
                            "big_fishing_net"
                        } else if (cage) {
                            "lobster_pot"
                        } else {
                            "harpoon"
                        }
                        ) in it.ids
                },
            )
            if (fly) {
                assertEquals(52, tackle.single { "feather" in it.ids }.min)
                assertTrue(activity.produces.containsAll(setOf("item:raw_trout", "item:raw_salmon")))
            }
            val fish = activity.actions.first() as BotInteractNpc
            assertEquals(
                if (fly) {
                    "Lure"
                } else if (net) {
                    "Net"
                } else if (cage) {
                    "Cage"
                } else {
                    "Harpoon"
                },
                fish.option,
            )
            val bank = if (guild) {
                "fishing_guild_bank"
            } else if (fly) {
                "edgeville_bank"
            } else {
                "catherby_bank"
            }
            assertEquals(BotGoTo(bank), activity.actions[1])
            assertEquals("Bank", (activity.actions[2] as BotInteractNpc).option)
            val deposits = activity.actions.filterIsInstance<BotInterfaceOption>()
            assertEquals(if (net) 8 else 2, deposits.size)
            assertTrue(deposits.all { it.option == "Deposit-All" && it.id.startsWith("bank_side:inventory:") })
            assertEquals(activity.produces.filter { it.startsWith("item:") }.map { it.removePrefix("item:") }.toSet(), deposits.map { it.id.substringAfterLast(':') }.toSet())
        }
    }

    @Test
    fun `Both guild piers and Catherby and fly fishing spots have bank routes`() {
        Areas.load(
            listOf(
                "../data/area/kandarin/catherby/catherby.areas.toml",
                "../data/area/kandarin/fishing_guild/fishing_guild.areas.toml",
                "../data/area/misthalin/edgeville/edgeville.areas.toml",
                "../data/area/misthalin/barbarian_village/barbarian_village.areas.toml",
            ),
        )
        val graph = NavigationGraph.loadGraph(
            listOf(
                "../data/bot/catherby.nav-edges.toml",
                "../data/bot/fishing_guild.nav-edges.toml",
                "../data/bot/varrock.nav-edges.toml",
            ),
            emptyList(),
        )
        val player = Player()
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.levels.set(Skill.Fishing, 68)
        for ((tile, bank, area) in listOf(
            Triple(Tile(2854, 3425), "catherby_bank", "catherby_cage_fishing_area"),
            Triple(Tile(3108, 3433), "edgeville_bank", "barbarian_village_fishing_spot"),
            Triple(Tile(2600, 3420), "fishing_guild_bank", "fishing_guild_north_fishing_area"),
            Triple(Tile(2607, 3412), "fishing_guild_bank", "fishing_guild_south_fishing_area"),
        )) {
            player.tile = tile
            val path = mutableListOf<Int>()
            assertTrue(graph.find(player, path, bank), "No bank route for $area")
            player.tile = graph.endTile(path.last())
            path.clear()
            assertTrue(graph.find(player, path, area), "No return route for $area")
        }
        player.tile = Tile(2624, 3383)
        assertTrue(graph.find(player, mutableListOf(), "fishing_guild_north_fishing_area"))
        player.levels.set(Skill.Fishing, 67)
        assertTrue(!graph.find(player, mutableListOf(), "fishing_guild_north_fishing_area"))
    }
}
