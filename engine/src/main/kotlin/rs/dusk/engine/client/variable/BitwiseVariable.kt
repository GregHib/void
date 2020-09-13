package rs.dusk.engine.client.variable

data class BitwiseVariable<T>(override val id: Int, override val type: Variable.Type, override val persistent: Boolean = false, override val defaultValue: Int = 0, val values: List<T>) : BitwiseVar<T>() {
    override fun toInt(value: Int): Int {
        return value
    }

    /**
     * @return pow(2, index) or null if not found
     */
    override fun getValue(id: T): Int? {
        val index = values.indexOf(id)
        if (index == -1) {
            return null//Invalid value
        }
        return 1 shl index//Return power of 2 of the index
    }
}