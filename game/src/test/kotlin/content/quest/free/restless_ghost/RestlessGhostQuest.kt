package content.quest.free.restless_ghost

import WorldTest
import content.entity.player.dialogue.continueDialogue
import content.quest.quest
import dialogueContinue
import dialogueOption
import equipItem
import interfaceOption
import npcOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RestlessGhostQuest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3245, 3207))

        // start quest
        val fatherAereck = NPCs.find(player.tile.regionLevel, "father_aereck")
        player.npcOption(fatherAereck, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(3) // I'm looking for a quest!
        player.fastForwardDialogue()
        tick(2)
        // quest overview interface
        player.interfaceOption("quest_intro", "startyes_layer", "Yes")
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("started", player.quest("the_restless_ghost"))

        // get ghostspeak amulet at father urhney
        player.tele(3207, 3149)
        val fatherUrhney = NPCs.find(player.tile.regionLevel, "father_urhney")
        player.npcOption(fatherUrhney, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(2) // Father Aereck sent me to talk to you.
        player.fastForwardDialogue()
        player.selectDialogueOption(1) // A ghost is haunting his graveyard.
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("ghost", player.quest("the_restless_ghost"))
        assertEquals(1, player.inventory.count("ghostspeak_amulet"))

        // equip amulet
        player.equipItem("ghostspeak_amulet", 0, "Wear")
        assertEquals(0, player.inventory.count("ghostspeak_amulet"))

        // go talk to ghost
        player.tele(3251, 3192)
        val closedCoffin = GameObjects.find(Tile(3249, 3192), "restless_ghost_coffin_closed")
        player.interactObject(closedCoffin, "Open")
        tick(5)
        val restlessGhost = NPCs.find(Tile(3250, 3195), "restless_ghost")
        player.npcOption(restlessGhost, "Talk-to")
        tick(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1) // Yep. Now, tell me what the problem is.
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("mining_spot", player.quest("the_restless_ghost"))

        // get skull from rock
        player.tele(3234, 3147)
        val rock = GameObjects.find(Tile(3234, 3145), "rocks_skull_restless_ghost_quest_base")
        player.interactObject(rock, "Search")
        tick()
        player.continueDialogue()

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("muddy_skull"))
        assertEquals("found_skull", player.quest("the_restless_ghost"))
        // flee from skeleton
        player.tele(3251, 3192)

        // put skull into coffin and complete the quest
        val coffin2 = GameObjects.find(Tile(3249, 3192), "coffin_restless_ghost_2")
        player.interactObject(coffin2, "Search")
        tick(16)

        assertEquals("completed", player.quest("the_restless_ghost"))
        assertEquals(1125.0, player.experience.get(Skill.Prayer))
    }

    private fun Player.fastForwardDialogue() {
        assertNotNull(dialogue)
        require(suspension is Suspension.Continue)
        while (suspension is Suspension.Continue) {
            dialogueContinue()
        }
    }

    private fun Player.selectDialogueOption(option: Int) {
        assertNotNull(dialogue)
        require(suspension is Suspension.IntEntry)
        dialogueOption("line$option")
    }
}
