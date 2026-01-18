package content.skill.runecrafting

import WorldTest
import content.entity.obj.ObjectTeleports
import itemOnObject
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.koin.test.get
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class RunecraftingTest : WorldTest() {

    private lateinit var teleports: ObjectTeleports

    @BeforeEach
    fun setup() {
        teleports = get()
    }

    @TestFactory
    fun `Craft runes with rune essence`() = altars.filter { !it.pure }.map { (type, _, altarTile) ->
        dynamicTest("Craft $type runes with rune essence") {
            val tile = teleports.get("${type}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 99)
            player.inventory.add("rune_essence")

            val altar = GameObjects.find(altarTile, "${type}_altar")
            player.objectOption(altar, "Craft-rune")
            tick(1)
            tickIf { player.visuals.moved }

            assertFalse(player.inventory.contains("rune_essence"))
            assertTrue(player.inventory.contains("${type}_rune"))
            assertTrue(player.experience.get(Skill.Runecrafting) > 0)
        }
    }

    @TestFactory
    fun `Cant craft high level runes with rune essence`() = altars.filter { it.pure }.map { (type, _, altarTile) ->
        dynamicTest("Can't craft $type runes with rune essence") {
            val tile = teleports.get("${type}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 99)
            player.inventory.add("rune_essence")

            val altar = GameObjects.find(altarTile, "${type}_altar")
            player.objectOption(altar, "Craft-rune")
            tick(1)
            tickIf { player.visuals.moved }

            assertTrue(player.inventory.contains("rune_essence"))
            assertFalse(player.inventory.contains("${type}_rune"))
            assertEquals(0.0, player.experience.get(Skill.Runecrafting))
        }
    }

    @TestFactory
    fun `Craft runes with pure essence`() = altars.map { (type, _, altarTile) ->
        dynamicTest("Craft $type runes with pure essence") {
            val tile = teleports.get("${type}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 99)
            player.inventory.add("pure_essence")

            val altar = GameObjects.find(altarTile, "${type}_altar")
            player.itemOnObject(altar, 0)
            tick(1)
            tickIf { player.visuals.moved }

            assertFalse(player.inventory.contains("pure_essence"))
            assertTrue(player.inventory.contains("${type}_rune"))
            assertTrue(player.experience.get(Skill.Runecrafting) > 0)
        }
    }

    @TestFactory
    fun `Can craft multiple runes with one essence`() = altars.filter { it.type != "law" && it.type != "death" && it.type != "blood" }.map { (type, _, altarTile) ->
        dynamicTest("Craft multiple $type runes with pure essence") {
            val tile = teleports.get("${type}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 99)
            player.inventory.add("pure_essence")

            val altar = GameObjects.find(altarTile, "${type}_altar")
            player.itemOnObject(altar, 0)
            tick(1)
            tickIf { player.visuals.moved }

            assertFalse(player.inventory.contains("pure_essence"))
            assertTrue(player.inventory.count("${type}_rune") > 1)
            assertTrue(player.experience.get(Skill.Runecrafting) > 0)
        }
    }

    @TestFactory
    fun `Cant craft runes without required level`() = altars.map { (type, _, altarTile) ->
        dynamicTest("Can't craft $type runes") {
            val tile = teleports.get("${type}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 0)
            player.inventory.add("pure_essence")

            val altar = GameObjects.find(altarTile, "${type}_altar")
            player.objectOption(altar, "Craft-rune")
            tick(1)
            tickIf { player.visuals.moved }

            assertTrue(player.inventory.contains("pure_essence"))
            assertFalse(player.inventory.contains("${type}_rune"))
            assertEquals(0.0, player.experience.get(Skill.Runecrafting))
        }
    }

    companion object {
        internal data class Altar(val type: String, val ruinsTile: Tile, val altarTile: Tile, val pure: Boolean = false)

        internal val altars = listOf(
            Altar("air", Tile(3126, 3404), Tile(2843, 4833)),
            Altar("water", Tile(3184, 3164), Tile(3483, 4835)),
            Altar("earth", Tile(3305, 3473), Tile(2657, 4840)),
            Altar("fire", Tile(3312, 3254), Tile(2584, 4837)),
            Altar("mind", Tile(2981, 3513), Tile(2785, 4840)),
            Altar("body", Tile(3052, 3444), Tile(2522, 4839)),
            Altar("cosmic", Tile(2407, 4376), Tile(2141, 4832), pure = true),
            Altar("law", Tile(2857, 3380), Tile(2463, 4831), pure = true),
            Altar("nature", Tile(2868, 3018), Tile(2399, 4840), pure = true),
            Altar("chaos", Tile(3059, 3590), Tile(2270, 4841), pure = true),
            Altar("death", Tile(1860, 4638), Tile(2204, 4835), pure = true),
            Altar("blood", Tile(3560, 9780), Tile(2461, 4894, 1), pure = true),
        )
    }
}
