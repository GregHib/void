package content.area.misthalin.draynor_village.wise_old_man

import WorldTest
import content.quest.quests
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class QuestPointCapeTest : WorldTest() {

    @Test
    fun `Buy a quest point cape after completing every quest`() {
        val player = createPlayer(Tile(3088, 3253))
        player["wise_old_man_met"] = true
        for (quest in quests) {
            player[quest] = "completed"
        }
        player.inventory.add("coins", 99_000)
        val wiseOldMan = createNPC("wise_old_man_draynor", Tile(3088, 3254))

        player.npcOption(wiseOldMan, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()
        player.dialogueOption(2)
        tick()
        player.skipDialogues()

        assertEquals(1, player.inventory.count("quest_point_cape"))
        assertEquals(1, player.inventory.count("quest_point_hood"))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `No cape offered without every quest`() {
        val player = createPlayer(Tile(3088, 3253))
        player["wise_old_man_met"] = true
        player.inventory.add("coins", 99_000)
        val wiseOldMan = createNPC("wise_old_man_draynor", Tile(3088, 3254))

        player.npcOption(wiseOldMan, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        tick()
        player.skipDialogues()

        assertEquals(0, player.inventory.count("quest_point_cape"))
        assertEquals(99_000, player.inventory.count("coins"))
    }
}
