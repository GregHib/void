package rs.dusk.engine.client.variable

data class ListVariable<T : Any>(override val id: Int, override val type: Variable.Type, override val persistent: Boolean = false, val values: List<T>, override val defaultValue: T = values.first()) : Variable<T> {
    override fun toInt(value: T): Int {
        val index = values.indexOf(value)
        return if(index != -1) index else super.toInt(value)
    }
}