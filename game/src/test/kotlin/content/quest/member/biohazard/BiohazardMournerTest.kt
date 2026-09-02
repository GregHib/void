package content.quest.member.biohazard

import WorldTest
import content.entity.combat.hit.damage
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BiohazardMournerTest : WorldTest() {

    override var loadNpcs: Boolean = true

    private val deathTicks = 20

    private val respawnTicks = 24

    @Test
    fun `The mourners the world spawns upstairs are the ones carrying the key`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        val mourner = NPCs.findOrNull(Tile(2551, 3324, 1), "mourner_2")
        assertNotNull(mourner, "a mourner should spawn upstairs in the headquarters")

        mourner.damage(5000, source = player)
        tick(deathTicks)

        val keys = FloorItems.at(mourner.tile.zone).flatten().count { it.id == "key_biohazard" }
        assertEquals(1, keys, "the spawned mourner should drop a key")
    }

    @Test
    fun `A killed mourner comes back`() {
        val player = createPlayer(Tile(2551, 3325, 1))
        player["biohazard"] = "poisoned_stew"
        val mourner = NPCs.findOrNull(Tile(2551, 3324, 1), "mourner_2")
        assertNotNull(mourner)

        mourner.damage(5000, source = player)
        tick(deathTicks)
        assertTrue(mourner.hide, "the body should be gone")

        tick(respawnTicks)

        assertFalse(mourner.hide, "and another mourner should take its place")
        assertEquals(Tile(2551, 3324, 1), mourner.tile)
    }

    @Test
    fun `Mourners fight with their own animations and sounds`() {
        val sounds: SoundDefinitions = get()
        val definition = get<CombatDefinitions>().getOrNull("mourner")
        assertNotNull(definition, "the mourners need a combat definition to fight with")

        assertEquals("mourner_defend", definition.defendAnim)
        assertEquals("mourner_defend", definition.defendSound?.id)
        assertEquals("mourner_death", definition.deathAnim)
        assertEquals("mourner_death", definition.deathSound?.id)
        assertEquals(424, AnimationDefinitions.get("mourner_defend").id)
        assertEquals(836, AnimationDefinitions.get("mourner_death").id)
        assertEquals(513, sounds.get("mourner_defend").id)
        assertEquals(512, sounds.get("mourner_death").id)

        val melee = definition.attacks["melee"]
        assertNotNull(melee, "the mourners need a melee attack")
        assertEquals("mourner_attack", melee.anim)
        assertEquals(422, AnimationDefinitions.get("mourner_attack").id)
        assertTrue(melee.sounds.isEmpty(), "mourners punch silently")
        assertEquals("crush", melee.targetHits.first().offense)
        assertEquals(20, melee.targetHits.first().max)
    }
}
