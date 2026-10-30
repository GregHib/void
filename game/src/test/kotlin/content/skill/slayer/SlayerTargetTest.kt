package content.skill.slayer

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import kotlin.test.assertTrue

class SlayerTargetTest : WorldTest() {

    // Tasks the 634 client has no slayer target name for
    private val unmapped = setOf(
        "aviansies",
        "bandits",
        "black_knights",
        "chaos_druid",
        "crabs",
        "dark_warriors",
        "frost_dragons",
        "magic_axe",
        "pirates",
        "rogues",
        "spiritual_creatures",
    )

    private val tasks: List<String>
        get() = Tables.get("slayer_tasks").rows.map { Rows.get(it).rowId }

    @Test
    fun `Every slayer task has a slayer target value`() {
        val values = (VariableDefinitions.get("slayer_target")!!.values as MapValues).values
        val missing = tasks.filter { it !in unmapped && !values.containsKey(it) }
        assertTrue(missing.isEmpty(), "Slayer tasks without a slayer_target value: $missing")
    }

    @Test
    fun `Every slayer task has npcs in its category`() {
        val categories = NPCDefinitions.definitions.flatMap { it.get<Set<String>>("categories", emptySet()) }.toSet()
        val missing = tasks.filter { it !in categories }
        assertTrue(missing.isEmpty(), "Slayer tasks with no npcs in their category: $missing")
    }
}
