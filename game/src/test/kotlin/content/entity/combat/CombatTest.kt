package content.entity.combat

import FakeRandom
import WorldTest
import content.entity.player.effect.skull
import equipItem
import interfaceOption
import kotlinx.coroutines.test.runTest
import npcOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import playerOption
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

internal class CombatTest : WorldTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
            override fun nextInt(from: Int, until: Int): Int {
                if (until == 128 || until == 1) { // Drops
                    return from
                }
                return until
            }
        })
    }

    @Test
    fun `Kill rat with magic`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))
        player.equipment.set(EquipSlot.Weapon.index, "staff_of_air")
        player.experience.set(Skill.Magic, EXPERIENCE)
        player.inventory.add("mind_rune", 100)

        player.interfaceOption("modern_spellbook", "wind_strike", option = "Autocast")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(6) // npc death

        assertEquals(emptyTile, player.tile)
        assertTrue(player.experience.get(Skill.Magic) > EXPERIENCE)
        assertTrue(floorItems[tile].any { it.id == "bones" })
        assertTrue(player.inventory.count("mind_rune") < 100)
    }

    @Test
    fun `Kill rat with melee`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, EXPERIENCE)
        player.experience.set(Skill.Strength, EXPERIENCE)
        player.experience.set(Skill.Defence, EXPERIENCE)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.interfaceOption("combat_styles", "style3", "Select")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(7) // npc death

        assertNotEquals(emptyTile, player.tile)
        assertTrue(player.experience.get(Skill.Attack) > EXPERIENCE)
        assertTrue(player.experience.get(Skill.Strength) > EXPERIENCE)
        assertTrue(player.experience.get(Skill.Defence) > EXPERIENCE)
        println(floorItems[tile.zone])
        assertTrue(floorItems[tile].any { it.id == "bones" })
    }

    @Test
    fun `Kill rat with range`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int): Int = until / 2

            override fun nextBits(bitCount: Int) = 100
        })
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))

        player.levels.set(Skill.Ranged, 50)
        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Ranged, EXPERIENCE)
        player.experience.set(Skill.Defence, EXPERIENCE)
        player.levels.boost(Skill.Ranged, 25)

        player.interfaceOption("combat_styles", "style3", "Select")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        val tile = npc["death_tile", npc.tile]
        tick(6) // npc death

        val drops = floorItems[tile]
        assertEquals(emptyTile, player.tile)
        assertTrue(player.equipment[EquipSlot.Ammo.index].amount < 100)
        assertTrue(drops.any { it.id == "bones" })
        assertTrue(floorItems[emptyTile.addY(4)].any { it.id == "rune_arrow" })
        assertTrue(player.experience.get(Skill.Ranged) > EXPERIENCE)
        assertTrue(player.experience.get(Skill.Defence) > EXPERIENCE)
        assertTrue(player.inventory.count("rune_arrow") < 100)
    }

    @Test
    fun `Dragon dagger special attack`() {
        var hits = 0
        object : Script {
            init {
                npcCombatDamage {
                    hits++
                }
            }
        }
        val player = createPlayer(emptyTile)
        player.experience.set(Skill.Attack, EXPERIENCE)
        player.levels.set(Skill.Attack, 99)
        val npc = createNPC("rat", emptyTile.addY(1))

        player.inventory.add("dragon_dagger")
        player.equipItem("dragon_dagger")

        player.interfaceOption("combat_styles", "special_attack_bar", "Use")
        player.npcOption(npc, "Attack")
        tick(2)

        assertEquals(2, hits)
    }

    @Test
    fun `Don't take damage with protection prayers`() {
        setRandom(object : FakeRandom() {
            override fun nextBits(bitCount: Int) = 100
        })
        val player = createPlayer(emptyTile)
        player.experience.set(Skill.Constitution, EXPERIENCE)
        player.experience.set(Skill.Prayer, EXPERIENCE)
        player.levels.set(Skill.Constitution, 990)
        player.levels.set(Skill.Prayer, 99)
        val npc = createNPC("rat", emptyTile.addY(1))
        npc.levels.link(
            npc,
            object : Levels.Level {
                override fun getMaxLevel(skill: Skill): Int = if (skill == Skill.Constitution) 10000 else 1
            },
        )
        npc.levels.clear()

        player.interfaceOption("prayer_list", "regular_prayers", "Activate", slot = 19, optionIndex = 0)
        player.npcOption(npc, "Attack")
        tick(4)

        assertEquals(990, player.levels.get(Skill.Constitution))
        assertNotEquals(0, player["protected_damage", 0])
    }

    @Test
    fun `Ranged attacks will run within distance and stop`() {
        val player = createPlayer(Tile(3228, 3415))
        val npc = createNPC("rat", Tile(3228, 3407))

        player.equipment.set(EquipSlot.Weapon.index, "magic_shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 100)
        player.experience.set(Skill.Ranged, EXPERIENCE)
        player.levels.set(Skill.Ranged, 50)
        player.levels.boost(Skill.Ranged, 25)

        player.interfaceOption("combat_styles", "style1", "Select")
        player.npcOption(npc, "Attack")
        tickIf { npc.levels.get(Skill.Constitution) > 0 }
        tick(5) // npc death

        assertEquals(Tile(3228, 3414), player.tile)
    }

    @Test
    fun `Kill player with melee`() {
        val startTile = Tile(3143, 3553)
        val targetTile = startTile.addX(3)
        val player = createPlayer(startTile)
        val target = createPlayer(targetTile)

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, EXPERIENCE)
        player.experience.set(Skill.Strength, EXPERIENCE)
        player.experience.set(Skill.Defence, EXPERIENCE)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)
        target.appearance.combatLevel = 90
        target.inventory.add("dragon_longsword", 1)
        target.inventory.add("magic_shortbow", 1)
        target.inventory.add("rune_arrow", 10)
        target.inventory.add("plague_jacket", 1)
        target.skull()

        player.interfaceOption("combat_styles", "style3", "Select")
        player.playerOption(target, "Attack")
        tickIf { target.levels.get(Skill.Constitution) > 0 }
        tick(7) // player death

        assertNotEquals(startTile, player.tile)
        assertTrue(player.experience.get(Skill.Attack) > EXPERIENCE)
        assertTrue(player.experience.get(Skill.Strength) > EXPERIENCE)
        assertTrue(player.experience.get(Skill.Defence) > EXPERIENCE)
        val items = floorItems[targetTile]
        assertTrue(items.any { it.id == "coins" })
        assertTrue(items.any { it.id == "rune_arrow" && it.amount > 1 })
        assertTrue(items.any { it.id == "bones" })
    }

    @Test
    fun `Can't attack players`() {
        val player = createPlayer(emptyTile)
        val target = createPlayer(emptyTile.addY(4))

        player.equipment.set(EquipSlot.Weapon.index, "dragon_longsword")
        player.experience.set(Skill.Attack, EXPERIENCE)
        player.experience.set(Skill.Strength, EXPERIENCE)
        player.experience.set(Skill.Defence, EXPERIENCE)
        player.levels.boost(Skill.Attack, 25)
        player.levels.boost(Skill.Strength, 25)

        player.interfaceOption("combat_styles", "style3", "Select")
        runTest {
            player.instructions.send(InteractPlayer(target.index, 1))
        }
        tick(2)

        assertEquals(emptyTile, player.tile)
        assertEquals(EXPERIENCE, player.experience.get(Skill.Attack))
        assertEquals(EXPERIENCE, player.experience.get(Skill.Strength))
        assertEquals(EXPERIENCE, player.experience.get(Skill.Defence))
    }

    companion object {
        private const val EXPERIENCE = 14000000.0
    }
}
