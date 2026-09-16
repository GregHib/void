package content.area.asgarnia.goblin_village

import WorldTest
import content.entity.combat.target
import content.entity.item.height
import floorItemOption
import interfaceOnFloorItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class ChaosTempleWineTest : WorldTest() {

    private val wineTile = ChaosTempleWine.WINE_TILE

    @Test
    fun `Taking the wine by hand burns, drains combat stats and angers the monks`() {
        val player = createPlayer(wineTile.addX(1))
        player.levels.set(Skill.Attack, 50)
        player.levels.set(Skill.Strength, 26)
        player.levels.set(Skill.Defence, 12)
        val monk = createNPC("monk_of_zamorak_goblin_village", wineTile.add(2, -1))
        val wine = createFloorItem("wine_of_zamorak", wineTile)
        tick()

        player.floorItemOption(wine, "Take")
        tick(5)

        assertTrue(player.inventory.contains("wine_of_zamorak"))
        assertEquals(47, player.levels.get(Skill.Attack))
        assertEquals(24, player.levels.get(Skill.Strength))
        assertEquals(12, player.levels.get(Skill.Defence))
        assertTrue(player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution))
        assertEquals(player, monk.target)
    }

    @Test
    fun `Telegrabbing the wine is safe`() {
        val player = createPlayer(wineTile.addX(3))
        player.levels.set(Skill.Magic, 99)
        player.levels.set(Skill.Attack, 50)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune")
        val monk = createNPC("monk_of_zamorak_goblin_village", Tile(2940, 3517))
        val wine = createFloorItem("wine_of_zamorak", wineTile)
        tick()

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", wine)
        tick(6)

        assertEquals(384, wine.height())
        assertTrue(player.inventory.contains("wine_of_zamorak"))
        assertEquals(43.0, player.experience.get(Skill.Magic))
        assertEquals(50, player.levels.get(Skill.Attack))
        assertEquals(player.levels.getMax(Skill.Constitution), player.levels.get(Skill.Constitution))
        assertFalse(monk.target == player)
    }

    @Test
    fun `Telegrab loses to a player who takes the wine first`() {
        val caster = createPlayer(wineTile.addX(3))
        caster.levels.set(Skill.Magic, 99)
        caster.inventory.add("law_rune")
        caster.inventory.add("air_rune")
        val thief = createPlayer(wineTile.addX(1))
        val wine = createFloorItem("wine_of_zamorak", wineTile)
        tick()

        caster.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", wine)
        tick()
        thief.floorItemOption(wine, "Take")
        tick(8)

        assertTrue(thief.inventory.contains("wine_of_zamorak"))
        assertFalse(caster.inventory.contains("wine_of_zamorak"))
        assertEquals(0, caster.inventory.count("law_rune"))
        assertTrue(FloorItems.at(wineTile).none { it.id == "wine_of_zamorak" })
    }

    @Test
    fun `Arriving after the wine is taken isn't punished`() {
        val late = createPlayer(wineTile.addX(6))
        late.levels.set(Skill.Attack, 50)
        val thief = createPlayer(wineTile.addX(1))
        val wine = createFloorItem("wine_of_zamorak", wineTile)
        tick()

        late.floorItemOption(wine, "Take")
        thief.floorItemOption(wine, "Take")
        tick(10)

        assertTrue(thief.inventory.contains("wine_of_zamorak"))
        assertFalse(late.inventory.contains("wine_of_zamorak"))
        assertEquals(50, late.levels.get(Skill.Attack))
        assertEquals(late.levels.getMax(Skill.Constitution), late.levels.get(Skill.Constitution))
    }

    @Test
    fun `Wine away from its spawn tile is harmless`() {
        val tile = wineTile.addY(1)
        val player = createPlayer(tile.addX(1))
        player.levels.set(Skill.Attack, 50)
        val wine = createFloorItem("wine_of_zamorak", tile)
        tick()

        player.floorItemOption(wine, "Take")
        tick(5)

        assertTrue(player.inventory.contains("wine_of_zamorak"))
        assertEquals(50, player.levels.get(Skill.Attack))
        assertEquals(player.levels.getMax(Skill.Constitution), player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Wine respawns after being taken`() {
        loadItemSpawns(get<ItemSpawns>(), configFiles().list(Settings["spawns.items"]))
        val player = createPlayer(wineTile.addX(1))
        tick()

        val wine = FloorItems.at(wineTile).first { it.id == "wine_of_zamorak" }
        player.floorItemOption(wine, "Take")
        tick(5)

        assertTrue(player.inventory.contains("wine_of_zamorak"))
        assertEquals(1, FloorItems.at(wineTile).count { it.id == "wine_of_zamorak" })
    }
}
