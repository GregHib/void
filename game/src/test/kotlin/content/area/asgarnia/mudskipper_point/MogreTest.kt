package content.area.asgarnia.mudskipper_point

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MogreTest : WorldTest() {

    @Test
    fun `Skippy's miniquest teaches where mogres are`() {
        val player = createPlayer(Tile(2983, 3195))
        player.inventory.add("bucket_of_water", "nettle_tea", "hangover_cure")
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Sober-Up")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line1") // Throw the water!
        player.skipDialogues()
        assertEquals(1, player["mogre_miniquest", 0])
        assertEquals(1, player.inventory.count("bucket"))

        player.npcOption(skippy, "Talk-To")
        tick()
        player.skipDialogues()
        assertEquals(2, player["mogre_miniquest", 0])
        assertEquals(1, player.inventory.count("bowl"))

        player.npcOption(skippy, "Talk-To")
        tick()
        player.skipDialogues()
        assertEquals(3, player["mogre_miniquest", 0])
    }

    @Test
    fun `Fishing explosive lures a mogre out after Skippy's miniquest`() {
        val player = createPlayer(Tile(2982, 3117))
        player["mogre_miniquest"] = 3
        player.inventory.add("fishing_explosive", 2)
        val spot = GameObjects.find(Tile(2982, 3113), "ominous_fishing_spot")

        player.itemOnObject(spot, 0)
        tick(6)

        assertEquals(1, NPCs.count { it.id == "mogre" && it["owner", ""] == player.accountName })
        assertEquals(1, player.inventory.count("fishing_explosive"))

        player.itemOnObject(spot, 0)
        tick(6)

        assertEquals(1, NPCs.count { it.id == "mogre" && it["owner", ""] == player.accountName })
        assertEquals(1, player.inventory.count("fishing_explosive"))
    }

    @Test
    fun `Fishing the ominous spot from the shore says the fish are scared`() {
        val player = createPlayer(Tile(2982, 3117))
        val spot = GameObjects.find(Tile(2982, 3113), "ominous_fishing_spot")

        player.objectOption(spot, "Lure")
        tick(6)

        assertTrue(player.containsMessage("Something seems to have scared all the fishes away..."))
    }

    @Test
    fun `Fishing explosive does nothing before Skippy's miniquest`() {
        val player = createPlayer(Tile(2982, 3117))
        player.inventory.add("fishing_explosive")
        val spot = GameObjects.find(Tile(2982, 3113), "ominous_fishing_spot")

        player.itemOnObject(spot, 0)
        tick(6)

        assertEquals(0, NPCs.count { it.id == "mogre" })
        assertEquals(1, player.inventory.count("fishing_explosive"))
    }

    @Test
    fun `Only the player who lured a mogre can attack it`() {
        val player = createPlayer(Tile(2982, 3115))
        val other = createPlayer(Tile(2983, 3115))
        val mogre = createNPC("mogre", Tile(2982, 3116))
        mogre["owner"] = player.accountName

        assertTrue(CombatApi.canAttack(player, mogre))
        assertFalse(CombatApi.canAttack(other, mogre))
    }
}
