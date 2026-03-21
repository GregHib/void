package content.quest.member.lost_city

import WorldTest
import dialogueContinue
import dialogueOption
import equipItem
import itemOnItem
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LostCityTest : WorldTest() {
    override var loadNpcs = true

    @Test
    fun `Complete lost city quest`() {
        val player = createPlayer(Tile(3150, 3207))
        player.levels.set(Skill.Woodcutting, 36)
        player.levels.set(Skill.Crafting, 35)
        player.inventory.add("bronze_hatchet")
        player.inventory.add("knife")
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true

        val warrior = NPCs.find(player.tile.regionLevel, "warrior_lumbridge")
        player.npcOption(warrior, "Talk-to")
        tick(2)
        player.dialogueContinue(1)
        player.dialogueOption("line1")
        player.dialogueContinue(2)
        player.dialogueOption("line1") // Who's zanaris?
        player.dialogueContinue(2)
        player.dialogueOption("line1") // How find?
        player.dialogueContinue(2)
        player.dialogueOption("line2") // You don't know
        player.dialogueContinue(3)
        assertEquals("started", player["lost_city", "unstarted"])

        player.tele(3138, 3211)
        var tree = GameObjects.find(Tile(3138, 3212), "lost_city_tree")
        player.interactObject(tree, "Chop")
        tick(2)
        player.dialogueContinue(5)
        player.dialogueOption("line2") // Been in that shed
        player.dialogueContinue(7)
        assertEquals("find_staff", player["lost_city", "unstarted"])

        player.tele(2821, 3374)
        val monk = NPCs.find(Tile(2822, 3374), "cave_monk")
        player.npcOption(monk, "Talk-to")
        tick(1)
        player.dialogueContinue(3)
        player.dialogueOption("line2") // Risk it
        player.dialogueContinue(1)
        assertEquals(Tile(2822, 9774), player.tile)

        player.tele(2859, 9735)
        tree = GameObjects.find(Tile(2860, 9734), "dramen_tree")
        player.interactObject(tree, "Chop down")
        tick(2)
        assertEquals("tree_spirit", player["lost_city", "unstarted"])
        // Fight tree spirit
        tick(4)
        assertEquals("spirit_killed", player["lost_city", "unstarted"])
        player.interactObject(tree, "Chop down")
        tick(5)

        assertTrue(player.inventory.contains("dramen_branch"))
        player.itemOnItem(1, 2)
        tick(2)
        assertTrue(player.inventory.contains("dramen_staff"))

        player.tele(3201, 3169)
        player.equipItem("dramen_staff")
        val door = GameObjects.find(Tile(3201, 3169), "zanaris_door_closed")
        player.interactObject(door, "Open")
        tick(9)
        assertEquals(Tile(2452, 4473), player.tile)
        assertEquals(3, player["quest_points", 0])
        assertEquals("completed", player["lost_city", "unstarted"])
    }
}
