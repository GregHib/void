package content.skill.summoning

import FakeRandom
import WorldTest
import content.entity.combat.target
import content.entity.effect.frozen
import content.entity.effect.stunned
import content.entity.effect.toxin.poisoned
import content.entity.player.combat.special.specialAttackEnergy
import interfaceOption
import itemOnNpc
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Covers the combat-flavoured familiar specials by invoking the registered blocks directly - the
 * scroll/points gate itself is covered by [FamiliarSpecialMoveTest].
 */
class FamiliarCombatSpecialEffectTest : WorldTest() {

    private fun summon(familiar: String, tile: Tile = Tile(2523, 3056)): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get(familiar), restart = false)
        tick(2) // let the summon queue assign the follower
        player.set("summoning_special_points_remaining", 60)
        return player
    }

    private fun Player.runNpcSpecial(familiar: String, target: NPC): Boolean = FamiliarSpecialMoves.npcTarget.getValue(familiar).invoke(this, target)

    private fun Player.runPlayerSpecial(familiar: String, target: Player): Boolean = FamiliarSpecialMoves.playerTarget.getValue(familiar).invoke(this, target)

    /** Max out damage rolls so every hit is observable. */
    private fun maxRolls() = setRandom(object : FakeRandom() {
        override fun nextInt(until: Int) = until - 1
    })

    private fun tankyRat(player: Player, tile: Tile = player.tile.addY(4)): NPC {
        val rat = createNPC("giant_rat", tile)
        rat.levels.set(Skill.Constitution, 2000)
        return rat
    }

    /** The giant rat is 2x2 - size-gated effects (stun, bind) need this 1x1 rat instead. */
    private fun smallRat(player: Player, tile: Tile = player.tile.addY(4)): NPC {
        val rat = createNPC("rat", tile)
        rat.levels.set(Skill.Constitution, 2000)
        return rat
    }

    @Test
    fun `Doomsphere damages the target and washes away some of its Magic`() {
        maxRolls()
        val player = summon("karamthulhu_overlord_familiar")
        val target = tankyRat(player)
        target.levels.set(Skill.Magic, 20)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("karamthulhu_overlord_familiar", target))
        assertTrue(target.levels.get(Skill.Magic) < 20, "Doomsphere lowers the target's Magic")

        tick(6)
        assertTrue(target.levels.get(Skill.Constitution) < before, "Doomsphere damaged the target")
    }

    @Test
    fun `Spike Shot lands a heavy single hit and stuns on impact`() {
        maxRolls()
        val player = summon("spirit_dagannoth_familiar")
        val target = tankyRat(player)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("spirit_dagannoth_familiar", target))

        var sawStun = false
        repeat(8) {
            tick()
            sawStun = sawStun || target.stunned
        }
        assertTrue(before - target.levels.get(Skill.Constitution) >= 170, "the maxed spike hits for 170")
        assertTrue(sawStun, "the spike stuns the target as it lands")
    }

    @Test
    fun `Swamp Plague poisons the target as it lands`() {
        maxRolls()
        val player = summon("swamp_titan_familiar")
        val target = tankyRat(player)

        assertTrue(player.runNpcSpecial("swamp_titan_familiar", target))
        tick(6)

        assertTrue(target.poisoned, "the swamp titan's plague poisons")
    }

    @Test
    fun `Ebon Thunder drains a player target's special attack energy on impact`() {
        maxRolls()
        val player = summon("lava_titan_familiar")
        val target = createPlayer(player.tile.addY(3))
        target.levels.set(Skill.Constitution, 2000)
        player.set("in_pvp", true)
        target.set("in_pvp", true)
        assertEquals(1000, target.specialAttackEnergy)

        assertTrue(player.runPlayerSpecial("lava_titan_familiar", target))
        tick(8)

        assertEquals(900, target.specialAttackEnergy, "the bolt saps a tenth of the special energy")
    }

    @Test
    fun `Mantis Strike binds a small target in place without stunning it`() {
        maxRolls()
        val player = summon("praying_mantis_familiar")
        val target = smallRat(player)

        assertTrue(player.runNpcSpecial("praying_mantis_familiar", target))

        var bound = false
        repeat(8) {
            tick()
            bound = bound || target.frozen
            assertFalse(target.stunned, "the mantis binds movement, it never stuns")
        }
        assertTrue(bound, "the strike froze the target once it landed")
    }

    @Test
    fun `Deadly Claw rakes the target three times`() {
        maxRolls()
        val player = summon("talon_beast_familiar")
        val target = tankyRat(player)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("talon_beast_familiar", target))
        tick(8)

        // A single maxed swipe is 100 - anything above 200 proves all three landed.
        assertTrue(before - target.levels.get(Skill.Constitution) >= 300, "three raking hits landed")
    }

    @Test
    fun `Acorn Missile pelts targets adjacent to the one aimed at`() {
        maxRolls()
        val player = summon("giant_ent_familiar")
        val target = tankyRat(player)
        val splashed = tankyRat(player, target.tile.addY(1))
        val before = splashed.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("giant_ent_familiar", target))
        tick(8)

        assertTrue(splashed.levels.get(Skill.Constitution) < before, "the acorns splash the adjacent npc")
    }

    @Test
    fun `Famine works on players in the wilderness, not just PvP areas`() {
        val player = summon("ravenous_locust_familiar")
        val target = createPlayer(player.tile.addY(3))
        target.levels.set(Skill.Constitution, 2000)
        target.inventory.transaction { add("shark", 1) }
        // The wilderness flags players with in_wilderness, not in_pvp - the special must accept both.
        player.set("in_wilderness", true)
        target.set("in_wilderness", true)
        // Within each other's wilderness combat-level bracket.
        player.combatLevel = 100
        target.combatLevel = 100

        assertTrue(player.runPlayerSpecial("ravenous_locust_familiar", target), "the swarm flies in the wilderness too")
        assertEquals(1, target.inventory.count("rotten_food"))
    }

    @Test
    fun `Famine turns a player target's food rotten`() {
        maxRolls()
        val player = summon("ravenous_locust_familiar")
        val target = createPlayer(player.tile.addY(3))
        target.levels.set(Skill.Constitution, 2000)
        target.inventory.transaction { add("shark", 1) }
        player.set("in_pvp", true)
        target.set("in_pvp", true)

        assertTrue(player.runPlayerSpecial("ravenous_locust_familiar", target))

        assertEquals(0, target.inventory.count("shark"), "the shark was devoured")
        assertEquals(1, target.inventory.count("rotten_food"), "leaving rotten food behind")
    }

    @Test
    fun `Inferno hits and burns another player's weapon and shield out of their hands`() {
        maxRolls()
        val player = summon("forge_regent_familiar")
        val target = createPlayer(player.tile.addY(3))
        target.levels.set(Skill.Constitution, 2000)
        target.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        target.equipment.set(EquipSlot.Shield.index, "wooden_shield")
        player.set("in_pvp", true)
        target.set("in_pvp", true)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runPlayerSpecial("forge_regent_familiar", target))

        assertTrue(target.equipment[EquipSlot.Weapon.index].isEmpty(), "the weapon is knocked loose")
        assertTrue(target.equipment[EquipSlot.Shield.index].isEmpty(), "the shield is knocked loose")
        assertEquals(1, target.inventory.count("bronze_sword"))
        assertEquals(1, target.inventory.count("wooden_shield"))

        tick(6)
        assertTrue(target.levels.get(Skill.Constitution) < before, "the fiery bolt also damages")
    }

    @Test
    fun `Inferno still casts against a player with nothing to disarm`() {
        maxRolls()
        val player = summon("forge_regent_familiar")
        val target = createPlayer(player.tile.addY(3))
        target.levels.set(Skill.Constitution, 2000)
        player.set("in_pvp", true)
        target.set("in_pvp", true)

        assertTrue(player.runPlayerSpecial("forge_regent_familiar", target), "the bolt flies regardless")
    }

    @Test
    fun `A plain Dust Cloud cast chokes the foes around the smoke devil`() {
        maxRolls()
        val player = summon("smoke_devil_familiar")
        val familiar = player.follower!!
        val target = tankyRat(player, familiar.tile.addX(1))
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runSpecial("smoke_devil_familiar"))
        tick(8)

        assertTrue(target.levels.get(Skill.Constitution) < before, "the adjacent rat chokes on the dust")
    }

    @Test
    fun `Dust Cloud finds a large npc standing beside the devil`() {
        maxRolls()
        val player = summon("smoke_devil_familiar")
        val familiar = player.follower!!
        // The giant rat is 2x2: anchored two tiles west, its body still touches the devil - the
        // scan must match npc bounds, not just anchor tiles.
        val target = tankyRat(player, familiar.tile.addX(-2))
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runSpecial("smoke_devil_familiar"))
        tick(8)

        assertTrue(target.levels.get(Skill.Constitution) < before, "the big rat chokes on the dust")
    }

    @Test
    fun `Dust Cloud chokes the target and the foes around it`() {
        maxRolls()
        val player = summon("smoke_devil_familiar")
        val target = tankyRat(player)
        val other = tankyRat(player, target.tile.addX(1))
        val before = target.levels.get(Skill.Constitution)
        val otherBefore = other.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("smoke_devil_familiar", target))
        tick(8)

        assertTrue(target.levels.get(Skill.Constitution) < before, "the picked target chokes on the dust")
        assertTrue(other.levels.get(Skill.Constitution) < otherBefore, "so does its neighbour")
    }

    @Test
    fun `Iron Within batters the target with three magic bolts from afar`() {
        maxRolls()
        val player = summon("iron_titan_familiar")
        val target = tankyRat(player)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("iron_titan_familiar", target))
        tick(8)

        // A single maxed magic bolt is 220 - beyond 440 proves all three landed.
        assertTrue(before - target.levels.get(Skill.Constitution) >= 600, "three bolts of up to 220 landed")
    }

    @Test
    fun `A plain click on the cast button fires Iron Within at the familiar's foe`() {
        maxRolls()
        val player = summon("iron_titan_familiar")
        player.inventory.transaction { add("iron_within_scroll", 1) }
        val target = tankyRat(player)
        player.follower!!.target = target // the titan is mid-fight
        val before = target.levels.get(Skill.Constitution)

        player.interfaceOption("summoning_orb", "cast_iron_within", "Cast")
        tick(8)

        assertEquals(0, player.inventory.count("iron_within_scroll"), "one scroll spent")
        assertTrue(before - target.levels.get(Skill.Constitution) >= 600, "the volley fired at its current target")
    }

    @Test
    fun `Steel of Legends strikes the target four times from afar`() {
        maxRolls()
        val player = summon("steel_titan_familiar")
        val target = tankyRat(player)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("steel_titan_familiar", target))
        tick(8)

        assertTrue(before - target.levels.get(Skill.Constitution) >= 900, "four ranged strikes of up to 244 landed")
    }

    @Test
    fun `Goad gores the target twice on the spot`() {
        maxRolls()
        val player = summon("spirit_graahk_familiar")
        val target = tankyRat(player)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(FamiliarSpecialMoves.npcTarget.getValue("spirit_graahk_familiar").invoke(player, target))
        tick(4)

        assertTrue(before - target.levels.get(Skill.Constitution) >= 240, "both 120-max gores landed")
    }

    @Test
    fun `Ambush pounces the kyatt onto its target for one heavy strike`() {
        maxRolls()
        val player = summon("spirit_kyatt_familiar")
        val target = tankyRat(player, player.tile.addY(6))
        val before = target.levels.get(Skill.Constitution)

        assertTrue(FamiliarSpecialMoves.npcTarget.getValue("spirit_kyatt_familiar").invoke(player, target))
        // Measured from the target to the kyatt's bounds - its 2x2 body lands flush against the prey.
        assertTrue(target.tile.distanceTo(player.follower!!) <= 1, "the kyatt lands beside its prey")

        tick(4)
        assertTrue(before - target.levels.get(Skill.Constitution) >= 200, "the maxed pounce hits close to 224")
    }

    @Test
    fun `Toad Bark refuses to fire while the toad is unloaded`() {
        val player = summon("barker_toad_familiar")
        val target = tankyRat(player)

        assertFalse(player.runNpcSpecial("barker_toad_familiar", target), "no cannonball - no shot, nothing charged")
    }

    @Test
    fun `A loaded toad barks its cannonball at the target and unloads`() {
        maxRolls()
        val player = summon("barker_toad_familiar")
        val toad = player.follower!!
        toad["cannonball_loaded"] = true
        val target = tankyRat(player)
        val before = target.levels.get(Skill.Constitution)

        assertTrue(player.runNpcSpecial("barker_toad_familiar", target))
        tick(10)

        assertFalse(toad["cannonball_loaded", false], "the shot spends the loaded cannonball")
        // A maxed roll is 300, well beyond the old 80 bark; damage modifiers may shave a little off.
        assertTrue(before - target.levels.get(Skill.Constitution) >= 250, "the maxed cannonball hits close to 300")
    }

    @Test
    fun `Loading a cannonball arms the toad`() {
        val player = summon("barker_toad_familiar")
        player.inventory.transaction { add("cannonball", 1) }

        player.itemOnNpc(player.follower!!, 0)
        tick(2)

        assertTrue(player.follower!!["cannonball_loaded", false], "the toad is armed")
        assertEquals(0, player.inventory.count("cannonball"), "the cannonball is swallowed")
    }

    @Test
    fun `Arctic Blast can stun a small target once the shot lands`() {
        // Max the damage roll but win the 1-in-5 stun roll (nextInt(5) == 0).
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 5) 0 else until - 1
        })
        val player = summon("arctic_bear_familiar")
        val target = smallRat(player)

        assertTrue(player.runNpcSpecial("arctic_bear_familiar", target))

        var sawStun = false
        repeat(8) {
            tick()
            sawStun = sawStun || target.stunned
        }
        assertTrue(sawStun, "the blast stunned the rat on impact")
    }

    @Test
    fun `Arctic Blast does not always stun`() {
        // Lose the 1-in-5 stun roll - the blast still hits but leaves no stun.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 5) 1 else until - 1
        })
        val player = summon("arctic_bear_familiar")
        val target = smallRat(player)

        assertTrue(player.runNpcSpecial("arctic_bear_familiar", target))

        repeat(8) {
            tick()
            assertFalse(target.stunned, "a lost roll leaves the target free to act")
        }
    }

    @Test
    fun `Rise from the Ashes reincarnates the phoenix atop the ashes, scorching foes beside them`() {
        maxRolls()
        val player = summon("phoenix_familiar")
        val phoenix = player.follower!!
        phoenix.levels.drain(Skill.Constitution, 800) // wounded, so the rebirth flare has fury
        val ashes = FloorItems.add(player.tile.addY(3), "ashes", disappearTicks = 300, owner = player)
        val target = tankyRat(player, ashes.tile.addX(1))
        val before = target.levels.get(Skill.Constitution)

        assertTrue(FamiliarSpecialMoves.floorItemTarget.getValue("phoenix_familiar").invoke(player, ashes))
        tick(8) // the drop-and-glow, the rebirth, then the flare's magic hits landing

        assertEquals(ashes.tile, player.follower!!.tile, "the phoenix is reborn atop the ashes")
        assertEquals(phoenix.levels.getMax(Skill.Constitution), phoenix.levels.get(Skill.Constitution), "at full life points")
        assertTrue(FloorItems.at(ashes.tile).none { it.id == "ashes" }, "the ashes burn away")
        assertTrue(target.levels.get(Skill.Constitution) < before, "the rebirth flare scorched the foe beside the ashes")
    }

    @Test
    fun `Rise from the Ashes refuses anything that is not ashes`() {
        val player = summon("phoenix_familiar")
        val bones = FloorItems.add(player.tile.addY(2), "bones", disappearTicks = 300, owner = player)

        assertFalse(FamiliarSpecialMoves.floorItemTarget.getValue("phoenix_familiar").invoke(player, bones))
    }

    @Test
    fun `Crushing Claw drains a twentieth of the target's Defence`() {
        maxRolls()
        val player = summon("granite_lobster_familiar")
        val target = tankyRat(player)
        target.levels.set(Skill.Defence, 20)

        assertTrue(player.runNpcSpecial("granite_lobster_familiar", target))

        assertTrue(target.levels.get(Skill.Defence) < 20, "Crushing Claw chips the target's Defence")
    }

    private fun Player.runSpecial(familiar: String): Boolean = FamiliarSpecialMoves.instant.getValue(familiar).invoke(this)
}
