package world.gregs.voidps.engine.client.ui

object Interface {
    fun getId(packed: Int) = packed shr 16
    fun getComponentId(packed: Int) = packed and 0xffff
    fun pack(id: Int, component: Int) = (id shl 16) or component
}