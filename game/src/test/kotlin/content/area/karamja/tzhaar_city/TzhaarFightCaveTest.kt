package content.area.karamja.tzhaar_city

import FakeRandom
import WorldTest
import content.entity.combat.hit.directHit
import intEntry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.test.get
import world.gregs.voidps.engine.client.instruction.handle.interactObject
import world.gregs.voidps.engine.data.AccountManager
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

class TzhaarFightCaveTest : WorldTest() {

    @Test
    fun `Complete all waves to earn fire cape`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 0
        })
        val player = createPlayer(Tile(2438, 5168), "JalYt-1")
        player["auto_retaliate"] = true
        player["god_mode"] = true
        player["insta_kill"] = true

        val entrance = objects[Tile(2437, 5166), "cave_entrance_fight_cave"]!!
        player.interactObject(entrance, "Enter")
        tick(5)

        for (i in 2 until 64) {
            killAll(player)
            killKets(player)
            assertEquals(i, player["fight_cave_wave", 0])
        }
        killAll(player) // Jad
        tick(4)

        assertEquals(Tile(2436, 5170), player.tile)
        assertNull(player.get<Int>("fight_cave_wave"))
        assertNull(player.get<Int>("instance"))
        assertEquals(1, player.inventory.count("fire_cape"))
        assertEquals(8032, player.inventory.count("tokkul"))
    }

    @Test
    fun `Jad spawns healers after half hp`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 0
        })
        val player = createPlayer(Tile(2438, 5168), "JalYt-2")
        player["god_mode"] = true
        player["insta_kill"] = true
        player.rights = PlayerRights.Admin
        player.inventory.add("dragon_scimitar")
        val entrance = objects[Tile(2437, 5166), "cave_entrance_fight_cave"]!!
        player.interactObject(entrance, "Enter")
        tick(2)
        player.intEntry(63)
        tick(5)
        val jad = NPCs[player.tile.regionLevel].first { it.id == "tztok_jad" }
        jad.directHit(player, 1 + (jad.levels.getMax(Skill.Constitution) / 2))
        tick(3)
        assertEquals(4, NPCs[player.tile.regionLevel].count { it.id == "yt_hur_kot" })
    }

    private fun killAll(player: Player) {
        val current = NPCs[player.tile.regionLevel].toList()
        for (npc in current) {
            npc.directHit(player, npc.levels.get(Skill.Constitution))
        }
        tick(7)
    }

    private fun killKets(player: Player) {
        val current = NPCs[player.tile.regionLevel].toList()
        var killed = false
        for (npc in current) {
            if (npc.id == "tz_kek_spawn") {
                killed = true
                npc.directHit(player, npc.levels.get(Skill.Constitution))
            }
        }
        if (killed) {
            tick(7)
        }
    }

    @Test
    fun `Logout during a wave waits until end`() = runTest {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 0
        })
        val player = createPlayer(Tile(2438, 5168), "JalYt-1")
        player["god_mode"] = true
        val entrance = objects[Tile(2437, 5166), "cave_entrance_fight_cave"]!!
        player.interactObject(entrance, "Enter")
        tick(5)
        val manager = get<AccountManager>()
        manager.logout(player, true)
        assertTrue(player["fight_caves_logout_warning", false])
        tick()
        killAll(player)
        tick(3)
        assertTrue(player["logged_out", false])
        assertEquals(Tile(2413, 5105), player.tile)
    }

    @Test
    fun `Double pressing logout works immediately`() = runTest {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 0
        })
        val player = createPlayer(Tile(2438, 5168), "JalYt-2")
        player["god_mode"] = true
        val entrance = objects[Tile(2437, 5166), "cave_entrance_fight_cave"]!!
        player.interactObject(entrance, "Enter")
        tick(5)
        val manager = get<AccountManager>()
        manager.logout(player, true)
        assertTrue(player["fight_caves_logout_warning", false])
        manager.logout(player, true)
        tick()
        assertEquals(Tile(2413, 5113), player.tile)
    }

    @Test
    fun `Logging back in restarts the wave`() {
        val tile = Tile(2417, 5089)
        val delta = tile.delta(tile.region.tile)
        val player = createPlayer(tile, "JalYt-3")
        player["fight_cave_wave"] = 10

        Spawn.player(player)

        assertNotEquals(tile, player.tile)
        assertTrue(player.interfaces.contains("tzhaar_fight_cave"))
        assertEquals(delta, player.tile.delta(player.tile.region.tile))
    }

    @Test
    fun `Death in fight cave leaves and gives rewards`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 0
        })
        val player = createPlayer(Tile(2438, 5168), "JalYt-4")
        player.inventory.add("dragon_scimitar")
        val entrance = objects[Tile(2437, 5166), "cave_entrance_fight_cave"]!!
        player.interactObject(entrance, "Enter")
        tick(5)
        player["fight_cave_wave"] = 10
        tick(2)
        player.directHit(1000)
        tick(4)

        assertEquals(Tile(2436, 5170), player.tile)
        assertNull(player.get<Int>("fight_cave_wave"))
        assertNull(player.get<Int>("instance"))
        assertEquals(1, player.inventory.count("dragon_scimitar"))
    }

    @Test
    fun `Teleporting out of fight cave gives rewards`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int): Int = 0
        })
        val player = createPlayer(Tile(2438, 5168), "JalYt-5")
        val entrance = objects[Tile(2437, 5166), "cave_entrance_fight_cave"]!!
        player.interactObject(entrance, "Enter")
        tick(5)
        player.tele(3221, 3219)
        tick(2)
        assertNull(player.get<Int>("fight_cave_wave"))
        assertNull(player.get<Int>("instance"))
        assertFalse(player.interfaces.contains("tzhaar_fight_cave"))
    }
}
