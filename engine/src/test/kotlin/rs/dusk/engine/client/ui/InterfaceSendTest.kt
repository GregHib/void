package rs.dusk.engine.client.ui

import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.detail.InterfaceDetail

abstract class InterfaceSendTest : InterfaceTest() {

    abstract fun sendStrings(name: String, component: String): Boolean

    abstract fun expected(inter: InterfaceDetail, component: Int)

    @Test
    fun `Interface send component player head names`() {
        names[1] = "name"
        val inter = InterfaceDetail(id = 1, components = mapOf("component_name" to 2))
        interfaces["name"] = inter
        val result = sendStrings("name", "component_name")
        assertTrue(result)
        verify { expected(inter, 2) }
    }

    @Test
    fun `Interface send player head invalid component`() {
        names[1] = "name"
        val inter = InterfaceDetail(id = 1, components = mapOf("component_name" to 2))
        interfaces["name"] = inter
        val result = manager.sendPlayerHead("name", "unknown")
        assertFalse(result)
        verify(exactly = 0) { expected(inter, any()) }
    }

}
