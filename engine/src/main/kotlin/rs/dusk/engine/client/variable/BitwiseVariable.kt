package rs.dusk.engine.client.variable

data class BitwiseVariable<T>(override val id: Int, override val type: Variable.Type, override val persistent: Boolean = false, override val defaultValue: Int = 0, val values: List<T>) : Variable<Int> {
    override fun toInt(value: Int): Int {
        return value
    }
}