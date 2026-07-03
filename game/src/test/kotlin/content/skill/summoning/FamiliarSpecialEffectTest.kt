package content.skill.summoning

import FakeRandom
import WorldTest
import content.entity.combat.hit.hit
import content.entity.effect.toxin.poisoned
import content.skill.fletching.fletchLog
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Covers the effects of the four newly-ported familiar specials (Spirit spider Egg Spawn, Spirit
 * scorpion Venom Shot, Spirit Tz-Kih Fireball Assault, Spirit Kalphite Sandstorm) by invoking the
 * registered blocks directly - the scroll/points gate itself is covered by [FamiliarSpecialMoveTest].
 */
class FamiliarSpecialEffectTest : WorldTest() {

    private fun summon(familiar: String, tile: Tile = Tile(2523, 3056)): Player {
        val player = createPlayer(tile)
        player.levels.set(Skill.Summoning, 99)
        player.summonFamiliar(NPCDefinitions.get(familiar), restart = false)
        tick(2) // let the summon queue assign the follower
        player.set("summoning_special_points_remaining", 60)
        return player
    }

    private fun Player.runSpecial(familiar: String): Boolean = FamiliarSpecialMoves.instant.getValue(familiar).invoke(this)

    private fun Player.runObjectSpecial(familiar: String, obj: GameObject): Boolean = FamiliarSpecialMoves.objectTarget.getValue(familiar).invoke(this, obj)

    @Test
    fun `Multichop chops the targeted tree for a log, possibly a lower tier`() {
        // Force the log roll to index 0 (the lowest tier) - proving a yew tree can hand over plain logs.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = summon("beaver_familiar")
        val tree = createObject("yew", player.tile.addX(2))

        val cast = player.runObjectSpecial("beaver_familiar", tree)
        assertTrue(cast)
        assertTrue(player.follower!!.get("chopping_logs", false), "the beaver is busy while chopping")

        tick(6) // walk to the tree and start chopping
        assertEquals(-1, player.follower!!.visuals.watch.index, "the beaver faces the tree, not the owner, while chopping")

        tick(10) // finish the chop and its animation

        assertEquals(1, player.beastOfBurden.count("logs"), "the beaver stashed a (lower-tier) log in its pack")
        assertFalse(player.follower!!.get("chopping_logs", false), "the busy flag clears once it's done")
    }

    @Test
    fun `Multichop does nothing when the target is not a tree`() {
        val player = summon("beaver_familiar")
        val notATree = createObject("bank_booth", player.tile.addX(1))

        assertFalse(player.runObjectSpecial("beaver_familiar", notATree))
        assertEquals(0, player.inventory.count("logs"), "no log handed over for a non-tree")
    }

    @Test
    fun `Using a log on the beaver routes to fletching, not beast-of-burden storage`() {
        // The fletch handler registers on the specific "<log>:beaver_familiar" operate key so it beats
        // the beast-of-burden store handler's "*:beaver_familiar" (invoke picks item:npc before *:npc),
        // otherwise a log gets refused with "your familiar won't carry yours" instead of fletching.
        assertTrue(world.gregs.voidps.engine.entity.Operation.itemOnNpc.containsKey("logs:beaver_familiar"))
    }

    @Test
    fun `Beaver fletches a log into a bow, no knife, while it is cutting`() {
        val player = summon("beaver_familiar")
        player.experience.set(Skill.Fletching, 14_000_000.0)
        player.follower!!["chopping_logs"] = true
        player.inventory.transaction { add("logs", 1) }
        val unf = Rows.getOrNull("fletching_unf.shortbow_u")!!

        player.fletchLog("shortbow_u", unf, "logs", 1, animate = {}, hasTool = { follower?.get("chopping_logs", false) == true })
        tick(4) // let the fletch queue produce the bow

        assertEquals(1, player.inventory.count("shortbow_u"), "the beaver acts as a knife while cutting")
        assertEquals(0, player.inventory.count("logs"), "consumed the log")
    }

    @Test
    fun `Beaver won't fletch when it isn't cutting and there is no knife`() {
        val player = summon("beaver_familiar")
        player.experience.set(Skill.Fletching, 14_000_000.0)
        player.inventory.transaction { add("logs", 1) }
        val unf = Rows.getOrNull("fletching_unf.shortbow_u")!!

        // Not cutting and no knife -> the tool check fails and nothing is made.
        player.fletchLog("shortbow_u", unf, "logs", 1, animate = {}, hasTool = { inventory.contains("knife") || follower?.get("chopping_logs", false) == true })
        tick(4)

        assertEquals(0, player.inventory.count("shortbow_u"), "no knife and not cutting - nothing made")
        assertEquals(1, player.inventory.count("logs"), "the log is untouched")
    }

    @Test
    fun `Summoning orb Cast option fires the special through the dispatcher`() {
        val player = summon("spirit_spider_familiar")
        player.inventory.transaction { add("egg_spawn_scroll", 2) }

        // The minimap orb's right-click "Cast <special>" (one cast_<special> component per move, each
        // with a "Cast" option) goes through the shared cast dispatcher.
        player.interfaceOption("summoning_orb", "cast_egg_spawn", "Cast")
        tick(2) // eggs drop a tick after the cast, then the floor-item queue flushes the tick after

        assertEquals(1, player.inventory.count("egg_spawn_scroll"), "one scroll spent")
        val eggs = (-1..1).flatMap { dx ->
            (-1..1).flatMap { dy -> FloorItems.at(player.tile.add(dx, dy)).filter { it.id == "red_spiders_eggs" } }
        }
        assertTrue(eggs.isNotEmpty())
    }

    @Test
    fun `Egg Spawn drops red spider eggs on free tiles around the player`() {
        val player = summon("spirit_spider_familiar")

        val cast = player.runSpecial("spirit_spider_familiar")
        tick(2) // eggs drop a tick after the cast, then the floor-item queue flushes the tick after

        assertTrue(cast)
        // Eggs scatter across free tiles in the 3x3 centred on the player.
        val eggs = (-1..1).flatMap { dx ->
            (-1..1).flatMap { dy -> FloorItems.at(player.tile.add(dx, dy)).filter { it.id == "red_spiders_eggs" } }
        }
        assertTrue(eggs.isNotEmpty())
    }

    @Test
    fun `Venom Shot charges the next attack and blocks a second charge`() {
        val player = summon("spirit_scorpion_familiar")

        assertTrue(player.runSpecial("spirit_scorpion_familiar"))
        assertTrue(player.get("familiar_venom_shot_charged", false))

        // Already charged - the second cast does nothing (no scroll should be spent).
        assertFalse(player.runSpecial("spirit_scorpion_familiar"))
    }

    @Test
    fun `Venom Shot poisons the target of the next ranged hit then clears`() {
        val player = summon("spirit_scorpion_familiar")
        val target = createNPC("giant_rat", player.tile.addY(4))
        player.set("familiar_venom_shot_charged", true)

        player.hit(target, offensiveType = "range", damage = 5)

        assertTrue(target.poisoned)
        assertFalse(player.get("familiar_venom_shot_charged", false))

        // The poison must actually deal damage over time, not just set the flag (a value <= ~10 is
        // cured before it ever lands a hit).
        val before = target.levels.get(Skill.Constitution)
        tick(60)
        assertTrue(target.levels.get(Skill.Constitution) < before, "poison ticked damage onto the target")
    }

    @Test
    fun `Venom Shot is not consumed by a melee hit`() {
        val player = summon("spirit_scorpion_familiar")
        val target = createNPC("giant_rat", player.tile.addY(4))
        player.set("familiar_venom_shot_charged", true)

        player.hit(target, offensiveType = "melee", damage = 5)

        assertFalse(target.poisoned)
        assertTrue(player.get("familiar_venom_shot_charged", false))
    }

    @Test
    fun `Explode hits a nearby foe and consumes the familiar`() {
        // Force the random damage roll off zero so the hit is observable.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = summon("giant_chinchompa_familiar")
        val target = createNPC("giant_rat", player.tile.addX(1))
        val before = target.levels.get(Skill.Constitution)

        val cast = player.runSpecial("giant_chinchompa_familiar")
        tick(5) // let the blast land and the familiar dismiss

        assertTrue(cast)
        assertTrue(target.levels.get(Skill.Constitution) < before, "the blast damaged the nearby npc")
        assertEquals(null, player.follower, "the familiar is consumed by the explosion")
    }

    @Test
    fun `Explode auto-fires for free when the familiar is attacked`() {
        // Force the 1/10 auto-trigger (nextInt(10) == 0) while keeping the damage roll observable.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 10) 0 else until - 1
        })
        val player = summon("giant_chinchompa_familiar")
        // A scroll present only so we can prove the free auto-explode leaves it untouched.
        player.inventory.transaction { add("explode_scroll", 2) }
        val attacker = createNPC("giant_rat", player.tile.addX(1))
        val before = attacker.levels.get(Skill.Constitution)

        // Something hitting the familiar rolls the auto-explode.
        attacker.hit(player.follower!!, offensiveType = "melee", damage = 1)
        tick(6) // let the blast land and the familiar dismiss

        assertTrue(attacker.levels.get(Skill.Constitution) < before, "the auto-explosion damaged the attacker")
        assertEquals(2, player.inventory.count("explode_scroll"), "the free auto-explosion spends no scroll")
        assertEquals(null, player.follower, "the familiar is consumed by the auto-explosion")
    }

    @Test
    fun `Explode does nothing with no target nearby`() {
        val player = summon("giant_chinchompa_familiar")

        val cast = player.runSpecial("giant_chinchompa_familiar")
        tick(5)

        assertFalse(cast, "no cast, so no scroll or points are spent")
        assertTrue(player.follower != null, "the familiar is not dismissed on a failed cast")
    }

    @Test
    fun `Insane Ferocity boosts Attack and Strength and drains Ranged, Magic and Defence`() {
        val player = summon("honey_badger_familiar")
        val skills = listOf(Skill.Attack, Skill.Strength, Skill.Ranged, Skill.Magic, Skill.Defence)
        for (skill in skills) {
            player.experience.set(skill, 14_000_000.0) // level 99, so boosts/drains have headroom
            player.levels.set(skill, 50)
        }

        assertTrue(player.runSpecial("honey_badger_familiar"))

        assertTrue(player.levels.get(Skill.Attack) > 50, "Attack is boosted")
        assertTrue(player.levels.get(Skill.Strength) > 50, "Strength is boosted")
        assertTrue(player.levels.get(Skill.Ranged) < 50, "Ranged is drained")
        assertTrue(player.levels.get(Skill.Magic) < 50, "Magic is drained")
        assertTrue(player.levels.get(Skill.Defence) < 50, "Defence is drained")
    }

    @Test
    fun `Call to Arms teleports the owner to the Void Knights' Outpost`() {
        val player = summon("void_ravager_familiar")

        val cast = player.runSpecial("void_ravager_familiar")
        assertTrue(cast)
        tick(4) // the teleport lands (and movement applies) four ticks after the cast

        assertEquals(Tile(2659, 2658, 0), player.tile)
    }

    @Test
    fun `Call to Arms is shared by all four Void familiars`() {
        for (familiar in listOf("void_ravager_familiar", "void_shifter_familiar", "void_spinner_familiar", "void_torcher_familiar")) {
            val player = summon(familiar)

            assertTrue(player.runSpecial(familiar), "$familiar casts Call to Arms")
            tick(4)

            assertEquals(Tile(2659, 2658, 0), player.tile, "$familiar teleports to the outpost")
        }
    }

    @Test
    fun `Fireball Assault hits a nearby foe`() {
        // Force the random damage roll off zero so the hit is observable.
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
        val player = summon("spirit_tz-kih_familiar")
        val target = createNPC("giant_rat", player.tile.addX(1))
        val before = target.levels.get(Skill.Constitution)

        val cast = player.runSpecial("spirit_tz-kih_familiar")
        tick(5) // let the magic hit land

        assertTrue(cast)
        assertTrue(target.levels.get(Skill.Constitution) < before)
    }
}
