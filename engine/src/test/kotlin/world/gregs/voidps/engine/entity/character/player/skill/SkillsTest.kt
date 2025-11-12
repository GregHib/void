package world.gregs.voidps.engine.entity.character.player.skill

import org.junit.jupiter.api.Nested
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.test.assertEquals

class SkillsTest {

    @Nested
    inner class LevelChangedTest : ScriptTest {
        override val checks = listOf(
            listOf(),
            listOf("Attack")
        )
        override val failedChecks = listOf(
            listOf("Strength"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            val input = args.getOrNull(0)?.let { Skill.of(it) }
            levelChanged(input) { skill, from, to ->
                caller.call()
                assertEquals(Skill.Attack, skill)
                assertEquals(1, from)
                assertEquals(2, to)
            }
        }

        override fun invoke(args: List<String>) {
            Skills.changed(Player(), Skill.Attack, 1, 2)
        }

        override val apis = listOf(Skills)

    }

    @Nested
    inner class NPCLevelChangedTest : ScriptTest {
        override val checks = listOf(
            listOf("Attack", "npc"),
            listOf("Attack", "*")
        )
        override val failedChecks = listOf(
            listOf("Strength", "npc"),
            listOf("Strength", "*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            npcLevelChanged(Skill.of(args[0])!!, args[1]) { skill, from, to ->
                caller.call()
                assertEquals(Skill.Attack, skill)
                assertEquals(1, from)
                assertEquals(2, to)
            }
        }

        override fun invoke(args: List<String>) {
            Skills.changed(NPC("npc"), Skill.Attack, 1, 2)
        }

        override val apis = listOf(Skills)

    }

    @Nested
    inner class MaxLevelChangedTest : ScriptTest {
        override val checks = listOf(
            listOf(),
            listOf("Attack")
        )
        override val failedChecks = listOf(
            listOf("Strength"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            val input = args.getOrNull(0)?.let { Skill.of(it) }
            maxLevelChanged(input) { skill, from, to ->
                caller.call()
                assertEquals(Skill.Attack, skill)
                assertEquals(1, from)
                assertEquals(2, to)
            }
        }

        override fun invoke(args: List<String>) {
            Skills.maxChanged(Player(), Skill.Attack, 1, 2)
        }

        override val apis = listOf(Skills)

    }

    @Nested
    inner class ExperienceTest : ScriptTest {
        override val checks = listOf(listOf<String>())
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            experience { skill, from, to ->
                caller.call()
                assertEquals(Skill.Attack, skill)
                assertEquals(10, from)
                assertEquals(20, to)
            }
        }

        override fun invoke(args: List<String>) {
            Skills.exp(Player(), Skill.Attack, 10, 20)
        }

        override val apis = listOf(Skills)

    }

    @Nested
    inner class BlockedExperienceTest : ScriptTest {
        override val checks = listOf(listOf<String>())
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            blockedExperience { skill, exp ->
                caller.call()
                assertEquals(Skill.Attack, skill)
                assertEquals(3.0, exp)
            }
        }

        override fun invoke(args: List<String>) {
            Skills.blocked(Player(), Skill.Attack, 3.0)
        }

        override val apis = listOf(Skills)

    }

}