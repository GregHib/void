package world.gregs.voidps.engine.entity.character.npc

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.koin.test.mock.declare
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunt
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class HuntTest {

    @Nested
    inner class HuntFloorItemTest : ScriptTest {
        override val checks = listOf(
            listOf("mode"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            huntFloorItem(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Hunt.hunt(NPC("npc"), FloorItem(Tile.EMPTY, "item"), "mode")
        }

        override val apis = listOf(Hunt)

    }

    @Nested
    inner class HuntNPCTest : ScriptTest {
        override val checks = listOf(
            listOf("mode"),
        )
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            huntNPC(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Hunt.hunt(NPC("npc"), NPC("target"), "mode")
        }

        override val apis = listOf(Hunt)

    }

    @Nested
    inner class HuntPlayerTest : ScriptTest {
        override val checks = listOf(
            listOf("npc", "mode"),
            listOf("*", "mode"),
        )
        override val failedChecks = listOf(
            listOf("npc", "*"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            huntPlayer(args[0], args[1]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Hunt.hunt(NPC("npc"), Player(), "mode")
        }

        override val apis = listOf(Hunt)

    }

    @Nested
    inner class HuntObjectTest : KoinMock(), ScriptTest {
        override val checks = listOf(
            listOf("mode"),
        )
        override val failedChecks = emptyList<List<String>>()

        @BeforeEach
        fun setup() {
            declare {
                val def = mockk<ObjectDefinitions>(relaxed = true)
                every { def.resolve(any(), any()) } returns ObjectDefinition(0, stringId = "obj")
                every { def.get(any<Int>()) } returns ObjectDefinition(0, stringId = "obj")
                def
            }
        }

        override fun Script.register(args: List<String>, caller: Caller) {
            huntObject(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Hunt.hunt(NPC("npc"), GameObject(0), "mode")
        }

        override val apis = listOf(Hunt)

    }

}