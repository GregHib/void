package world.gregs.voidps.engine.entity.character.mode.move.combat

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.koin.test.mock.declare
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.script.KoinMock
import kotlin.math.truncate
import kotlin.test.assertEquals

class CombatApiTest {

    @Nested
    inner class PlayerCombatStartTest : ScriptTest {
        override val checks = listOf(
            listOf<String>(),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            combatStart {
                caller.call()
                assertTrue(it is NPC)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.start(Player(), NPC())
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class NPCCombatStartTest : ScriptTest {
        override val checks = listOf(
            listOf<String>(),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcCombatStart {
                caller.call()
                assertTrue(it is Player)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.start(NPC(), Player())
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class PlayerCombatStopTest : ScriptTest {
        override val checks = listOf(
            listOf<String>(),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            combatStop {
                caller.call()
                assertTrue(it is NPC)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.stop(Player(), NPC())
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class NPCCombatStopTest : ScriptTest {
        override val checks = listOf(
            listOf<String>(),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcCombatStop {
                caller.call()
                assertTrue(it is Player)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.stop(NPC(), Player())
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class PlayerCombatPrepareTest : ScriptTest {
        override val checks = listOf(
            listOf("style"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            combatPrepare(args[0]) {
                caller.call()
                assertTrue(it is NPC)
                true
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.prepare(Player(), NPC(), "style")
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class NPCCombatPrepareTest : ScriptTest {
        override val checks = listOf(
            listOf("npc"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcCombatPrepare(args[0]) {
                caller.call()
                assertTrue(it is Player)
                true
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.prepare(NPC("npc"), Player())
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class PlayerCombatSwingTest : ScriptTest {
        override val checks = listOf(
            listOf("weapon", "style"),
            listOf("*", "style"),
            listOf("weapon", "*"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            combatSwing(args[0], args[1]) {
                caller.call()
                assertTrue(it is NPC)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.swing(Player(), NPC(), "weapon", "style")
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class NPCCombatSwingTest : ScriptTest {
        override val checks = listOf(
            listOf<String>(),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcCombatSwing {
                caller.call()
                assertTrue(it is Player)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.swing(NPC("npc"), Player(), "style")
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class PlayerCombatAttackTest : ScriptTest {
        override val checks = listOf(
            listOf("style"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            combatAttack(args[0]) {
                caller.call()
                assertTrue(it.target is NPC)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.attack(Player(), CombatAttack(NPC("npc"), 0, "style", Item.EMPTY, "spell", true, 1))
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class NPCCombatAttackTest : ScriptTest {
        override val checks = listOf(
            listOf("npc"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcCombatAttack(args[0]) {
                caller.call()
                assertTrue(it.target is Player)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.attack(NPC("npc"), CombatAttack(Player(), 0, "style", Item.EMPTY, "spell", true, 1))
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class PlayerCombatDamageTest : ScriptTest {
        override val checks = listOf(
            listOf("style"),
            listOf("*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            combatDamage(args[0]) {
                caller.call()
                assertTrue(it.source is NPC)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.damage(Player(), CombatDamage(NPC("npc"), "style", 0, Item.EMPTY, "spell", true))
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class NPCCombatDamageTest : ScriptTest {
        override val checks = listOf(
            listOf("npc", "style"),
            listOf("npc", "*"),
            listOf("*", "style"),
            listOf("*", "*"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            npcCombatDamage(args[0], args[1]) {
                caller.call()
                assertTrue(it.source is Player)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.damage(NPC("npc"), CombatDamage(Player(), "style", 0, Item.EMPTY, "spell", true))
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class SpecialAttackTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            specialAttack(args[0]) { target, id ->
                caller.call()
                assertTrue(target is NPC)
                assertEquals("id", id)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.special(Player(), NPC("npc"), "id")
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class SpecialAttackPrepareTest : ScriptTest {
        override val checks = listOf(
            listOf("id"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            specialAttackPrepare(args[0]) { id ->
                caller.call()
                assertEquals("id", id)
                true
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.prepareSpec(Player(), "id")
        }

        override val apis = listOf(CombatApi)

    }

    @Nested
    inner class SpecialAttackDamageTest : ScriptTest {
        override val checks = listOf(
            listOf("mode"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            specialAttackDamage(args[0]) { target, damage ->
                caller.call()
                assertTrue(target is NPC)
                assertEquals(4, damage)
            }
        }

        override fun invoke(args: List<String>) {
            CombatApi.damageSpec(Player(), NPC("npc"), "mode", 4)
        }

        override val apis = listOf(CombatApi)

    }

}