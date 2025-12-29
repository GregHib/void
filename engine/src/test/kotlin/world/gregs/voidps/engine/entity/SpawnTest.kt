package world.gregs.voidps.engine.entity

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.koin.test.mock.declare
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.Caller
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.ScriptTest
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.type.Tile

class SpawnTest {

    @Nested
    inner class PlayerSpawnTest : ScriptTest {
        override val checks = emptyList<List<String>>()
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            playerSpawn {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Spawn.player(Player())
        }

        override val apis = listOf(Spawn)

    }

    @Nested
    inner class NPCSpawnTest : ScriptTest {
        override val checks = listOf(
            listOf("npc"),
            listOf("*"),
        )

        override val failedChecks = listOf(
            listOf("npc_2"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            npcSpawn(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Spawn.npc(NPC("npc"))
        }

        override val apis = listOf(Spawn)

    }

    @Nested
    inner class ObjectSpawnTest : KoinMock(), ScriptTest {
        override val checks = listOf(
            listOf("obj"),
            listOf("*"),
        )

        override val failedChecks = listOf(
            listOf("obj_2"),
        )

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
            objectSpawn(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Spawn.gameObject(GameObject(0))
        }

        override val apis = listOf(Spawn)

    }

    @Nested
    inner class FloorItemSpawnTest : ScriptTest {
        override val checks = listOf(
            listOf("floor_item"),
            listOf("*"),
        )

        override val failedChecks = listOf(
            listOf("floor_item_2"),
        )

        override fun Script.register(args: List<String>, caller: Caller) {
            floorItemSpawn(args[0]) {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Spawn.floorItem(FloorItem(Tile.EMPTY, "floor_item"))
        }

        override val apis = listOf(Spawn)

    }

    @Nested
    inner class WorldSpawnTest : ScriptTest {
        override val checks = emptyList<List<String>>()
        override val failedChecks = emptyList<List<String>>()

        override fun Script.register(args: List<String>, caller: Caller) {
            worldSpawn {
                caller.call()
            }
        }

        override fun invoke(args: List<String>) {
            Spawn.world(ConfigFiles(mapOf()))
        }

        override val apis = listOf(Spawn)

    }
}