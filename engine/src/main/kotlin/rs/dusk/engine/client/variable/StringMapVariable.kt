package rs.dusk.engine.client.variable

data class StringMapVariable(override val id: Int, override val type: Variable.Type, override val persistent: Boolean = false, val values: Map<Int, String>, override val defaultValue: String = values.getOrDefault(0, "")) : Variable<String> {

    init {
        check(values.containsValue(defaultValue)) { "Values must contain default '$defaultValue'" }
    }

    override fun toInt(value: String): Int {
        return values.entries.firstOrNull { it.value == value }?.key ?: super.toInt(value)
    }
}