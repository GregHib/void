package world.gregs.voidps.engine.entity.character.mode.interact

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import org.rsmod.game.pathfinder.LineValidator
import org.rsmod.game.pathfinder.PathFinder
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class InteractTest : KoinMock() {

    private lateinit var player: Player
    private lateinit var target: NPC
    private lateinit var interact: Interact
    private var approached = false
    private var operated = false

    override val modules: List<Module> = listOf(
        module {
            single {
                Collisions().apply {
                    for (x in 0 until 24 step 8) {
                        for (y in 0 until 24 step 8) {
                            allocateIfAbsent(x, y, 0)
                        }
                    }
                }
            }
            single { LineValidator(get()) }
            single { StepValidator(get()) }
            single { PathFinder(get()) }
        },
    )

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.client.ui.InterfacesKt")
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        approached = false
        operated = false
        player = spyk(Player(tile = Tile(10, 11)))
        player.interfaces = mockk(relaxed = true)
        every { player.close(null) } returns true
        every { player.sendScript(any()) } just Runs
        every { player.interfaces.get(any()) } returns null
        player.collision = CollisionStrategies.Normal
        target = NPC(tile = Tile(10, 10))
        target.collision = CollisionStrategies.Normal
        declareMock<AreaDefinitions> {
            every { get(any<Zone>()) } returns emptySet()
        }
    }

    private fun interact(operate: Boolean, approach: Boolean, suspend: Boolean) {
        interact = object : Interact(player, target) {
            override fun hasOperate() = operate

            override fun hasApproach() = approach

            override fun operate() {
                if (operate) {
                    Script.launch {
                        if (suspend) {
                            Suspension.start(player, 2)
                        }
                        operated = true
                    }
                }
            }

            override fun approach() {
                if (approach) {
                    Script.launch {
                        if (suspend) {
                            Suspension.start(player, 2)
                        }
                        approached = true
                    }
                }
            }

        }
        player.mode = interact
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 5])
    fun `Operate interaction from distance`(distance: Int) {
        player.tele(10 - distance, 10)
        target.tele(10, 10)
        interact(operate = true, approach = false, suspend = false)

        repeat((distance - 1).coerceAtLeast(1)) {
            assertFalse(operated)
            interact.tick()
        }

        assertEquals(Tile(9, 10), player.tile)
        assertTrue(operated)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 11, 12])
    fun `Approach interacts when in distance`(distance: Int) {
        player.tele(10 + distance, 10)
        target.tele(10, 10)
        interact(operate = false, approach = true, suspend = false)

        assertFalse(approached)
        repeat(2) {
            interact.tick()
        }

        assertTrue(approached)
    }

    @Test
    fun `Switch to operate once within approach range`() {
        player.tele(10, 15)
        target.tele(10, 10)
        interact(operate = true, approach = true, suspend = false)

        assertFalse(approached)
        interact.tick()
        assertTrue(approached)
        interact.updateRange(-1, false)
        repeat(3) {
            assertFalse(operated)
            interact.tick()
        }
        assertEquals(Tile(10, 11), player.tile)
        assertTrue(operated)
    }

    @TestFactory
    fun `Interaction waits before completion`() = listOf("suspension", "interface").map { type ->
        dynamicTest("Interaction waits for $type") {
            operated = false
            interact(operate = true, approach = false, suspend = type == "suspension")
            if (type == "delay") {
                player["delay"] = 2
            } else if (type == "interface") {
                every { player.interfaces.get("main_screen") } returns "an_interface"
            }

            repeat(3) {
                if (type == "interface" && it == 2) {
                    every { player.interfaces.get("main_screen") } returns null
                }
                assertFalse(operated)
                interact.tick()
                GameLoop.tick++
            }

            assertTrue(operated)
            assertEquals(Tile(10, 11), player.tile)
            interact.tick()
            assertEquals(EmptyMode, player.mode)
        }
    }
}
