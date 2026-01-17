package content.quest.free.prince_ali_rescue

import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import itemOnItem
import itemOnNpc
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrinceAliRescueTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3302, 3164))
        player.inventory.add(
            // Key
            "soft_clay",
            "bronze_bar",
            // Disguise
            "pink_skirt",
            // Paste
            "pot_of_flour",
            "redberries",
            "bucket_of_water",
            "ashes",
            // Wig
            "onion", "onion",
            "ball_of_wool", "ball_of_wool", "ball_of_wool",
            // For Joe
            "beer", "beer", "beer",
            // For Keli
            "rope",
        )
        player.inventory.add("coins", 5)
        // Talk to Hassan to start quest
        val hassan = NPCs.find(Tile(3302, 3163), "hassan")
        player.npcOption(hassan, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueOption("line1")
        player.dialogueContinue()
        player.dialogueContinue()
        assertNull(player.dialogue)
        assertEquals("osman", player.quest("prince_ali_rescue"))

        // Talk to Osman
        player.tele(3287, 3180)
        val osman = NPCs.find(Tile(3286, 3180), "osman")
        player.npcOption(osman, "Talk-to")
        tick()
        player.dialogueContinue()
        player.dialogueContinue()
        assertEquals("leela", player.quest("prince_ali_rescue"))

        // Talk to Ned to get wig
        player.tele(3100, 3258)
        val ned = NPCs.find(Tile(3100, 3257), "ned")
        player.npcOption(ned, "Talk-to")
        tick()
        player.dialogueContinue() // Hello
        player.dialogueOption("line1") // Other things
        player.dialogueContinue()
        player.dialogueContinue() // Sure
        player.dialogueOption("line2") // Wig
        player.dialogueContinue()
        player.dialogueContinue() // Sure
        player.dialogueOption("line1") // I have them
        player.dialogueContinue()
        player.dialogueContinue() // Okay
        player.dialogueContinue() // Give wig
        assertTrue(player.inventory.contains("wig_grey"))
        player.dialogueContinue() // Not bad
        player.dialogueContinue() // Thanks
        assertNull(player.dialogue)

        // Talk to Aggie to get skin paste
        player.tele(3086, 3260)
        val aggie = NPCs.find(Tile(3085, 3260), "aggie")
        player.npcOption(aggie, "Talk-to")
        tick()
        player.dialogueContinue() // Hello
        player.dialogueOption("line2") // Prince ali
        player.dialogueContinue()
        player.dialogueContinue() // Mix now?
        player.dialogueOption("line1") // Yes
        player.dialogueContinue()
        player.dialogueContinue() // Please give items
        player.dialogueContinue() // You hand the items
        player.dialogueContinue() // Gives paste
        assertTrue(player.inventory.contains("paste"))
        player.dialogueContinue() // There you go
        assertNull(player.dialogue)

        // Talk to Aggie to get yellow dye
        player.npcOption(aggie, "Make-dyes")
        tick()
        player.dialogueOption("line3") // Yellow
        player.dialogueContinue()
        tick(3) // Walk to cauldron
        assertTrue(player.inventory.contains("yellow_dye"))
        assertFalse(player.inventory.contains("onion"))
        assertFalse(player.inventory.contains("coins"))
        player.dialogueContinue() // Aggie gives dye

        // Use dye on wig
        player.itemOnItem(player.inventory.indexOf("wig_grey"), player.inventory.indexOf("yellow_dye"))
        tick(2)
        assertTrue(player.inventory.contains("wig_blonde"))
        assertFalse(player.inventory.contains("yellow_dye"))
        assertFalse(player.inventory.contains("wig_grey"))

        // Talk to Lady Keli to get key print
        player.tele(3127, 3244)
        val keli = NPCs.find(Tile(3128, 3244), "lady_keli")
        player.npcOption(keli, "Talk-to")
        tick()
        player.dialogueContinue() // Are you keli
        player.dialogueContinue() // I am
        player.dialogueOption("line1") // You're famous
        player.dialogueContinue()
        player.dialogueContinue() // How kind
        player.dialogueOption("line2") // What's your plan?
        player.dialogueContinue()
        player.dialogueContinue() // Prisoner
        player.dialogueContinue() // Reward
        player.dialogueOption("line3") // Escape
        player.dialogueContinue()
        player.dialogueContinue() // Impossible
        player.dialogueContinue() // Key
        player.dialogueOption("line1") // Can I see it?
        player.dialogueContinue()
        player.dialogueContinue() // Sure
        player.dialogueContinue() // Shows the key
        player.dialogueOption("line1") // Can I touch it?
        player.dialogueContinue()
        player.dialogueContinue() // Okay
        player.dialogueContinue() // Take print
        assertTrue(player.inventory.contains("key_print"))
        player.dialogueContinue() // Thanks
        player.dialogueContinue() // Bye
        assertNull(player.dialogue)

        // Give key print to osman
        player.tele(3287, 3180)
        player.npcOption(osman, "Talk-to")
        tick()
        player.dialogueContinue() // Well done
        player.dialogueContinue() // Takes imprint
        assertFalse(player.inventory.contains("key_print"))
        assertFalse(player.inventory.contains("bronze_bar"))
        player.dialogueContinue() // Talk to Leela
        player.dialogueOption("line1") // Thanks
        player.dialogueContinue()
        assertEquals("leela", player.quest("prince_ali_rescue"))
        assertTrue(player["prince_ali_rescue_key_made", false])
        assertFalse(player["prince_ali_rescue_key_given", false])

        // Get key from Leela
        player.tele(3112, 3263)
        val leela = NPCs.find(Tile(3113, 3263), "leela")
        player.npcOption(leela, "Talk-to")
        tick()
        player.dialogueContinue() // Hi
        player.dialogueContinue() // Give key
        assertTrue(player.inventory.contains("bronze_key_prince_ali_rescue"))
        player.dialogueContinue() // Ready
        player.dialogueOption("line5") // Not sure
        player.dialogueContinue()
        player.dialogueContinue() // Talk to him
        assertEquals("guard", player.quest("prince_ali_rescue"))
        assertNull(player.dialogue)

        // Get Joe drunk
        player.tele(3124, 3246)
        val joe = NPCs.find(Tile(3125, 3246), "jail_guard_joe")
        player.npcOption(joe, "Talk-to")
        tick()
        player.dialogueOption("line1") // Have beer
        player.dialogueContinue()
        player.dialogueContinue() // Lovely
        player.dialogueContinue() // Drink
        assertEquals(2, player.inventory.count("beer"))
        assertEquals("joe_beer", player.quest("prince_ali_rescue"))
        player.dialogueContinue() // Takes drink
        player.dialogueContinue() // Thank you
        player.dialogueContinue() // Drunk yet
        player.dialogueContinue() // Another
        player.dialogueContinue() // No
        player.dialogueContinue() // Hold these
        assertEquals(0, player.inventory.count("beer"))
        assertEquals("joe_beers", player.quest("prince_ali_rescue"))
        player.dialogueContinue() // Give beers
        player.dialogueContinue() // Thanks
        assertNull(player.dialogue)

        // Tie up Keli
        player.tele(3127, 3244)
        player.itemOnNpc(keli, player.inventory.indexOf("rope"))
        tick()
        player.dialogueContinue() // You tie her up
        assertFalse(player.inventory.contains("rope"))
        assertEquals("keli_tied_up", player.quest("prince_ali_rescue"))
        assertNull(player.dialogue)

        // Enter prison
        player.tele(3123, 3244)
        val door = objects[Tile(3123, 3243), "draynor_prison_door_closed"]!!
        player.objectOption(door, "Open")
        tick(4)
        assertEquals(Tile(3123, 3243), player.tile)

        // Free the prince
        player.tele(3123, 3243)
        val ali = NPCs.find(Tile(3123, 3242), "prince_ali")
        player.npcOption(ali, "Talk-to")
        tick()
        player.dialogueContinue() // Rescue
        player.dialogueContinue() // Thanks
        player.dialogueContinue() // Disguise
        player.dialogueContinue() // Take this
        player.dialogueContinue() // Hand items this
        assertFalse(player.inventory.contains("wig_blonde"))
        assertFalse(player.inventory.contains("pink_skirt"))
        assertFalse(player.inventory.contains("paste"))
        assertFalse(player.inventory.contains("bronze_key_prince_ali_rescue"))
        player.dialogueContinue() // Thank you
        player.dialogueContinue() // See Leela
        player.dialogueContinue() // Escaped
        assertNull(player.dialogue)

        // Talk with hassan to complete the quest
        player.tele(3302, 3164)
        player.npcOption(hassan, "Talk-to")
        tick()
        player.dialogueContinue()
        assertEquals("completed", player.quest("prince_ali_rescue"))
    }
}
