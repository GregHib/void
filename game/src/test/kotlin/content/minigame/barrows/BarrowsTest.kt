package content.minigame.barrows

import FakeRandom
import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarrowsTest : WorldTest() {

    @Test
    fun `Complete barrows run`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = bitCount
        })
        val player = createPlayer(Tile(3574, 3298))
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true
        player.inventory.add("spade")

        player.itemOption("Dig", "spade")
        tick(2)
        val dharok = GameObjects.find(Tile(3554, 9714, 3), "dharok_sarcophagus")
        player.objectOption(dharok, "Search")
        tick(8)
        assertTrue(player["dharok_killed", false])

        player.tele(3557, 3297)
        player.itemOption("Dig", "spade")
        tick(2)
        val verac = GameObjects.find(Tile(3573, 9705, 3), "verac_sarcophagus")
        player.objectOption(verac, "Search")
        tick(8)
        assertTrue(player["verac_killed", false])

        player.tele(3565, 3289)
        player.itemOption("Dig", "spade")
        tick(2)
        val ahrim = GameObjects.find(Tile(3555, 9698, 3), "ahrim_sarcophagus")
        player.objectOption(ahrim, "Search")
        tick(8)
        assertTrue(player["ahrim_killed", false])

        player.tele(3566, 3276)
        player.itemOption("Dig", "spade")
        tick(2)
        val karil = GameObjects.find(Tile(3550, 9682, 3), "karil_sarcophagus")
        player.objectOption(karil, "Search")
        tick(8)
        assertTrue(player["karil_killed", false])

        player.tele(3577, 3283)
        player.itemOption("Dig", "spade")
        tick(2)
        val guthan = GameObjects.find(Tile(3538, 9703, 3), "guthan_sarcophagus")
        player.objectOption(guthan, "Search")
        tick(8)
        assertTrue(player["guthan_killed", false])

        player.tele(3553, 3283)
        player.itemOption("Dig", "spade")
        tick(2)
        val torag = GameObjects.find(Tile(3569, 9685, 3), "torag_sarcophagus")
        player.objectOption(torag, "Search")
        tick(1)
        player.dialogueContinue()
        player.dialogueOption("line1")
        tick(2)
        assertEquals(0, player.tile.level)

        player.tele(3552, 9694)
        val chest = GameObjects.find(Tile(3551, 9695), "barrows_chest")
        player.objectOption(chest, "Open")
        tick(1)
        player.objectOption(chest, "Search")
        tick(10)
        assertTrue(player["torag_killed", false])
        player.objectOption(chest, "Search")
        tick(2)

        println(player.inventory.items.toList())
        assertTrue(player.containsMessage("The cave begins to collapse"))
    }
}