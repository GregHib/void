package content.quest.free.demon_slayer

import WorldTest
import content.entity.combat.hit.damage
import content.quest.quest
import dialogueContinue
import dialogueOption
import itemOnNpc
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class DemonSlayerTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `Complete demon slayer`() {
        val player = createPlayer(Tile(3203, 3424))
        player.experience.set(Skill.Attack, Level.experience(Skill.Attack, 50))
        player.experience.set(Skill.Strength, Level.experience(Skill.Strength, 50))
        player.experience.set(Skill.Defence, Level.experience(Skill.Defence, 50))
        player.inventory.add("coins")
        player.inventory.add("bucket_of_water")
        val aris = NPCs.find(Tile(3202, 3424), "gypsy_aris")

        player.npcOption(aris, "Talk-to")
        tick(1)
        player.dialogueContinue(3)
        player.dialogueOption("line1")
        player.dialogueContinue(15)
        player.dialogueOption("line3")
        player.dialogueContinue(4)
        player.dialogueOption("line3")
        player.dialogueContinue(1)
        tick(5)
        player.dialogueContinue(1)
        tick(6)
        player.dialogueContinue(2)
        tick(4)
        player.dialogueContinue(2)
        tick(4)
        player.dialogueContinue(2)
        tick(4)
        player.dialogueOption("line4")
        player.dialogueContinue(2)
        tick(3)
        player.dialogueContinue(2)
        assertEquals("sir_prysin", player["demon_slayer", ""])
        player.dialogueOption("line4")
        player.dialogueContinue(3)

        player.tele(3204, 3472)
        val sirPrysin = NPCs.find(player.tile.regionLevel, "sir_prysin")

        player.npcOption(sirPrysin, "Talk-to")
        tick(2)
        player.dialogueContinue(1)
        player.dialogueOption("line3")
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue(4)
        player.dialogueOption("line2")
        player.dialogueContinue(5)
        player.dialogueOption("line1")
        player.dialogueContinue(5)

        player.tele(3204, 3497, 2)
        val rovin = NPCs.find(player.tile.regionLevel, "captain_rovin")
        player.npcOption(rovin, "Talk-to")
        tick(2)
        player.dialogueContinue(1)
        player.dialogueOption("line3")
        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue(1)
        player.dialogueOption("line2")
        player.dialogueContinue(2)
        player.dialogueOption("line2")
        player.dialogueContinue(4)
        player.dialogueOption("line2") // I'll fight it
        player.dialogueContinue(4)
        player.dialogueOption("line3") // Prysin said so
        player.dialogueContinue(3)
        player.dialogueOption("line1") // Why give it to you
        player.dialogueContinue(6)
        assertEquals(1, player.inventory.count("silverlight_key_captain_rovin"))

        player.tele(3111, 3162, 1)
        val traiborn = NPCs.find(player.tile.regionLevel, "traiborn")
        player.npcOption(traiborn, "Talk-to")
        tick(1)
        player.dialogueContinue(1)
        player.dialogueOption("line3") // Need a key
        player.dialogueContinue(2)
        player.dialogueOption("line3") // Any keys?
        player.dialogueContinue(6)
        player.dialogueOption("line2")
        player.dialogueContinue(3)
        player.inventory.add("bones", 25)
        player.itemOnNpc(traiborn, 5)
        tick()
        player.dialogueContinue(3)
        tick(2)
        assertEquals(1, player.inventory.count("silverlight_key_wizard_traiborn"))

        val drain = GameObjects.find(Tile(3225, 3496), "varrock_palace_drain")
        player.tele(3226, 3496, 0)
        player.itemOnObject(drain, 1)
        tick(1)

        player.tele(3226, 9897)
        val key = GameObjects.find(Tile(3225, 9897), "demon_slayer_mud_base")
        player.objectOption(key, "Take")
        tick(2)
        assertEquals(1, player.inventory.count("silverlight_key_sir_prysin"))

        player.tele(3204, 3472)
        player.npcOption(sirPrysin, "Talk-to")
        tick(2)
        player.dialogueContinue(3)
        tick(26)
        assertEquals(1, player.inventory.count("silverlight"))

        player.tele(3222, 3367)

        tick(4)
        player.dialogueContinue(2)
        tick(2)
        player.dialogueContinue(3)
        tick(15)
        assertTrue(player["demon_slayer_summoned", false])
        player.dialogueContinue(3)
        tick(8)
        player.dialogueContinue(1)
        tick(5)

        val delrith = NPCs.find(player.tile.regionLevel, "delrith")
        delrith.damage(70)
        tick(2)
        assertEquals("delrith_weakened", delrith.def(player).stringId)

        player.npcOption(delrith, "Banish")
        tick(4)
        player.dialogueContinue(1)
        val words = listOf("Carlem", "Aber", "Camerinthum", "Purchai", "Gabindo")
        repeat(5) {
            val option = DemonSlayerSpell.getWord(player, it + 1)
            player.dialogueOption("line${words.indexOf(option) + 1}")
            player.dialogueContinue(1)
            tick(3)
        }
        tick(14)
        player.dialogueContinue(1)
        tick(1)
        assertEquals("completed", player.quest("demon_slayer"))
    }
}
