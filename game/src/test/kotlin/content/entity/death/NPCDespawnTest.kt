package content.entity.death

import WorldTest
import content.entity.combat.damageDealers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems

class NPCDespawnTest : WorldTest() {

    @Test
    fun `Despawning a dying npc drops loot and doesn't respawn`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))
        npc["respawn_tile"] = npc.tile
        npc["respawn_delay"] = 5
        npc.damageDealers[player] = 100
        npc.levels.set(Skill.Constitution, 0)
        tick()
        npc.despawn()
        tick(20)

        assertNotNull(FloorItems.firstOrNull(npc["death_tile", npc.tile], "bones"))
        assertEquals(-1, npc.index)
        assertNull(NPCs.firstOrNull(emptyTile.addY(4)) { it.id == "giant_rat" })
    }

    @Test
    fun `Despawning a npc waiting to respawn removes it`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("giant_rat", emptyTile.addY(4))
        npc["respawn_tile"] = npc.tile
        npc["respawn_delay"] = 20
        npc.damageDealers[player] = 100
        npc.levels.set(Skill.Constitution, 0)
        tick(10)
        npc.despawn()
        tick(30)

        assertEquals(-1, npc.index)
        assertNull(NPCs.firstOrNull(emptyTile.addY(4)) { it.id == "giant_rat" })
    }
}
