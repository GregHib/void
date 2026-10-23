package content.quest.member.gertrudes_cat

import WorldTest
import content.quest.quest
import dialogueOption
import itemOnNpc
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class GertrudesCatTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3151, 3411, 0))
        player.inventory.clear()
        player.inventory.add("coins", 100)

        // talk to gertrude and start the quest
        val gertrude = NPCs.find(player.tile.regionLevel, "gertrude")
        player.npcOption(gertrude, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()

        assertNull(player.dialogue)
        assertEquals("spoke_to_gertrude", player.quest("gertrudes_cat"))

        // pay one of the boys to find out where fluffs is
        player.tele(3221, 3434, 0)
        val wilough = NPCs.find(player.tile.regionLevel, "wilough")
        player.npcOption(wilough, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()

        assertNull(player.dialogue)
        assertEquals(0, player.inventory.count("coins"))
        assertEquals("found_the_boys", player.quest("gertrudes_cat"))

        // fluffs won't come with the player until she is fed and watered
        player.inventory.add("doogle_sardine")
        player.inventory.add("bucket_of_milk")
        player.tele(3307, 3512, 1)
        val fluffs = NPCs.find(player.tile.regionLevel, "fluffs")
        player.npcOption(fluffs, "Pick-up")
        tick(10)
        player.skipDialogues()
        assertNull(player.dialogue)
        assertEquals("attempt_fluffs_pickup", player.quest("gertrudes_cat"))

        player.itemOnNpc(fluffs, player.inventory.indexOf("doogle_sardine"))
        tick(3)
        player.skipDialogues()
        assertEquals(true, player["gertrudes_cat_fluffs_fed", false])
        assertEquals(0, player.inventory.count("doogle_sardine"))

        player.itemOnNpc(fluffs, player.inventory.indexOf("bucket_of_milk"))
        tick(3)
        player.skipDialogues()
        assertEquals(true, player["gertrudes_cat_fluffs_milk", false])
        assertEquals(1, player.inventory.count("bucket"))
        assertEquals("attempt_fluffs_pickup", player.quest("gertrudes_cat"))

        // search the crates downstairs for the kittens
        player.tele(3305, 3501, 0)
        val crate = NPCs.find(Tile(3305, 3500, 0), "fluffs_kittens")
        player.npcOption(crate, "Search")
        tick(2)
        player.skipDialogues()

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("three_little_kittens"))
        assertEquals(true, player["three_little_kittens_found", false])

        // return the kittens to fluffs
        player.tele(3307, 3512, 1)
        player.itemOnNpc(fluffs, player.inventory.indexOf("three_little_kittens"))
        tick(60)
        player.skipDialogues()

        assertEquals(0, player.inventory.count("three_little_kittens"))
        assertEquals("fluffs_returned", player.quest("gertrudes_cat"))
        tick(30)

        // collect the reward from gertrude
        player.tele(3151, 3411, 0)
        player.npcOption(gertrude, "Talk-to")
        tick(2)
        player.skipDialogues()

        assertNull(player.dialogue)
        assertEquals("completed", player.quest("gertrudes_cat"))
        assertEquals(1, player.inventory.count("pet_kitten"))
        assertEquals(1, player.inventory.count("chocolate_cake"))
        assertEquals(1, player.inventory.count("stew"))
        assertTrue(player.experience.get(Skill.Cooking) >= 1525.0)
    }
}
