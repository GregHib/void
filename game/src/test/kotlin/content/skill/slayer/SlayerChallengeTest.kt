package content.skill.slayer

import FakeRandom
import WorldTest
import content.entity.combat.damageDealers
import dialogueContinue
import dialogueOption
import interfaceOption
import itemOnNpc
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SlayerChallengeTest : WorldTest() {

    private fun challenger(master: String, challenge: String, tile: Tile = Tile(3200, 3200)): Player {
        val player = createPlayer(tile)
        player["smoking_kills"] = "completed"
        player.slayerMaster = master
        player.slayerChallenge = challenge
        player.slayerStreak = 9
        return player
    }

    private fun kill(player: Player, npc: NPC, damage: Int = 10) {
        npc.damageDealers[player] = damage
        Death.killed(npc)
    }

    @Test
    fun `Kill the Kalphite Queen for Sumona's challenge`() {
        val player = challenger("sumona", "kalphite_queen")
        val partner = createPlayer(Tile(3201, 3200))
        val queen = createNPC("kalphite_queen", Tile(3200, 3202))
        queen.damageDealers[partner] = 400

        kill(player, queen, damage = 600)

        assertEquals("", player.slayerChallenge)
        assertEquals(15_000.0, player.experience.get(Skill.Slayer))
        // 30 challenge points and the 10th task bonus
        assertEquals(30 + 60, player.slayerPoints)
        assertEquals(10, player.slayerStreak)
    }

    @Test
    fun `Kalphite Queen challenge needs the majority of the damage`() {
        val player = challenger("sumona", "kalphite_queen")
        val partner = createPlayer(Tile(3201, 3200))
        val queen = createNPC("kalphite_queen", Tile(3200, 3202))
        queen.damageDealers[partner] = 600

        kill(player, queen, damage = 400)

        assertEquals("kalphite_queen", player.slayerChallenge)
    }

    @Test
    fun `Clear the Slayer Tower for Duradel's challenge`() {
        val player = challenger("duradel", "slayer_tower", Tile(3428, 3538))
        val monsters = mapOf(
            "crawling_hand" to 21,
            "banshee" to 8,
            "infernal_mage" to 9,
            "bloodveld" to 7,
            "aberrant_spectre" to 7,
            "gargoyle" to 5,
            "nechryael" to 6,
            "abyssal_demon" to 6,
        )
        for ((id, count) in monsters) {
            val npc = createNPC(id, Tile(3429, 3538))
            repeat(count) {
                assertEquals("slayer_tower", player.slayerChallenge, id)
                kill(player, npc)
            }
        }

        assertEquals("", player.slayerChallenge)
        assertEquals(20_000.0, player.experience.get(Skill.Slayer))
    }

    @Test
    fun `Leaving the Slayer Tower resets Duradel's challenge`() {
        val player = challenger("duradel", "slayer_tower", Tile(3428, 3538))
        kill(player, createNPC("banshee", Tile(3429, 3538)))
        assertEquals(1, player["slayer_tower_banshees", 0])

        player.tele(3428, 3510)
        tick(2)

        assertEquals(0, player["slayer_tower_banshees", 0])
        assertEquals("slayer_tower", player.slayerChallenge)
    }

    @Test
    fun `Defeat TzTok-Jad for Kuradal's challenge`() {
        val player = challenger("kuradal", "fight_cave")
        player["fight_cave_wave"] = 63

        kill(player, createNPC("tztok_jad", Tile(3200, 3203)))

        assertEquals("", player.slayerChallenge)
        assertEquals(25_000.0, player.experience.get(Skill.Slayer))
    }

    @Test
    fun `Dragons drop perfect scales during Vannaka's challenge`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val player = challenger("vannaka", "dragon_scales")
        val dragon = createNPC("green_dragon", Tile(3200, 3203))

        kill(player, dragon)

        assertTrue(FloorItems.at(dragon.tile).any { it.id == "perfect_green_dragon_scale" })
    }

    @Test
    fun `Dragons don't drop perfect scales without the challenge`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val player = createPlayer(Tile(3200, 3200))
        val dragon = createNPC("green_dragon", Tile(3200, 3203))

        kill(player, dragon)

        assertFalse(FloorItems.at(dragon.tile).any { it.id == "perfect_green_dragon_scale" })
    }

    @Test
    fun `Hand perfect scales in to Vannaka`() {
        val player = challenger("vannaka", "dragon_scales", Tile(3146, 9914))
        player.inventory.add("perfect_green_dragon_scale", "perfect_blue_dragon_scale", "perfect_red_dragon_scale", "perfect_black_dragon_scale", "perfect_red_dragon_scale")
        val vannaka = createNPC("vannaka", Tile(3147, 9914))

        player.npcOption(vannaka, "Get-task")
        tick(2)
        player.dialogueContinue()

        assertEquals("", player.slayerChallenge)
        assertEquals(0, player.inventory.count("perfect_red_dragon_scale"))
        assertEquals(4_000.0, player.experience.get(Skill.Slayer))
    }

    @Test
    fun `Experimental spray on zygomites for Chaeldar's challenge`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = 0
        })
        val player = challenger("chaeldar", "zygomites", Tile(2400, 4430))
        player.inventory.add("experimental_spray")
        val zygomite = createNPC("zygomite", Tile(2400, 4431))
        zygomite.levels.set(Skill.Constitution, 50)

        player.itemOnNpc(zygomite, 0)
        tick(2)

        assertEquals(1, player["slayer_challenge_progress", 0])
        assertEquals(0, zygomite.levels.get(Skill.Constitution))
    }

    @Test
    fun `Experimental spray can heal a zygomite`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int): Int = until - 1
        })
        val player = challenger("chaeldar", "zygomites", Tile(2400, 4430))
        player.inventory.add("experimental_spray")
        val zygomite = createNPC("zygomite", Tile(2400, 4431))
        zygomite.levels.set(Skill.Constitution, 50)

        player.itemOnNpc(zygomite, 0)
        tick(2)

        assertEquals(zygomite.levels.getMax(Skill.Constitution), zygomite.levels.get(Skill.Constitution))
    }

    @Test
    fun `Report spray results to Chaeldar`() {
        val player = challenger("chaeldar", "zygomites", Tile(2445, 4431))
        player["slayer_challenge_progress"] = 10
        player.inventory.add("experimental_spray")
        val chaeldar = createNPC("chaeldar", Tile(2445, 4432))

        player.npcOption(chaeldar, "Get-task")
        tick(2)
        player.dialogueContinue()

        assertEquals("", player.slayerChallenge)
        assertEquals(0, player.inventory.count("experimental_spray"))
        assertEquals(10_000.0, player.experience.get(Skill.Slayer))
    }

    @Test
    fun `Contact Mazchna near the fairy rings to summon ravenous creatures`() {
        val player = challenger("mazchna", "ravenous_creatures", Tile(3448, 3471))
        player.inventory.add("enchanted_gem")

        player.itemOption("Activate", "enchanted_gem")
        tick()

        val ghoul = NPCs.first { it.id == "ravenous_ghoul" && it.tile.within(Tile(3447, 3470), 5) }
        kill(player, ghoul)
        kill(player, createNPC("ravenous_vampire", Tile(3448, 3472)))
        assertEquals("ravenous_creatures", player.slayerChallenge)
        kill(player, createNPC("ravenous_snail", Tile(3448, 3472)))

        assertEquals("", player.slayerChallenge)
        assertEquals(2_000.0, player.experience.get(Skill.Slayer))
    }

    @Test
    fun `Reassigning cancels a challenge`() {
        val player = challenger("duradel", "slayer_tower")
        player.slayerPoints = 30
        player.open("slayer_rewards_assignment")

        player.interfaceOption("slayer_rewards_assignment", "reassign_points", "Reassign Current")

        assertEquals("", player.slayerChallenge)
        assertEquals(0, player.slayerPoints)
    }

    @Test
    fun `Masters assign tasks instead of challenges when they don't offer one`() {
        val player = createPlayer(Tile(3511, 3510))
        for (skill in Skill.entries) {
            player.levels.set(skill, skill.maximum())
            player.experience.set(skill, Level.experience(skill, skill.maximum()))
        }
        val master = createNPC("mazchna_canifis", Tile(3511, 3509))

        player.npcOption(master, "Get-task")
        tick(2)

        assertEquals("", player.slayerChallenge)
        assertTrue(player.slayerTask != "nothing")
    }

    @Test
    fun `Accept a challenge offered by a master`() {
        Settings.load(mapOf("slayer.challengeChance" to "100"))
        try {
            val player = createPlayer(Tile(2869, 2983))
            for (skill in Skill.entries) {
                player.levels.set(skill, skill.maximum())
                player.experience.set(skill, Level.experience(skill, skill.maximum()))
            }
            player["shilo_village"] = "completed"
            val master = createNPC("duradel_shilo_village", Tile(2869, 2982))

            player.npcOption(master, "Get-task")
            tick(2)
            player.dialogueContinue()
            player.dialogueOption("line1") // Yes, I'll take the challenge.
            player.dialogueContinue()

            assertEquals("slayer_tower", player.slayerChallenge)
            assertEquals("duradel", player.slayerMaster)
            assertEquals("nothing", player.slayerTask)
        } finally {
            Settings.load(mapOf("slayer.challengeChance" to "0"))
        }
    }

    @Test
    fun `Decline a challenge for a normal task`() {
        Settings.load(mapOf("slayer.challengeChance" to "100"))
        try {
            val player = createPlayer(Tile(2869, 2983))
            for (skill in Skill.entries) {
                player.levels.set(skill, skill.maximum())
                player.experience.set(skill, Level.experience(skill, skill.maximum()))
            }
            player["shilo_village"] = "completed"
            val master = createNPC("duradel_shilo_village", Tile(2869, 2982))

            player.npcOption(master, "Get-task")
            tick(2)
            player.dialogueContinue()
            player.dialogueOption("line2") // No thanks, I'd rather have a normal assignment.
            player.dialogueContinue()

            assertEquals("", player.slayerChallenge)
            assertTrue(player.slayerTask != "nothing")
        } finally {
            Settings.load(mapOf("slayer.challengeChance" to "0"))
        }
    }

    @Test
    fun `Can't enter Turael's dungeon without his challenge`() {
        val player = createPlayer(Tile(2931, 3540))
        val ladder = GameObjects.find(Tile(2931, 3539), "turaels_dungeon_ladder_down")

        player.objectOption(ladder, "Climb-down")
        tick(3)

        assertTrue(player.tile.y < 4000)
    }

    @Test
    fun `Kill every monster in Turael's dungeon in one visit`() {
        val player = challenger("turael", "turaels_dungeon", Tile(2931, 3540))
        val ladder = GameObjects.find(Tile(2931, 3539), "turaels_dungeon_ladder_down")

        player.objectOption(ladder, "Climb-down")
        tick(5)

        assertEquals(Tile(3098, 4959), player.tile)
        val monsters = NPCs.filter { it["owner", ""] == player.accountName }
        assertEquals(15, monsters.size)
        for (monster in monsters) {
            assertEquals("turaels_dungeon", player.slayerChallenge)
            kill(player, monster)
        }

        assertEquals("", player.slayerChallenge)
        assertEquals(1_000.0, player.experience.get(Skill.Slayer))
    }

    @Test
    fun `Leaving Turael's dungeon resets his challenge`() {
        val player = challenger("turael", "turaels_dungeon", Tile(2931, 3540))
        player.objectOption(GameObjects.find(Tile(2931, 3539), "turaels_dungeon_ladder_down"), "Climb-down")
        tick(5)
        kill(player, NPCs.first { it["owner", ""] == player.accountName })

        player.objectOption(GameObjects.find(Tile(3097, 4959), "turaels_dungeon_ladder_up"), "Climb-up")
        tick(5)

        assertEquals(Tile(2931, 3538), player.tile)
        assertFalse(Collisions.check(player.tile, CollisionFlag.OBJECT or CollisionFlag.FLOOR))
        assertEquals(0, NPCs.count { it["owner", ""] == player.accountName })
        assertEquals("turaels_dungeon", player.slayerChallenge)
    }
}
