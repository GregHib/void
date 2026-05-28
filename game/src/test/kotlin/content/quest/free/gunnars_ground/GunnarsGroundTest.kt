package content.quest.free.gunnars_ground

import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import itemOnItem
import npcOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertNull

class GunnarsGroundTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Cant start with under 5 Crafting`() {
        val player = createPlayer(Tile(3099, 3422))

        val dororan = NPCs.find(Tile(3098, 3422), "dororan")

        player.npcOption(dororan, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(1)

        player.fastForwardDialogue()
        player.selectDialogueOption(2)

        player.fastForwardDialogue()
        player.selectDialogueOption(1)

        player.fastForwardDialogue()
        player.selectDialogueOption(2)

        player.fastForwardDialogue()
        player.selectDialogueOption(1)

        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("unstarted", player.quest("gunnars_ground"))
    }

    @Test
    fun `Complete Gunnars Ground`() {
        val player = createPlayer(Tile(3099, 3422))

        // clear so we know index of items later
        player.inventory.clear()

        player.experience.set(Skill.Crafting, Level.experience(Skill.Crafting, 5))
        // save current crafting exp to validate exp rewards
        var playerCraftingExp = player.experience.get(Skill.Crafting)

        val dororan = NPCs.find(Tile(3098, 3422), "dororan")

        // start quest
        player.npcOption(dororan, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)

        assertEquals("started", player.quest("gunnars_ground"))

        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("love_poem"))
        assertEquals("love_poem", player.quest("gunnars_ground"))

        // bring ring to jeff
        player.tele(3108, 3499)
        val jeffery = NPCs.find(player.tile.regionLevel, "jeffery")
        player.npcOption(jeffery, "Talk-to")
        tick(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        // check if jeffery_ring / love_poem
        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("ring_from_jeffery"))
        assertEquals("jeffery_ring", player.quest("gunnars_ground"))

        // bring ring to dororan
        player.tele(3099, 3422)
        player.npcOption(dororan, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("chisel"))
        assertEquals("engrave", player.quest("gunnars_ground"))

        player.itemOnItem(1, 0)
        tick()
        player.fastForwardDialogue()

        // player should get 125 exp for engraving
        assertEquals(playerCraftingExp + 125, player.experience.get(Skill.Crafting))
        playerCraftingExp = player.experience.get(Skill.Crafting)

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("dororans_engraved_ring"))
        assertEquals("engraved_ring", player.quest("gunnars_ground"))

        // show engraved ring to dororan
        player.npcOption(dororan, "Talk-to")
        tick()

        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("show_gudrun", player.quest("gunnars_ground"))

        // show ring to gudrun
        player.tele(3082, 3415)
        val gudrun = NPCs.find(player.tile.regionLevel, "gudrun")
        player.npcOption(gudrun, "Talk-to")
        tick(4)

        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("meet_chieftain", player.quest("gunnars_ground"))

        // talk to chieftain
        player.tele(3079, 3444)
        val chieftain = NPCs.find(Tile(3078, 3444), "chieftain_gunthor")
        player.npcOption(chieftain, "Talk-to")
        tick()

        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("tell_gudrun", player.quest("gunnars_ground"))

        // tell gudrun
        player.tele(3082, 3415)
        player.npcOption(gudrun, "Talk-to")
        tick()

        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("tell_dororan", player.quest("gunnars_ground"))

        // talk to dororan
        player.tele(3098, 3422)
        player.npcOption(dororan, "Talk-to")
        tick()

        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        tick(6)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(5)
        player.selectDialogueOption(3)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(5)
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(5)
        player.selectDialogueOption(3)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("poem", player.quest("gunnars_ground"))

        player.tele(3082, 3415)
        player.npcOption(gudrun, "Talk-to")
        tick(4)

        player.fastForwardDialogue()

        tick(6)

        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        tick(6)

        player.fastForwardDialogue()

        tick(4)

        player.fastForwardDialogue()

        tick(9)

        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        tick(4)

        assertNull(player.dialogue)
        assertEquals("completed", player.quest("gunnars_ground"))
        assertEquals(1, player.inventory.count("swanky_boots"))
        assertEquals(1, player.inventory.count("antique_lamp_gunnars_ground"))
        assertEquals(playerCraftingExp + 300, player.experience.get(Skill.Crafting))
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
