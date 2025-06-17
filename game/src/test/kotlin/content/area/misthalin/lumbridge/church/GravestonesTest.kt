package content.area.misthalin.lumbridge.church

import WorldTest
import content.entity.combat.hit.damage
import content.entity.effect.transform
import interfaceOption
import npcOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.type.Tile
import kotlin.test.*

class GravestonesTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setCurrentTime { 0 }
    }

    @Test
    fun `Spawn a gravestone on death`() {
        val tile = Tile(3235, 3220)
        val player = createPlayer(tile)
        player.inventory.add("coins", 10)
        tick()

        player.damage(101)

        tick(9)

        val grave = npcs[tile].firstOrNull { it.id.startsWith("gravestone") }
        assertNotNull(grave)
        assertEquals(player.name, grave["player_name", ""])
        assertTrue(grave.softTimers.contains("grave_degrade"))
        val remaining = grave.remaining("grave_timer", epochSeconds())
        assertNotEquals(-1, remaining)
        assertEquals(300, remaining - epochSeconds())
        assertFalse(player.inventory.contains("coins", 10))
        assertEquals(Tile(3221, 3219), player.tile)
    }

    @Test
    fun `A gravestone breaks after 3 minutes`() {
        val tile = Tile(3235, 3220)
        val player = createPlayer(tile)
        Gravestone.spawn(npcs, player, tile)
        tick()
        val grave = npcs[tile].first { it.id.startsWith("gravestone") }
        grave["grave_timer"] = 119
        grave.emit(TimerTick("grave_degrade"))
        assertEquals("gravestone_memorial_plaque_broken", grave.transform)
        grave["grave_timer"] = 20
        grave.emit(TimerTick("grave_degrade"))
        assertEquals("gravestone_memorial_plaque_collapse", grave.transform)
        grave["grave_timer"] = 0
        grave.emit(TimerStop("grave_degrade", false))
        tick()
        assertNull(npcs[tile].firstOrNull { it.id.startsWith("gravestone") })
    }

    @Test
    fun `Demolish a grave early`() {
        val tile = Tile(3235, 3220)
        val player = createPlayer(tile)
        Gravestone.spawn(npcs, player, tile)
        tick()
        val grave = npcs[tile].first { it.id.startsWith("gravestone") }
        player.npcOption(grave, "Demolish")
        tick(2)
        assertNull(npcs[tile].firstOrNull { it.id.startsWith("gravestone") })
    }

    @Test
    fun `Repairing a grave returns it to 5 minutes remaining`() {
        val tile = Tile(3235, 3220)
        val player = createPlayer(tile)
        Gravestone.spawn(npcs, player, tile)
        val floorItem = floorItems.add(tile, "coins", 10, revealTicks = 100, disappearTicks = 160, owner = player.name)
        tick()
        val grave = npcs[tile].first { it.id.startsWith("gravestone") }
        grave["grave_timer"] = 119
        grave.emit(TimerTick("grave_degrade"))

        val friend = createPlayer(tile.addY(1), name = "friend")
        friend.levels.set(Skill.Prayer, 5)
        friend.npcOption(grave, "Repair")
        tick(1)
        assertEquals(499, floorItem.revealTicks)
        assertEquals(560, floorItem.disappearTicks)
        tick(3)
        assertEquals(300, grave.remaining("grave_timer", epochSeconds()))
    }

    @Test
    fun `Blessing a grave gives it 60 minutes remaining`() {
        val tile = Tile(3235, 3220)
        val player = createPlayer(tile)
        Gravestone.spawn(npcs, player, tile)
        val floorItem = floorItems.add(tile, "coins", 10, revealTicks = 100, disappearTicks = 160, owner = player.name)
        tick()
        val grave = npcs[tile].first { it.id.startsWith("gravestone") }
        grave["grave_timer"] = 119
        grave.emit(TimerTick("grave_degrade"))

        val friend = createPlayer(tile.addY(1), name = "friend")
        friend.levels.set(Skill.Prayer, 75)
        friend.npcOption(grave, "Bless")
        tick(1)
        assertEquals(5999, floorItem.revealTicks)
        assertEquals(6060, floorItem.disappearTicks)
        tick(3)
        assertEquals(3600, grave.remaining("grave_timer", epochSeconds()))
    }

    @Test
    fun `Can't drop items onto a grave`() {
        val tile = Tile(3235, 3220)
        val player = createPlayer(tile)
        Gravestone.spawn(npcs, player, tile)
        tick()
        player.inventory.add("bronze_sword")

        player.interfaceOption("inventory", "inventory", "Drop", 4, Item("bronze_sword"), 0)
        tick()

        assertFalse(player.inventory.isEmpty())
        assertFalse(floorItems[player.tile].any { it.id == "bronze_sword" })
    }
}
