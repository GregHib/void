package content.area.kandarin.yanille

import FakeRandom
import WorldTest
import content.entity.combat.hit.hit
import content.entity.player.equip.DragonfireShield
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.setRandom

class SalarinTheTwistedTest : WorldTest() {

    @TestFactory
    fun `Elemental spells deal Salarin fixed damage`() = listOf(
        "wind_strike" to 90,
        "water_strike" to 100,
        "earth_strike" to 110,
        "fire_strike" to 120,
    ).map { (spell, expected) ->
        DynamicTest.dynamicTest(spell) {
            val player = createPlayer(emptyTile)
            val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
            val health = salarin.levels.get(Skill.Constitution)

            player.hit(salarin, offensiveType = "magic", spell = spell, damage = 200)
            tick(3)

            assertEquals(health - expected, salarin.levels.get(Skill.Constitution))
        }
    }

    @Test
    fun `Wind rush always deals fixed damage to Salarin`() {
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
        val health = salarin.levels.get(Skill.Constitution)

        player.hit(salarin, offensiveType = "magic", spell = "wind_rush", damage = -1)
        tick(3)

        assertEquals(health - 90, salarin.levels.get(Skill.Constitution))
    }

    @Test
    fun `Strike spells can still miss Salarin`() {
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
        val health = salarin.levels.get(Skill.Constitution)

        player.hit(salarin, offensiveType = "magic", spell = "fire_strike", damage = -1)
        tick(3)

        assertEquals(health, salarin.levels.get(Skill.Constitution))
    }

    @Test
    fun `Magic dart damages Salarin normally`() {
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
        val health = salarin.levels.get(Skill.Constitution)

        player.hit(salarin, offensiveType = "magic", spell = "magic_dart", damage = 150)
        tick(3)

        assertEquals(health - 150, salarin.levels.get(Skill.Constitution))
    }

    @Test
    fun `Dragonfire shield discharge damages Salarin`() {
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
        val health = salarin.levels.get(Skill.Constitution)

        player.hit(salarin, weapon = Item(DragonfireShield.CHARGED), offensiveType = "dragonfire", damage = 200)
        tick(3)

        assertEquals(health - 200, salarin.levels.get(Skill.Constitution))
    }

    @Test
    fun `Deflect attacks damage Salarin`() {
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
        val health = salarin.levels.get(Skill.Constitution)

        player.hit(salarin, offensiveType = "deflect", damage = 200)
        tick(3)

        assertEquals(health - 200, salarin.levels.get(Skill.Constitution))
    }

    @Test
    fun `Korasi sword special damages Salarin`() {
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
        val health = salarin.levels.get(Skill.Constitution)

        player.hit(salarin, weapon = Item("korasis_sword"), offensiveType = "magic", special = true, damage = 200)
        tick(3)

        assertEquals(health - 200, salarin.levels.get(Skill.Constitution))
    }

    @TestFactory
    fun `Salamander attack styles cannot damage Salarin`() = listOf(
        "scorch",
        "range",
        "blaze",
    ).map { type ->
        DynamicTest.dynamicTest(type) {
            val player = createPlayer(emptyTile)
            val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
            val health = salarin.levels.get(Skill.Constitution)

            player.hit(salarin, weapon = Item("black_salamander"), offensiveType = type, damage = 200)
            tick(3)

            assertEquals(health, salarin.levels.get(Skill.Constitution))
        }
    }

    @Test
    fun `Blocked attacks only sometimes trigger Salarin's taunt`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 1
        })
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))

        player.hit(salarin, offensiveType = "slash", damage = 200)
        tick(1)

        assertEquals("", salarin.visuals.say.text)
    }

    @Test
    fun `Blocked attacks can trigger Salarin's taunt`() {
        setRandom(FakeRandom())
        val player = createPlayer(emptyTile)
        val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))

        player.hit(salarin, offensiveType = "slash", damage = 200)
        tick(1)

        assertEquals("Your pitiful attacks cannot hurt me!", salarin.visuals.say.text)
    }

    @TestFactory
    fun `Unsupported attacks cannot damage Salarin`() = listOf(
        Triple("slash", "", Item.EMPTY),
        Triple("magic", "fire_bolt", Item.EMPTY),
        Triple("dragonfire", "", Item("dragon_bolts_e")),
    ).map { (type, spell, weapon) ->
        DynamicTest.dynamicTest(listOf(type, spell.ifBlank { weapon.id.ifBlank { "unarmed" } }).joinToString(":")) {
            val player = createPlayer(emptyTile)
            val salarin = createNPC("salarin_the_twisted", emptyTile.addY(4))
            val health = salarin.levels.get(Skill.Constitution)

            player.hit(salarin, weapon = weapon, offensiveType = type, spell = spell, damage = 200)
            tick(3)

            assertEquals(health, salarin.levels.get(Skill.Constitution))
        }
    }
}
