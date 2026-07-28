package content.minigame.mage_arena

import WorldTest
import containsMessage
import content.entity.combat.hit.directHit
import content.skill.magic.book.modern.teleBlockCounter
import content.skill.melee.weapon.multiTargets
import dialogueContinue
import dialogueOption
import floorItemOption
import interfaceOption
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

internal class MageArenaTest : WorldTest() {

    @Test
    fun `Kolodion refuses wizards below level 60 magic`() {
        val player = createPlayer(Tile(2539, 4716), "mage-1")
        val kolodion = createNPC("kolodion", Tile(2540, 4716))

        player.npcOption(kolodion, "Talk-to")
        tick(1)
        player.skipDialogues()

        assertTrue(player.containsMessage("Hah! A wizard of your level? Don't be absurd.") || player["mage_arena", "unstarted"] == "unstarted")
        assertEquals("unstarted", player["mage_arena", "unstarted"])
    }

    @Test
    fun `Accepting Kolodion's challenge teleports into the arena and starts the fight`() {
        val player = createPlayer(Tile(2539, 4716), "mage-2")
        player.levels.set(Skill.Magic, 60)
        val kolodion = createNPC("kolodion", Tile(2540, 4716))

        player.npcOption(kolodion, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1) // Can I fight here?
        player.skipDialogues()
        player.dialogueOption(1) // Yes indeed.
        player.skipDialogues()
        player.dialogueOption(1) // Okay, let's fight.
        player.skipDialogues()
        tick(6)

        assertEquals("started", player["mage_arena", "unstarted"])
        assertEquals(Tile(3104, 3933), player.tile)
        val boss = NPCs.indexed(player["kolodion_index", -1])
        assertNotNull(boss)
        assertEquals("kolodion_human", boss!!.id)
    }

    @Test
    fun `Kolodion shapeshifts through all forms and defeat completes the minigame`() {
        val player = createPlayer(Tile(2539, 4716), "mage-3")
        player["god_mode"] = true
        startFight(player)

        for (form in listOf("kolodion_ogre", "kolodion_troll", "kolodion_dark_beast", "kolodion_demon")) {
            kill(player)
            val boss = NPCs.indexed(player["kolodion_index", -1])
            assertNotNull(boss, "Expected $form to spawn")
            assertEquals(form, boss!!.id)
        }
        kill(player)
        tick(4)

        assertEquals("completed", player["mage_arena", "unstarted"])
        assertEquals(Tile(2540, 4717), player.tile)
        assertNull(player.get<Int>("kolodion_index"))
        assertFalse(NPCs.at(Tile(3105, 3933).regionLevel).any { it.id.startsWith("kolodion_") })
    }

    @Test
    fun `Leaving the arena despawns the boss and progress resumes at the same form`() {
        val player = createPlayer(Tile(2539, 4716), "mage-4")
        player["god_mode"] = true
        startFight(player)
        kill(player)
        assertEquals("ogre", player["kolodion_form", "human"])

        player.tele(Tile(3105, 3960))
        tick(2)

        assertNull(player.get<Int>("kolodion_index"))
        assertFalse(NPCs.at(Tile(3105, 3933).regionLevel).any { it.id.startsWith("kolodion_") })

        val kolodion = createNPC("kolodion", Tile(3105, 3959))
        player.npcOption(kolodion, "Talk-to")
        tick(1)
        player.dialogueContinue(2)
        tick(6)

        val boss = NPCs.indexed(player["kolodion_index", -1])
        assertNotNull(boss)
        assertEquals("kolodion_ogre", boss!!.id)
    }

    @Test
    fun `Fight restarts at human form when a save holds an unknown form`() {
        val player = createPlayer(Tile(2539, 4716), "mage-20")
        player["god_mode"] = true
        player.levels.set(Skill.Magic, 60)
        player["mage_arena"] = "started"
        player["kolodion_form"] = "spider"
        val kolodion = createNPC("kolodion", player.tile.addX(1))

        player.npcOption(kolodion, "Talk-to")
        tick(1)
        player.dialogueContinue(2)
        tick(6)

        val boss = NPCs.indexed(player["kolodion_index", -1])
        assertNotNull(boss)
        assertEquals("kolodion_human", boss!!.id)
        assertEquals("human", player["kolodion_form", "human"])
    }

    @Test
    fun `Only the owner can attack Kolodion and only with magic`() {
        val player = createPlayer(Tile(2539, 4716), "mage-5")
        player["god_mode"] = true
        startFight(player)
        val boss = NPCs.indexed(player["kolodion_index", -1])!!

        player.npcOption(boss, "Attack")
        tick(2)
        assertTrue(player.containsMessage("You can only use magic in the arena!"))

        val other = createPlayer(Tile(3103, 3934), "mage-6")
        other.npcOption(boss, "Attack")
        tick(2)
        assertTrue(other.containsMessage("He's not interested in fighting you."))
    }

    @Test
    fun `God spells are blocked outside the arena until cast 100 times`() {
        val player = createGodSpellCaster(emptyTile, "mage-7")
        val target = createNPC("giant_rat", emptyTile.addY(2))

        player.interfaceOption("modern_spellbook", "flames_of_zamorak", option = "Autocast")
        player.npcOption(target, "Attack")
        tick(5)

        assertTrue(player.containsMessage("You need to cast Flames of zamorak 100 more times inside the Mage Arena."))
        assertEquals(400, player.inventory.count("blood_rune"))
    }

    @Test
    fun `Casting inside the arena counts towards unlocking god spells`() {
        val arenaTile = Tile(3104, 3933)
        val player = createGodSpellCaster(arenaTile, "mage-15")
        player["flames_of_zamorak_casts"] = 99
        val target = createNPC("giant_rat", arenaTile.addY(2))

        player.interfaceOption("modern_spellbook", "flames_of_zamorak", option = "Autocast")
        player.npcOption(target, "Attack")
        tickIf { player["flames_of_zamorak_casts", 0] < 100 }

        assertEquals(100, player["flames_of_zamorak_casts", 0])
        assertTrue(player.containsMessage("You can now cast Flames of zamorak outside the Arena."))
        assertTrue(player.inventory.count("blood_rune") < 400)
    }

    @Test
    fun `God spells work outside the arena after 100 casts`() {
        val player = createGodSpellCaster(emptyTile, "mage-16")
        player["flames_of_zamorak_casts"] = 100
        val target = createNPC("giant_rat", emptyTile.addY(2))

        player.interfaceOption("modern_spellbook", "flames_of_zamorak", option = "Autocast")
        player.npcOption(target, "Attack")
        tickIf(50) { player.inventory.count("blood_rune") == 400 }

        assertFalse(player.containsMessage("You need to cast Flames of zamorak 100 more times inside the Mage Arena."))
        assertTrue(player.inventory.count("blood_rune") < 400)
    }

    private fun createGodSpellCaster(tile: Tile, name: String): Player {
        val player = createPlayer(tile, name)
        player.experience.set(Skill.Magic, 14000000.0)
        player.levels.set(Skill.Magic, 99)
        player.equipment.set(EquipSlot.Weapon.index, "zamorak_staff")
        player.inventory.add("blood_rune", 400)
        player.inventory.add("fire_rune", 400)
        player.inventory.add("air_rune", 400)
        return player
    }

    @Test
    fun `Battle mages ignore players wearing their god's cape`() {
        val player = createPlayer(Tile(3104, 3933), "mage-8")
        player["god_mode"] = true
        player.equipment.set(EquipSlot.Cape.index, "zamorak_cape")
        val mage = createNPC("battle_mage_zamorak", Tile(3105, 3933))

        tick(10)
        assertFalse(mage.mode is CombatMovement)

        player.equipment.set(EquipSlot.Cape.index, "saradomin_cape")
        tickIf(50) { mage.mode !is CombatMovement }
        assertTrue(mage.mode is CombatMovement)
    }

    @Test
    fun `Praying at a statue grants one god cape at a time`() {
        val player = createPlayer(Tile(2508, 4692), "mage-9")
        val statue = createObject("statue_of_saradomin", Tile(2508, 4694))

        player.objectOption(statue, "Pray at")
        tick(10)
        val cape = FloorItems.firstOrNull(Tile(2508, 4693), "saradomin_cape")
        assertNotNull(cape)

        player.floorItemOption(cape!!, "Take")
        tick(2)
        assertEquals(1, player.inventory.count("saradomin_cape"))

        val zamorak = createObject("statue_of_zamorak", Tile(2510, 4694))
        player.objectOption(zamorak, "Pray at")
        tick(10)
        assertTrue(player.containsMessage("...but there is no response."))
        assertNull(FloorItems.firstOrNull(Tile(2510, 4693), "zamorak_cape"))
    }

    @Test
    fun `Only one sacred cape can be possessed at a time`() {
        val player = createPlayer(Tile(2508, 4692), "mage-10")
        player.inventory.add("saradomin_cape")
        val cape = FloorItems.add(Tile(2508, 4692), "guthix_cape", owner = player)

        player.floorItemOption(cape, "Take")
        tick(2)

        assertTrue(player.containsMessage("You may only possess one sacred cape at a time."))
        assertEquals(0, player.inventory.count("guthix_cape"))
    }

    @Test
    fun `Dropping a god cape destroys it`() {
        val player = createPlayer(Tile(2508, 4692), "mage-11")
        player.inventory.add("zamorak_cape")

        player.itemOption("Drop", "zamorak_cape")
        tick(2)

        assertEquals(0, player.inventory.count("zamorak_cape"))
        assertNull(FloorItems.firstOrNull(player.tile, "zamorak_cape"))
        assertTrue(player.containsMessage("The cape ignites and burns up as it touches the ground."))
    }

    @Test
    fun `Chamber guardian hands out a staff matching the cape then opens his shop`() {
        val player = createPlayer(Tile(2507, 4694), "mage-12")
        val guardian = createNPC("chamber_guardian", Tile(2507, 4695))

        player.npcOption(guardian, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertEquals(0, player.inventory.count("guthix_staff"))

        player.inventory.add("guthix_cape")
        player.npcOption(guardian, "Talk-to")
        tick(1)
        player.skipDialogues()

        assertEquals(1, player.inventory.count("guthix_staff"))
        assertEquals("staff_received", player["mage_arena", "unstarted"])
    }

    @Test
    fun `Gundai opens the bank through dialogue and the Bank option`() {
        val player = createPlayer(Tile(2533, 4714), "mage-21")
        val gundai = createNPC("gundai", Tile(2534, 4714))

        player.npcOption(gundai, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1) // Access my bank account
        tick(1)
        player.skipDialogues()
        tick(2)
        assertTrue(player.hasOpen("bank"))

        player.closeInterfaces()
        player.npcOption(gundai, "Bank")
        tick(2)
        assertTrue(player.hasOpen("bank"))
    }

    @Test
    fun `Lundail sells rune stones through dialogue`() {
        val player = createPlayer(Tile(2533, 4719), "mage-22")
        val lundail = createNPC("lundail", Tile(2534, 4719))

        player.npcOption(lundail, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1) // What are you selling?
        tick(1)
        player.skipDialogues()
        tick(2)
        assertTrue(player.hasOpen("shop"))
    }

    @Test
    fun `Sparkling pool is closed until Kolodion is defeated`() {
        val player = createPlayer(Tile(2542, 4718), "mage-13")
        val pool = createObject("sparkling_pool_mage_bank", Tile(2542, 4720))

        player.objectOption(pool, "Step-into")
        tick(10)
        assertTrue(player.containsMessage("Your boots get wet."))
        assertTrue(player.tile.y > 4700, "Player shouldn't be teleported to the chamber")

        player["mage_arena"] = "completed"
        player.objectOption(pool, "Step-into")
        tick(15)
        assertEquals(Tile(2509, 4689), player.tile)
    }

    @Test
    fun `Levers can't be pulled while teleblocked without a forinthry bracelet`() {
        val player = createPlayer(Tile(3105, 3956), "mage-17")
        player["mage_arena"] = "completed"
        player.teleBlockCounter = 500
        val lever = createObject("mage_arena_lever_in", Tile(3104, 3957))

        player.objectOption(lever, "Pull")
        tick(5)
        assertTrue(player.containsMessage("A magical force has stopped you from teleporting."))
        assertTrue(player.tile.y > 3951, "Player shouldn't be teleported")

        player.equipment.set(EquipSlot.Hands.index, "forinthry_bracelet_5")
        player.objectOption(lever, "Pull")
        tick(12)
        assertEquals(Tile(3105, 3951), player.tile)
    }

    @Test
    fun `Multi target spells don't splash another player's Kolodion`() {
        val owner = createPlayer(Tile(3105, 3930), "mage-18")
        val bystander = createPlayer(Tile(3105, 3928), "mage-19")
        val primary = createNPC("giant_rat", Tile(3105, 3929)) { it["in_multi_combat"] = true }
        val boss = NPCs.add("kolodion_human", Tile(3106, 3929), ticks = -1, owner = owner)
        boss["in_multi_combat"] = true
        tick(1)

        assertFalse(multiTargets(bystander, primary, 9).contains(boss))
        assertTrue(multiTargets(owner, primary, 9).contains(boss))
        NPCs.remove(boss)
    }

    @Test
    fun `Arena entry lever is locked until Kolodion is defeated`() {
        val player = createPlayer(Tile(3105, 3956), "mage-14")
        val lever = createObject("mage_arena_lever_in", Tile(3104, 3956))

        player.objectOption(lever, "Pull")
        tick(2)
        assertEquals(Tile(3105, 3956), player.tile)

        player["mage_arena"] = "completed"
        player.objectOption(lever, "Pull")
        tick(10)
        assertEquals(Tile(3105, 3951), player.tile)
    }

    private fun startFight(player: Player) {
        player.levels.set(Skill.Magic, 60)
        player["mage_arena"] = "started"
        player["kolodion_form"] = "human"
        val kolodion = createNPC("kolodion", player.tile.addX(1))
        player.npcOption(kolodion, "Talk-to")
        tick(1)
        player.dialogueContinue(2)
        tick(6)
        assertEquals(Tile(3104, 3933), player.tile)
    }

    private fun kill(player: Player) {
        val boss: NPC = NPCs.indexed(player["kolodion_index", -1])!!
        tickIf {
            // Hits are capped so multiple may be needed
            if (boss.levels.get(Skill.Constitution) > 0) {
                boss.directHit(player, boss.levels.get(Skill.Constitution))
                true
            } else {
                false
            }
        }
        tick(9)
    }
}
