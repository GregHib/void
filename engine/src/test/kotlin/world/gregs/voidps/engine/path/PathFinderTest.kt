package world.gregs.voidps.engine.path

import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.collision.Collisions

internal class PathFinderTest {
    lateinit var pf: PathFinder
    lateinit var collisions: Collisions
    lateinit var source: Character

    @BeforeEach
    fun setup() {
        source = mockk(relaxed = true)
        collisions = mockk(relaxed = true)
        pf = spyk(PathFinder())
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionStrategyKt")
    }

    @Test
    fun `Player smart finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Smart, false)
        // Then
        verify {
//            bfs.find(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Player dumb finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Follow, false)
        // Then
        verify {
//            dd.find(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `NPC finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Dumb, false)
        // Then
        verify {
//            aa.find(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Retreat finder`() {
        // When
        pf.find(source, mockk<Path>(relaxed = true), PathType.Retreat, false)
        // Then
        verify {
//            retreat.find(any(), any(), any(), any(), any())
        }
    }

}