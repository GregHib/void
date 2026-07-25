package content.area.karamja.tzhaar_city

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class TzHaarHealersTest : WorldTest() {

    @Test
    fun `Healers heal jad 50 life points every 4 ticks`() {
        val jad = createNPC("tztok_jad", Tile(2401, 5088))
        val healer = createNPC("yt_hur_kot", jad.tile.add(-1, 0))
        jad.levels.drain(Skill.Constitution, 500)
        healer.softTimers.start("yt_hur_kot_heal")

        tick(5)
        assertEquals(2050, jad.levels.get(Skill.Constitution))

        tick(4)
        assertEquals(2100, jad.levels.get(Skill.Constitution))
    }

    @Test
    fun `Healers heal jad within 4 tiles of any side of him`() {
        val jad = createNPC("tztok_jad", Tile(2401, 5088))
        val healer = createNPC("yt_hur_kot", jad.tile.add(jad.size + 3, 0))
        jad.levels.drain(Skill.Constitution, 500)
        healer.softTimers.start("yt_hur_kot_heal")

        tick(5)
        assertEquals(2050, jad.levels.get(Skill.Constitution))
    }

    @Test
    fun `Healers do not heal jad from more than 4 tiles away`() {
        val jad = createNPC("tztok_jad", Tile(2401, 5088))
        val healer = createNPC("yt_hur_kot", jad.tile.add(jad.size + 4, 0))
        jad.levels.drain(Skill.Constitution, 500)
        healer.softTimers.start("yt_hur_kot_heal")

        tick(6)
        assertEquals(2000, jad.levels.get(Skill.Constitution))
    }
}
