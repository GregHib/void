package content.quest.free.vampire_slayer

import WorldTest
import content.entity.combat.Target
import content.entity.combat.hit.damage
import content.entity.player.bank.bank
import content.quest.quest
import content.quest.questStage
import dialogueOption
import messages
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VampireSlayerTest : WorldTest() {
    override var loadNpcs = true

    @Test
    fun `Accept quest, buy advice, collect garlic and defeat Count for correct reward only once`() {
        val player = createPlayer(Tile(3098, 3267))
        player.talk("morgan")
        player.dialogueOption(1)
        player.skipDialogues()
        assertEquals("started", player.quest("vampire_slayer"))
        assertEquals(1, player.questStage("vampire_slayer"))

        player.inventory.add("beer")
        player.tele(3222, 3395)
        player.talk("dr_harlow")
        assertEquals("armed", player.quest("vampire_slayer"))
        assertEquals(2, player.questStage("vampire_slayer"))
        assertFalse(player.inventory.contains("beer"))
        assertTrue(player.inventory.contains("stake"))
        assertTrue(player.inventory.contains("stake_hammer"))

        player.tele(3097, 3269, 1)
        player.objectOption(GameObjects.find(Tile(3096, 3269, 1), "morgan_garlic_drawers"), "Open")
        tick()
        player.skipDialogues()
        assertTrue(player.inventory.contains("garlic"))

        player.tele(3079, 9784, 0)
        val count = player.openCoffin()!!
        assertEquals(9356, count.def.id)
        assertFalse(Target.attackable(createPlayer(Tile(3078, 9784)), count))
        val beforeXp = player.experience.get(Skill.Attack)
        val beforePoints = player["quest_points", 0]
        count.damage(1000, source = player)
        tickIf(20) { player.quest("vampire_slayer") != "completed" }
        assertEquals(3, player.questStage("vampire_slayer"))
        assertEquals(beforeXp + 4825.0, player.experience.get(Skill.Attack))
        assertEquals(beforePoints + 3, player["quest_points", 0])
        assertFalse(player.inventory.contains("stake"))
        assertTrue(player.inventory.contains("stake_hammer"))
        tick(12)
        player.openCoffin(expectSpawn = false)
        assertEquals(beforeXp + 4825.0, player.experience.get(Skill.Attack))
        assertEquals(beforePoints + 3, player["quest_points", 0])
    }

    @Test
    fun `Declining Morgan does not start the quest`() {
        val player = createPlayer(Tile(3098, 3267))
        player.talk("morgan")
        player.dialogueOption(3)
        player.skipDialogues()
        assertEquals("unstarted", player.quest("vampire_slayer"))
        player.tele(3079, 9784, 0)
        player.openCoffin(expectSpawn = false)
    }

    @Test
    fun `Harlow requires beer and sufficient room without consuming payment prematurely`() {
        val player = createPlayer(Tile(3222, 3395))
        player["vampire_slayer"] = "started"
        player.talk("dr_harlow")
        assertEquals("started", player.quest("vampire_slayer"))
        player.inventory.add("beer")
        player.inventory.add("logs", 27)
        player.talk("dr_harlow")
        assertEquals("started", player.quest("vampire_slayer"))
        assertTrue(player.inventory.contains("beer"))
        assertFalse(player.inventory.contains("stake"))
        player.inventory.remove("logs")
        player.talk("dr_harlow")
        assertEquals("armed", player.quest("vampire_slayer"))
        assertEquals(0, player.inventory.spaces)
    }

    @Test
    fun `Lost tools can be replaced for free but banked or equipped tools are not duplicated`() {
        val player = createPlayer(Tile(3222, 3395))
        player["vampire_slayer"] = "armed"
        player.bank.add("stake")
        player.equipment.add("stake_hammer")
        player.talk("dr_harlow")
        assertFalse(player.inventory.contains("stake"))
        assertFalse(player.inventory.contains("stake_hammer"))
        player.bank.remove("stake")
        player.equipment.remove("stake_hammer")
        player.talk("dr_harlow")
        assertTrue(player.inventory.contains("stake"))
        assertTrue(player.inventory.contains("stake_hammer"))
    }

    @Test
    fun `Missing stake or hammer prevents killing Count even after a fight has started`() {
        val player = createPlayer(Tile(3079, 9784))
        player["vampire_slayer"] = "armed"
        player.inventory.add("stake")
        val count = player.openCoffin()!!
        count.damage(1000, source = player)
        tick(2)
        assertFalse(count.dead)
        assertTrue(count.levels.get(Skill.Constitution) > 0)
        assertEquals("armed", player.quest("vampire_slayer"))
        player.inventory.add("hammer")
        player.inventory.remove("stake")
        count.damage(1000, source = player)
        tick(2)
        assertFalse(count.dead)
        assertEquals("armed", player.quest("vampire_slayer"))
        player.equipment.add("stake")
        count.damage(1000, source = player)
        tickIf(20) { player.quest("vampire_slayer") != "completed" }
        assertFalse(player.equipment.contains("stake"))
        assertTrue(player.inventory.contains("hammer"))
    }

    @Test
    fun `Coffin cannot duplicate a boss and leaving the cellar removes it`() {
        val player = createPlayer(Tile(3079, 9784))
        player["vampire_slayer"] = "armed"
        val count = player.openCoffin()!!
        assertEquals(count, player.openCoffin())
        assertEquals(1, NPCs.count { it.id == "count_draynor_normal" })
        player.tele(3115, 3356)
        tickIf(10) { count.index != -1 }
        assertFalse(player.contains("vampire_slayer_count"))
    }

    @Test
    fun `Logging out removes the personal boss`() {
        val player = createPlayer(Tile(3079, 9784))
        player["vampire_slayer"] = "armed"
        val count = player.openCoffin()!!
        Despawn.player(player)
        tick(2)
        assertEquals(-1, count.index)
        assertFalse(player.contains("vampire_slayer_count"))
    }

    @Test
    fun `Ordinary Harlow dialogue still works without starting the quest`() {
        val player = createPlayer(Tile(3222, 3395))
        player.inventory.add("beer")
        player.talk("dr_harlow")
        player.dialogueOption(2)
        player.skipDialogues()
        assertFalse(player.inventory.contains("beer"))
        assertEquals("unstarted", player.quest("vampire_slayer"))
    }

    private fun Player.talk(id: String) {
        val npc = NPCs.find(tile.regionLevel, id)
        npcOption(npc, "Talk-to")
        tickIf(30) { dialogue == null }
        skipDialogues()
    }

    private fun Player.openCoffin(expectSpawn: Boolean = true): NPC? {
        objectOption(GameObjects.find(Tile(3080, 9785), "coffin"), "Open")
        if (expectSpawn) tickIf(15) { get<NPC>("vampire_slayer_count") == null } else tick(2)
        tick()
        val count = get<NPC>("vampire_slayer_count")
        if (expectSpawn) {
            assertTrue(count != null, "Opening the coffin should spawn Count Draynor: tile=$tile mode=$mode messages=$messages")
        } else {
            assertTrue(count == null, "The coffin should not spawn Count Draynor")
        }
        return count
    }
}
