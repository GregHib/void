package content.area.kharidian_desert.magic_carpet

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class MagicCarpetTest : WorldTest() {

    @Test
    fun `Shantay Pass to Uzer`() {
        val player = createPlayer(Tile(3309, 3109))
        player.inventory.add("coins", 200)
        player["the_golem"] = "completed"
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3470, 3114) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Shantay Pass to Bedabin Camp`() {
        val player = createPlayer(Tile(3309, 3109))
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3181, 3045) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Shantay Pass to Pollnivneach`() {
        val player = createPlayer(Tile(3309, 3109))
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_shantay_pass", Tile(3310, 3109))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line2")

        tickIf(limit = 200) { player.tile != Tile(3351, 3003) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Pollnivneach to Shantay Pass`() {
        val player = createPlayer(Tile(3350, 3002))
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_north_pollnivneach", Tile(3350, 3001))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3309, 3110) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Bedabin Camp to Shantay Pass`() {
        val player = createPlayer(Tile(3181, 3045))
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_bedabin_camp", Tile(3181, 3044))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3309, 3110) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Uzer to Shantay Pass`() {
        val player = createPlayer(Tile(3467, 3111))
        player.inventory.add("coins", 200)
        player["the_golem"] = "completed"
        val merchant = createNPC("rug_merchant_uzer", Tile(3467, 3110))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3309, 3110) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Pollnivneach to Nardah`() {
        val player = createPlayer(Tile(3350, 2942))
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_south_pollnivneach", Tile(3350, 2943))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3402, 2916) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Pollnivneach to Sophanem`() {
        val player = createPlayer(Tile(3350, 2942))
        player.inventory.add("coins", 200)
        player["icthlarins_little_helper"] = "completed"
        val merchant = createNPC("rug_merchant_south_pollnivneach", Tile(3350, 2943))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line2")

        tickIf(limit = 200) { player.tile != Tile(3286, 2813) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Pollnivneach to Menaphos`() {
        val player = createPlayer(Tile(3350, 2942))
        player.inventory.add("coins", 200)
        player["icthlarins_little_helper"] = "completed"
        val merchant = createNPC("rug_merchant_south_pollnivneach", Tile(3350, 2943))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line3")

        tickIf(limit = 200) { player.tile != Tile(3246, 2813) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Nardah to Pollnivneach`() {
        val player = createPlayer(Tile(3401, 2917))
        player.inventory.add("coins", 200)
        val merchant = createNPC("rug_merchant_nardah", Tile(3401, 2918))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3352, 2941) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Sophanem to Pollnivneach`() {
        val player = createPlayer(Tile(3286, 2813))
        player.inventory.add("coins", 200)
        player["icthlarins_little_helper"] = "completed"
        val merchant = createNPC("rug_merchant_sophanem", Tile(3286, 2813))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3352, 2941) }

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Menaphos to Pollnivneach`() {
        val player = createPlayer(Tile(3244, 2813))
        player.inventory.add("coins", 200)
        player["icthlarins_little_helper"] = "completed"
        val merchant = createNPC("rug_merchant_attendant", Tile(3243, 2813))

        player.npcOption(merchant, "Travel")
        tick()
        player.dialogueOption("line1")

        tickIf(limit = 200) { player.tile != Tile(3352, 2941) }

        assertEquals(0, player.inventory.count("coins"))
    }
}
