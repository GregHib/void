package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions

class PackYakDefinitionTest : WorldTest() {

    @Test
    fun `pack yak familiar has beast of burden params after config load`() {
        val def = NPCDefinitions.get("pack_yak_familiar")
        assertEquals(1, def.get("summoning_beast_of_burden", 0))
        assertEquals(30, def.get("summoning_beast_of_burden_capacity", 0))
    }
}
