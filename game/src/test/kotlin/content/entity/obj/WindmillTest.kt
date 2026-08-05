package content.entity.obj

import WorldTest
import itemOnObject
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class WindmillTest : WorldTest() {

    @Test
    fun `Hopper only holds one grain at a time`() {
        val player = createPlayer(Tile(3165, 3307, 2))
        player.inventory.add("grain", 12)

        val hopper = GameObjects.find(Tile(3166, 3307, 2), "hopper")
        player.itemOnObject(hopper, player.inventory.indexOf("grain"))
        tick()
        assertEquals(11, player.inventory.count("grain"))

        player.itemOnObject(hopper, player.inventory.indexOf("grain"))
        tick()
        assertEquals(11, player.inventory.count("grain"))
    }

    @Test
    fun `Mill grain into a pot of flour`() {
        val player = createPlayer(Tile(3165, 3307, 2))
        player.inventory.add("grain")
        player.inventory.add("empty_pot")

        val hopper = GameObjects.find(Tile(3166, 3307, 2), "hopper")
        player.itemOnObject(hopper, player.inventory.indexOf("grain"))
        tick()
        assertEquals(0, player.inventory.count("grain"))

        player.tele(3165, 3305, 2)
        val hopperControls = GameObjects.find(Tile(3166, 3305, 2), "hopper_controls")
        player.interactObject(hopperControls, "Operate")
        tick(2)

        player.tele(3165, 3306, 0)
        val flourBin = GameObjects.find(Tile(3166, 3306, 0), "flour_bin_3")
        player.interactObject(flourBin, "Take-flour")
        tick()
        assertEquals(1, player.inventory.count("pot_of_flour"))
        assertEquals(0, player.inventory.count("empty_pot"))
    }
}
