package content.quest.member.myreque

import WorldTest
import content.quest.quest
import dialogueOption
import floorItemOption
import interfaceOption
import itemOnNpc
import itemOnObject
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NatureSpiritTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `Complete quest`() {
        val player = createPlayer(Tile(3439, 9895))
        player["auto_retaliate"] = true
        player["insta_kill"] = true
        player.equipment.set(EquipSlot.Amulet.index, "ghostspeak_amulet")
        player.levels.set(Skill.Crafting, 18)
        player.levels.set(Skill.Prayer, 99)
        player.inventory.add("silver_bar")
        player.inventory.add("sickle_mould")
        player["priest_in_peril"] = "completed_wolfbane"

        val drezel = NPCs.findBySpawn(Tile(3440, 9895), "priestperiltrappedmonk2")
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(2) // Anything else interesting
        player.skipDialogues()
        player.dialogueOption(2) // What is it?
        player.skipDialogues()
        player.dialogueOption(4) // Yes, I'll go look
        player.skipDialogues()
        player.dialogueOption(1) // Yes, I'm sure
        player.skipDialogues()
        assertEquals("find_filliman", player.quest("nature_spirit"))

        player.tele(3444, 3458)
        val swampGate = GameObjects.find(Tile(3444, 3458), "gate_mort_myre_closed")
        player.objectOption(swampGate, "Open")
        tick(1)
        player.interfaceOption("warning_mort_myre", "yes", "Yes")
        tick(4)
        assertEquals(Tile(3444, 3457), player.tile)

        player.tele(3440, 3337)
        val grotto = GameObjects.find(Tile(3440, 3337), "grotto_door_druidicspirit")
        player.objectOption(grotto, "Enter")
        tick(2)
        player.skipDialogues()

        // Convince filliman he's a ghost
        val bowl = FloorItems.add(Tile(3437, 3337), "washing_bowl")
        player.floorItemOption(bowl, "Take")
        tick(4)
        assertTrue(player.inventory.contains("washing_bowl"))
        val mirror = FloorItems.first(Tile(3437, 3337), "mirror")
        player.floorItemOption(mirror, "Take")
        tick(3)
        assertTrue(player.inventory.contains("mirror"))

        val filliman = NPCs.find(Tile(3440, 3336), "filliman_tarlock_ghost")
        player.itemOnNpc(filliman, player.inventory.indexOf("mirror"))
        tick(1)
        player.skipDialogues()

        val grottoTree = GameObjects.find(Tile(3439, 3338), "grotto_druidicspirit")
        player.objectOption(grottoTree, "Search")
        tick(6)
        player.skipDialogues()
        assertTrue(player.inventory.contains("journal_nature_spirit"))

        player.itemOnNpc(filliman, player.inventory.indexOf("journal_nature_spirit"))
        tick(4)
        player.skipDialogues()
        player.dialogueOption(4)
        player.skipDialogues()
        assertEquals("spell", player.quest("nature_spirit"))
        assertTrue(player.inventory.contains("druidic_spell"))

        // Return to drezel
        player.tele(3439, 9895)
        player.npcOption(drezel, "Talk-to")
        tick(1)
        player.skipDialogues()
        tick(3)
        player.skipDialogues()
        assertEquals("blessed_spell", player.quest("nature_spirit"))

        // Cast spell
        player.tele(3440, 3455)
        player.itemOption("Cast", "druidic_spell")
        tick(3)
        assertEquals("spell_cast", player.quest("nature_spirit"))

        val log = GameObjects.find(Tile(3440, 3454), "log_druidicspirit2")
        player.objectOption(log, "Pick")
        tick(2)
        assertTrue(player.inventory.contains("mort_myre_fungus"))

        // Transform
        player.tele(3440, 3337)
        player.npcOption(filliman, "Talk-to")
        tick(1)
        player.skipDialogues()

        val westStone = GameObjects.find(Tile(3439, 3336), "stonedisc_ds_nature")
        player.itemOnObject(westStone, player.inventory.indexOf("mort_myre_fungus"))
        tick(3)
        assertTrue(player["ns_brown_correct", false])

        val eastStone = GameObjects.find(Tile(3441, 3336), "stonedisc_ds_spirit")
        player.itemOnObject(eastStone, player.inventory.indexOf("a_used_spell"))
        tick(3)
        assertTrue(player["ns_grey_correct", false])

        player.tele(3440, 3335)
        player.npcOption(filliman, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(3)
        player.skipDialogues()
        tick(4)
        assertEquals("transform_ready", player.quest("nature_spirit"))

        // Grotto
        player.objectOption(grotto, "Enter")
        tick(4)
        assertEquals(Tile(3442, 9734), player.tile)
        player.tele(3442, 9739)
        tick(4)
        assertEquals("transform_start", player.quest("nature_spirit"))

        val grottoMiddle = GameObjects.find(Tile(3441, 9740), "druidic_spirit_grotto")
        player.objectOption(grottoMiddle, "Search")
        tick(5)
        player.skipDialogues()
        tick(12)
        assertEquals("transform_done", player.quest("nature_spirit"))

        // Silver sickle
        player.tele(2975, 3369)
        val furnace = GameObjects.find(Tile(2976, 3368), "furnace_al_kharid")
        player.itemOnObject(furnace, player.inventory.indexOf("silver_bar"))
        tick(1)
        player.interfaceOption("silver_mould", "sickle_mould_button", "Make 1")
        tick(4)
        assertTrue(player.inventory.contains("silver_sickle"))

        // Druid pouch
        val natureSpirit = NPCs.find(RegionLevel(13720), "filliman_tarlock_ghost")
        player.tele(natureSpirit.tile)
        player.npcOption(natureSpirit, "Talk-to")
        tick(2)
        player.skipDialogues()
        tick(5)
        player.skipDialogues()

        assertTrue(player.inventory.contains("silver_sickle_b"))
        assertTrue(player.inventory.contains("druid_pouch"))

        player.tele(3423, 3336)

        var count = 100
        while (count-- > 0 && player.inventory.count("mort_myre_fungus") < 3) {
            val log1 = GameObjects.findOrNull(Tile(3423, 3337), "log_druidicspirit2")
            if (log1 != null) {
                player.objectOption(log1, "Pick")
                tick(1)
            }
            val log2 = GameObjects.findOrNull(Tile(3422, 3336), "log_druidicspirit2")
            if (log2 != null) {
                player.objectOption(log2, "Pick")
                tick(1)
            }
            if (log1 == null && log2 == null) {
                player.itemOption("Bloom", "silver_sickle_b")
                tick(1)
            }
        }
        assertEquals(4, player.inventory.count("mort_myre_fungus"))

        player.itemOption("Fill", "druid_pouch")
        assertEquals(3, player.inventory.count("druid_pouch_2"))

        // Kill ghasts
        repeat(3) {
            val ghast = NPCs.add("ghast", player.tile.addY(1))
            tick(2)
            ghast.interactPlayer(player, "Attack")
            tick(2)
            player.interactNpc(ghast, "Attack")
            tick(2)
        }
        assertEquals("ghasts_killed", player.quest("nature_spirit"))

        // Quest complete
        player.tele(natureSpirit.tile)
        player.npcOption(natureSpirit, "Talk-to")
        tick(2)
        player.skipDialogues()
        tick(10)
        assertEquals(1, player.tile.level)
        assertEquals("completed", player.quest("nature_spirit"))
    }
}
