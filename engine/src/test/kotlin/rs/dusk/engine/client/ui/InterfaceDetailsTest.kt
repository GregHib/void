package rs.dusk.engine.client.ui

import org.junit.jupiter.api.Test
import rs.dusk.engine.data.file.FileLoader

internal class InterfaceDetailsTest {

    private lateinit var ifaces: MutableMap<Int, Interface>


    @Test
    fun `Set resizable`() {
        val loader = InterfaceDetails(FileLoader())
        loader.load()
    }

}
