package world.gregs.void.engine.data

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

/**
 * @author GregHib <greg@gregs.world>
 * @since April 03, 2020
 */
internal class DataLoaderTest {

    @Test
    fun load() {
        // Given
        val strategy = mockk<StorageStrategy<Any>>(relaxed = true)
        val loader = object : DataLoader<Any>(strategy) {}
        // When
        loader.load("test")
        // Then
        verify { strategy.load("test") }
    }

    @Test
    fun save() {
        // Given
        val strategy = mockk<StorageStrategy<Any>>(relaxed = true)
        val data = mockk<Any>(relaxed = true)
        val loader = object : DataLoader<Any>(strategy) {}
        // When
        loader.save("test", data)
        // Then
        verify { strategy.save("test", data) }
    }
}