package rs.dusk.engine.client.ui

import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail

abstract class InterfaceSendTest : InterfaceTest() {

    abstract fun sendStrings(name: String, component: String): Boolean

    abstract fun expected(component: InterfaceComponentDetail)

    @Test
    fun `Interface send component player head names`() {
        names[1] = "name"
        val comp = InterfaceComponentDetail(2, "component_name")
        val inter = InterfaceDetail(id = 1, components = mapOf("component_name" to comp))
        comp.parent = inter.id
        interfaces["name"] = inter
        val result = sendStrings("name", "component_name")
        assertTrue(result)
        verify { expected(comp) }
    }

    @Test
    fun `Interface send player head invalid component`() {
        names[1] = "name"
        val inter = InterfaceDetail(id = 1, components = mapOf("component_name" to InterfaceComponentDetail(2, "component_name")))
        interfaces["name"] = inter
        val result = manager.sendPlayerHead("name", "unknown")
        assertFalse(result)
        verify(exactly = 0) { expected(any()) }
    }

}
