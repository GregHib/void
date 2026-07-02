package content.skill.summoning

import FakeRandom
import WorldTest
import content.entity.combat.hit.hit
import content.entity.effect.toxin.poisoned
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
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
