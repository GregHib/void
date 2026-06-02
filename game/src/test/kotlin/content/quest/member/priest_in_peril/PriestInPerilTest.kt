package content.quest.member.priest_in_peril

import FakeRandom
import WorldTest
import content.entity.combat.dead
import content.quest.quest
import dialogueOption
import floorItemOption
import itemOnObject
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PriestInPerilTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `Complete quest`() {
        setRandom(object : FakeRandom() {})
        val player = createPlayer(Tile(3222, 3475))
        player.inventory.add("bucket")
        player.levels.set(Skill.Attack, 50)
        player.levels.set(Skill.Strength, 50)
        player.levels.set(Skill.Defence, 50)

        // Quest from king roald
        val roald = NPCs.findBySpawn(Tile(3222, 3476), "king_roald")
        player.npcOption(roald, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1) // Sure
        player.skipDialogues()
        assertEquals("find_drezel", player.quest("priest_in_peril"))

        // Knock at door
        player.tele(3406, 3489)
        val door = GameObjects.find(Tile(3406, 3489), "priestperiltempledoorl_closed")
        player.objectOption(door, "Knock-at")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1) // Check on drezel
        player.skipDialogues()
        player.dialogueOption(1) // Sure
        player.skipDialogues()
        assertEquals("kill_dog", player.quest("priest_in_peril"))

        // Kill dog
        player.tele(3405, 9903)
        val guardian = NPCs.findBySpawn(Tile(3405, 9902), "priestperilguarddog")
        player.npcOption(guardian, "Attack")
        player["insta_kill"] = true
        tickIf { !guardian.dead }
        assertEquals("dog_dead", player.quest("priest_in_peril"))

        // Return to roald
        player.tele(3222, 3475)
        player.npcOption(roald, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals("go_back", player.quest("priest_in_peril"))

        // Find drezel in prison
        player.tele(3413, 3487, 2)
        val prisonDoor = GameObjects.find(Tile(3413, 3487, 2), "pip_prisondoor_closed")
        player.objectOption(prisonDoor, "Talk-through")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(2) // Don't have time
        player.skipDialogues()
        player.dialogueOption(1) // Yes
        player.skipDialogues()
        assertEquals("help_drezel", player.quest("priest_in_peril"))

        // Find gold key
        player.tele(3409, 3485, 0)
        val monk = NPCs.findBySpawn(Tile(3409, 3484), "priestperilevilmonk1")
        player.npcOption(monk, "Attack")
        tickIf { !monk.dead }
        tick(6)
        val key = FloorItems.first(monk.tile, "pipkey_gold")
        player.floorItemOption(key, "Take")
        tickIf { !player.inventory.contains("pipkey_gold") }

        // Find iron key
        player.tele(3422, 9886)
        val base = GameObjects.find(Tile(3422, 9884), "priestperil_grave_base4")
        player.itemOnObject(base, 1)
        tick(2)
        assertTrue(player.inventory.contains("pipkey_iron"))

        player.tele(3422, 9890)
        val well = GameObjects.find(Tile(3423, 9890), "priestperil_well")
        player.itemOnObject(well, 0)
        tick()
        assertTrue(player.inventory.contains("bucket_murkywater"))

        // Free drezel
        player.tele(3413, 3487, 2)
        player.objectOption(prisonDoor, "Open")
        tick(1)
        player.skipDialogues()
        assertEquals("drezel_free", player.quest("priest_in_peril"))

        player.tele(3415, 3488, 2)
        var drezel = NPCs.findBySpawn(Tile(3416, 3488, 2), "priestperiltrappedmonk_vis")
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertTrue(player.inventory.contains("bucket_blessedwater"))

        player.tele(3411, 3488, 2)
        val coffin = GameObjects.find(Tile(3410, 3488, 2), "priestperil_coffin_noanim")
        player.itemOnObject(coffin, 0)
        tick(5)
        assertEquals("coffin_destroyed", player.quest("priest_in_peril"))

        // Repair barrier
        player.tele(3415, 3488, 2)
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals("meet_monument", player.quest("priest_in_peril"))

        player.tele(3439, 9895, 0)
        drezel = NPCs.findBySpawn(Tile(3440, 9895), "priestperiltrappedmonk2")
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals("essence_0", player.quest("priest_in_peril"))
        tick(1)

        player.inventory.add("rune_essence", 25)
        player.npcOption(drezel, "Talk-to")
        tick(2)
        player.skipDialogues()
        assertEquals("essence_25", player.quest("priest_in_peril"))

        player.inventory.add("pure_essence", 25)
        player.npcOption(drezel, "Talk-to")
        tick(2)
        player.skipDialogues()
        assertEquals("completed", player.quest("priest_in_peril"))
    }
}
