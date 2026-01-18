package content.quest.free.cooks_assistant

import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import world.gregs.voidps.engine.client.instruction.handle.interactFloorItem
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.ContinueSuspension
import world.gregs.voidps.engine.suspend.IntSuspension
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CooksAssistantTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @BeforeAll
    fun beforeAllLocal() {
        loadItemSpawns(floorItems, get(), configFiles.list(Settings["spawns.items"]))
    }

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(3208, 3215, 0))

        // Start quest
        val cook = NPCs.find(Tile(3209, 3215), "cook_lumbridge")
        player.tele(3208, 3215, 0)
        player.npcOption(cook, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(1) // What's wrong?
        player.fastForwardDialogue()
        player.selectDialogueOption(1) // Yes, I'll help
        player.fastForwardDialogue()
        assertNull(player.dialogue)
        assertEquals("started", player.quest("cooks_assistant"))

        // Pick up empty pot
        val pot = floorItems[Tile(3209, 3214)].first { it.id == "empty_pot" }
        player.tele(3209, 3215, 0)
        player.interactFloorItem(pot, "Take")
        tick()
        assertEquals(1, player.inventory.count("empty_pot"))

        // Super Large Egg
        player.tele(3227, 3299)
        val egg = floorItems[Tile(3227, 3299)].first { it.id == "super_large_egg" }
        player.interactFloorItem(egg, "Take")
        tick()
        assertEquals(1, player.inventory.count("super_large_egg"))

        // Top Quality Milk
        player.tele(3263, 3278)
        val bucket = floorItems[Tile(3263, 3277)].first { it.id == "bucket" }
        player.interactFloorItem(bucket, "Take")
        tick()
        assertEquals(1, player.inventory.count("bucket"))
        val prizedDairyCow = GameObjects.find(Tile(3264, 3277), "prized_dairy_cow")
        player.objectOption(prizedDairyCow, "Milk")
        tick(8)
        assertEquals(0, player.inventory.count("bucket"))
        assertEquals(1, player.inventory.count("top_quality_milk"))

        // Extra Fine Flour
        player.inventory.add("grain")

        player.tele(3169, 3305)
        val millie = NPCs.find(Tile(3169, 3306), "millie_miller")
        player.npcOption(millie, "Talk-to")
        tick()
        player.fastForwardDialogue()
        player.selectDialogueOption(1) // Extra fine flour
        player.fastForwardDialogue()
        player.selectDialogueOption(2) // I'm fine, thanks

        player.tele(3165, 3307, 2)
        val hopper = GameObjects.find(Tile(3166, 3307, 2), "hopper")
        assertEquals(1, player.inventory.count("grain"))
        player.itemOnObject(hopper, player.inventory.indexOf("grain"))
        tick()
        assertEquals(0, player.inventory.count("grain"))

        player.tele(3165, 3305, 2)
        val hopperControls = GameObjects.find(Tile(3166, 3305, 2), "hopper_controls")
        player.interactObject(hopperControls, "Operate")
        tick()

        player.tele(3165, 3306, 0)
        val flourBin = GameObjects.find(Tile(3166, 3306, 0), "flour_bin_3")
        player.interactObject(flourBin, "Take-flour")
        tick()
        assertEquals(1, player.inventory.count("extra_fine_flour"))
        assertEquals(0, player.inventory.count("empty_pot"))

        // Hand in items
        player.tele(3208, 3215, 0)
        player.npcOption(cook, "Talk-to")
        tick()
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("completed", player.quest("cooks_assistant"))
        assertTrue(player.inventory.contains("sardine_noted", 20))
        assertTrue(player.inventory.contains("coins", 500))
    }

    private fun Player.fastForwardDialogue() {
        assertNotNull(dialogue)
        require(dialogueSuspension is ContinueSuspension)
        while (dialogueSuspension is ContinueSuspension) {
            dialogueContinue()
        }
    }

    private fun Player.selectDialogueOption(option: Int) {
        assertNotNull(dialogue)
        require(dialogueSuspension is IntSuspension)
        dialogueOption("line$option")
    }
}
