package content.area.karamja.shilo_village

import WorldTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class ShiloVillageCartTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Travel from shilo to brimhaven`() {
        val player = createPlayer(Tile(2833, 2954))
        player.inventory.add("coins", 10)
        val cart = GameObjects.find(Tile(2831, 2952), "travel_cart_shilo_village")

        player.interactObject(cart, "Pay-fare")

        tick(4)
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(Tile(2778, 3210), player.tile)
    }

    @Test
    fun `Travel from brimhaven to shilo village`() {
        val player = createPlayer(Tile(2778, 3210))
        player.inventory.add("coins", 10)
        val cart = GameObjects.find(Tile(2777, 3211), "travel_cart_brimhaven")

        player.interactObject(cart, "Pay-fare")

        tick(4)
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(Tile(2834, 2951), player.tile)
    }
}
