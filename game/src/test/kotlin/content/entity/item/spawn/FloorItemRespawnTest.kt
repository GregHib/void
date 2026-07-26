package content.entity.item.spawn

import WorldTest
import floorItemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class FloorItemRespawnTest : WorldTest() {

    @Test
    fun `Duplicate item timing out on a spawn tile doesn't create extra spawns`() {
        loadItemSpawns(get<ItemSpawns>(), configFiles().list(Settings["spawns.items"]))
        val tile = Tile(3229, 3300)
        tick()
        assertEquals(1, FloorItems.at(tile).count { it.id == "egg" })

        FloorItems.add(tile, "egg", disappearTicks = 2)
        tick(10)

        assertEquals(1, FloorItems.at(tile).count { it.id == "egg" })
    }

    @Test
    fun `Taking a duplicate item on a spawn tile doesn't create extra spawns`() {
        loadItemSpawns(get<ItemSpawns>(), configFiles().list(Settings["spawns.items"]))
        val tile = Tile(3229, 3300)
        val player = createPlayer(tile)
        val duplicate = FloorItems.add(tile, "egg")
        tick()

        player.floorItemOption(duplicate, "Take")
        tick(5)

        assertTrue(player.inventory.contains("egg"))
        assertEquals(1, FloorItems.at(tile).count { it.id == "egg" })
    }

    @Test
    fun `Spawn item still respawns after being taken`() {
        loadItemSpawns(get<ItemSpawns>(), configFiles().list(Settings["spawns.items"]))
        val tile = Tile(3229, 3300)
        val player = createPlayer(tile)
        tick()

        val egg = FloorItems.at(tile).first { it.id == "egg" }
        player.floorItemOption(egg, "Take")
        tick(1)

        assertTrue(player.inventory.contains("egg"))
        tick(10)
        assertEquals(1, FloorItems.at(tile).count { it.id == "egg" })
    }
}
