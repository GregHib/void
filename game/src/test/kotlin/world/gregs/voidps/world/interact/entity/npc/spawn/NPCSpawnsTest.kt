package world.gregs.voidps.world.interact.entity.npc.spawn

import io.mockk.every
import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.mock.declareMock
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.data.file.fileLoaderModule
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCRegistered
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.path.traverse.LargeTraversal
import world.gregs.voidps.engine.path.traverse.MediumTraversal
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.world.script.ScriptMock

/**
 * @author GregHib <greg@gregs.world>
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
        declareMock<NPCDefinitions> {
            every { get(any<Int>()) } returns NPCDefinition(id = 1, size = 2)
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
        declareMock<NPCDefinitions> {
            every { get(any<Int>()) } returns NPCDefinition(id = 1, size = 1)
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
        declareMock<NPCDefinitions> {
            every { get(any<Int>()) } returns NPCDefinition(id = 1, size = 2)
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
        declareMock<NPCDefinitions> {
            every { get(any<Int>()) } returns NPCDefinition(id = 1, size = 3)
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