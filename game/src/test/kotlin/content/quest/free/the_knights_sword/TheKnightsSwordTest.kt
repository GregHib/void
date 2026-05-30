package content.quest.free.the_knights_sword

import FakeRandom
import WorldTest
import content.quest.quest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class TheKnightsSwordTest : WorldTest() {
    override var loadNpcs: Boolean = true

    @Test
    fun `Complete the quest`() {
        val player = createPlayer(Tile(2973, 3344, 0))

        player.inventory.clear()

        // set mining level to 99, give dpick and set godmode for mining the blurite
        player.experience.set(Skill.Mining, Level.experience(Skill.Mining, 99))
        player.inventory.add("dragon_pickaxe")
        player["god_mode"] = true
        // items for quest
        player.inventory.add("iron_bar", 2)
        player.inventory.add("redberry_pie")

        // talk to squire and start quest
        val squireAsrol = NPCs.find(player.tile.regionLevel, "squire_asrol")
        player.npcOption(squireAsrol, "Talk-to")
        tick(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(2)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("started", player.quest("the_knights_sword"))

        // teleport and talk to reldo
        player.tele(3210, 3494, 0)
        val reldo = NPCs.find(player.tile.regionLevel, "reldo")
        player.npcOption(reldo, "Talk-to")
        tick(4)
        player.fastForwardDialogue()
        player.selectDialogueOption(3)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("find_thurgo", player.quest("the_knights_sword"))

        // teleport and talk to thurgo - give redberry pie
        player.tele(3000, 3144, 0)
        val thurgo = NPCs.find(player.tile.regionLevel, "thurgo")
        player.npcOption(thurgo, "Talk-to")
        tick(5)
        player.selectDialogueOption(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(0, player.inventory.count("redberry_pie"))
        assertEquals("happy_thurgo", player.quest("the_knights_sword"))

        // talk to thurgo again after pie
        player.npcOption(thurgo, "Talk-to")
        tick(5)
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("picture", player.quest("the_knights_sword"))

        // teleport to squire and ask about portrait
        player.tele(2973, 3344, 0)
        player.npcOption(squireAsrol, "Talk-to")
        tick(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals("cupboard", player.quest("the_knights_sword"))

        // teleport to cupboard to steal the portrait
        player.tele(2984, 3335, 2)

        // teleport sir vyvin away, so he does not interrupt the portrait heist
        val sirVyvin = NPCs.find(player.tile.regionLevel, "sir_vyvin")
        sirVyvin.tele(0, 0, 0)

        // open cupboard, search cupboard, steal portrait
        val cupboardClosed = GameObjects.find(Tile(2984, 3336, 2), "cupboard_the_knights_sword_closed")
        player.interactObject(cupboardClosed, "Open")
        tick()
        val cupboardOpen = GameObjects.find(Tile(2984, 3336, 2), "cupboard_the_knights_sword_opened")
        player.interactObject(cupboardOpen, "Search")
        tick()
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(1, player.inventory.count("portrait"))
        assertEquals("cupboard", player.quest("the_knights_sword"))

        // back to thurgo, give portrait
        player.tele(3000, 3144, 0)
        player.npcOption(thurgo, "Talk-to")
        tick(5)
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(0, player.inventory.count("portrait"))
        assertEquals("blurite_sword", player.quest("the_knights_sword"))

        // disable thing, so mining works properly and we can mine the blurite ore
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })

        // tele to rock and mine rock
        player.tele(3049, 9567, 0)
        val bluriteRock = GameObjects.find(Tile(3049, 9566, 0), "blurite_rocks_asgarnian_ice_1")
        player.interactObject(bluriteRock, "Mine")
        tick(4)

        assertEquals(1, player.inventory.count("blurite_ore"))
        assertEquals("blurite_sword", player.quest("the_knights_sword"))

        // back to thurgo, give iron bars, blurite ore, get sword
        player.tele(3000, 3144, 0)
        player.npcOption(thurgo, "Talk-to")
        tick(5)
        player.selectDialogueOption(1)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(0, player.inventory.count("iron_bar"))
        assertEquals(0, player.inventory.count("blurite_ore"))
        assertEquals(1, player.inventory.count("blurite_sword"))
        assertEquals("blurite_sword", player.quest("the_knights_sword"))

        // teleport to squire, give sword, complete quest
        player.tele(2973, 3344, 0)
        player.npcOption(squireAsrol, "Talk-to")
        tick(2)
        player.fastForwardDialogue()

        assertNull(player.dialogue)
        assertEquals(0, player.inventory.count("blurite_sword"))
        assertEquals("completed", player.quest("the_knights_sword"))
        assertEquals(12725.0, player.experience.get(Skill.Smithing))
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
