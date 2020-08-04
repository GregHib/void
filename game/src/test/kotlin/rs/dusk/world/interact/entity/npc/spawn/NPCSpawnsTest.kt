package rs.dusk.world.interact.entity.npc.spawn

import io.mockk.every
import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCRegistered
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventHandler
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.CollisionFlag
import rs.dusk.engine.map.collision.collisionModule
import rs.dusk.engine.path.traverse.LargeTraversal
import rs.dusk.engine.path.traverse.MediumTraversal
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.world.script.ScriptMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
internal class NPCSpawnsTest : ScriptMock() {

    lateinit var bus: EventBus
    lateinit var spawnHandler: EventHandler<NPC, NPCSpawn>

    @BeforeEach
    override fun setup() {
        bus = spyk(EventBus())
        loadModules(cacheDefinitionModule, entityListModule, module { single { bus }}, collisionModule, fileLoaderModule)
        super.setup()
        spawnHandler = bus.get(NPCSpawn::class)!!
    }

    @Suppress("RemoveExplicitTypeArguments")
    @Test
    fun `Spawn registers`() {
        // Given
        declareMock<NPCDecoder> {
            every { getSafe(any<Int>()) } returns NPCDefinition(id = 1, size = 2)
        }
        val event = spyk(
            NPCSpawn(
                1,
                Tile(10, 20, 1),
                Direction.NONE
            )
        )
        // When
        spawnHandler.invoke(event)
        val npc = event.result!!
        // Then
        assertEquals(1, npc.id)
        verifyOrder {
            bus.emit(any<NPCRegistered>())
            bus.emit(any<Registered>())
        }
        assertEquals(npc.size, Size(2, 2))
        assertEquals(npc.tile, Tile(10, 20, 1))
    }

    @Test
    fun `Traversal size small`() {
        // Given
        declareMock<NPCDecoder> {
            every { getSafe(any<Int>()) } returns NPCDefinition(id = 1, size = 1)
        }
        val event = NPCSpawn(1, Tile(10, 20, 1), Direction.NONE)
        // When
        spawnHandler.invoke(event)
        val npc = event.result!!
        // Then
        val traversal = npc.movement.traversal
        assert(traversal is SmallTraversal)
        traversal as SmallTraversal
        assertEquals(CollisionFlag.ENTITY, traversal.extra)
    }

    @Test
    fun `Traversal size medium`() {
        // Given
        declareMock<NPCDecoder> {
            every { getSafe(any<Int>()) } returns NPCDefinition(id = 1, size = 2)
        }
        val event = NPCSpawn(1, Tile(10, 20, 1), Direction.NONE)
        // When
        spawnHandler.invoke(event)
        val npc = event.result!!
        // Then
        val traversal = npc.movement.traversal
        assert(traversal is MediumTraversal)
        traversal as MediumTraversal
        assertEquals(CollisionFlag.ENTITY, traversal.extra)
    }

    @Test
    fun `Traversal size large`() {
        // Given
        declareMock<NPCDecoder> {
            every { getSafe(any<Int>()) } returns NPCDefinition(id = 1, size = 3)
        }
        val event = NPCSpawn(1, Tile(10, 20, 1), Direction.NONE)
        // When
        spawnHandler.invoke(event)
        val npc = event.result!!
        // Then
        val traversal = npc.movement.traversal
        assert(traversal is LargeTraversal)
        traversal as LargeTraversal
        assertEquals(Size(3, 3), traversal.size)
    }


}