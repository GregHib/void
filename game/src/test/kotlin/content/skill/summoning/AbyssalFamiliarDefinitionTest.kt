package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions

class AbyssalFamiliarDefinitionTest : WorldTest() {

    @Test
    fun `abyssal essence familiars are essence-only beasts of burden after config load`() {
        val cases = mapOf(
            "abyssal_parasite_familiar" to 7,
            "abyssal_lurker_familiar" to 12,
            "abyssal_titan_familiar" to 20,
        )
        for ((name, capacity) in cases) {
            val def = NPCDefinitions.get(name)
            assertEquals(1, def.get("summoning_beast_of_burden", 0), name)
            assertEquals(capacity, def.get("summoning_beast_of_burden_capacity", 0), name)
            assertEquals(1, def.get("summoning_beast_of_burden_essence", 0), name)
        }
    }
}
