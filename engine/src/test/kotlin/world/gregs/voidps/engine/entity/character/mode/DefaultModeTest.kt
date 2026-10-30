package world.gregs.voidps.engine.entity.character.mode

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.script
import kotlin.test.assertNull
import kotlin.test.assertSame

class DefaultModeTest {

    @Nested
    inner class NPCDefaultModeTest : ScriptTest {
        override val checks = listOf(
            listOf("npc"),
            listOf("*"),
        )
        override val failedChecks = listOf(
            listOf("other_npc"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            npcDefaultMode(args[0]) {
                caller.call()
                EmptyMode
            }
        }

        override fun invoke(args: List<String>) {
            DefaultMode.get(NPC("npc"))
        }

        override val apis = listOf(DefaultMode)
    }

    @AfterEach
    fun teardown() {
        DefaultMode.close()
    }

    @Test
    fun `Null handler falls through to the next`() {
        val mode = object : Mode {
            override fun tick() {}
        }
        script {
            npcDefaultMode("npc") { null }
            npcDefaultMode("*") { mode }
        }
        assertSame(mode, DefaultMode.get(NPC("npc")))
    }

    @Test
    fun `No handler returns null`() {
        assertNull(DefaultMode.get(NPC("npc")))
    }
}
