package content.skill.slayer

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import kotlin.test.assertTrue

class SlayerTargetTest : WorldTest() {

    private val tasks: List<String>
        get() = Tables.get("slayer_tasks").rows.map { Rows.get(it).rowId }

    @Test
    fun `Every slayer task has a slayer target value`() {
        val values = (VariableDefinitions.get("slayer_target")!!.values as MapValues).values
        val missing = tasks.filter { !values.containsKey(it) }
        assertTrue(missing.isEmpty(), "Slayer tasks without a slayer_target value: $missing")
    }

    @Test
    fun `Every slayer task has npcs in its category`() {
        val categories = NPCDefinitions.definitions.flatMap { it.get<Set<String>>("categories", emptySet()) }.toSet()
        val missing = tasks.filter { it !in categories }
        assertTrue(missing.isEmpty(), "Slayer tasks with no npcs in their category: $missing")
    }

    @Test
    fun `Every master task has a task definition and weight`() {
        val invalid = masters.flatMap { master ->
            val table = Tables.get("${master}_slayer_tasks")
            table.rows.map { Rows.get(it).rowId }.filter { task -> task !in tasks || Tables.int("${master}_slayer_tasks.$task.weight") <= 0 }.map { "$master.$it" }
        }
        assertTrue(invalid.isEmpty(), "Master tasks without a slayer_tasks definition or weight: $invalid")
    }

    @Test
    fun `Every slayer task has npcs which give slayer experience`() {
        val missing = tasks.filter { task ->
            NPCDefinitions.definitions.none { it.get<Set<String>>("categories", emptySet()).contains(task) && it["slayer_xp", it.hitpoints / 10.0] > 0.0 }
        }
        assertTrue(missing.isEmpty(), "Slayer tasks with no npcs giving slayer experience: $missing")
    }

    // Dungeoneering, player-owned house and Soul Wars monsters have no slayer requirements
    private val unrestricted = Regex("^(rand_|poh_|soul_wars_).*|.*_soul_wars$")

    private val attackable: List<NPCDefinition>
        get() = NPCDefinitions.definitions.filter { it.options.contains("Attack") && it.stringId != it.id.toString() && !unrestricted.matches(it.stringId) }

    @Test
    fun `Npcs with the same name share a slayer level`() {
        val mismatched = attackable.groupBy { it.name.lowercase() }.values
            .filter { group -> group.map { it["slayer_level", 0] }.toSet().size > 1 }
            .map { group -> group.joinToString { "${it.stringId}=${it["slayer_level", 0]}" } }
        assertTrue(mismatched.isEmpty(), "Npcs attackable at a different slayer level to others with the same name:\n${mismatched.joinToString("\n")}")
    }

    @Test
    fun `Numbered copies of slayer monsters count towards the same tasks`() {
        val assignable = masters.flatMap { master -> Tables.get("${master}_slayer_tasks").rows.map { Rows.get(it).rowId } }.toSet()
        val npcs = attackable.associateBy { it.stringId }
        val mismatched = npcs.values.mapNotNull { def ->
            val base = npcs[def.stringId.replace(copy, "")] ?: return@mapNotNull null
            val expected = base.get<Set<String>>("categories", emptySet()).intersect(assignable)
            val actual = def.get<Set<String>>("categories", emptySet()).intersect(assignable)
            if (base != def && expected.isNotEmpty() && actual.isEmpty()) "${def.stringId} but ${base.stringId}=$expected" else null
        }
        assertTrue(mismatched.isEmpty(), "Numbered npc copies missing slayer categories:\n${mismatched.joinToString("\n")}")
    }

    private val copy = Regex("_\\d+$")
    private val masters = listOf("turael", "mazchna", "vannaka", "chaeldar", "sumona", "duradel", "kuradal")
}
