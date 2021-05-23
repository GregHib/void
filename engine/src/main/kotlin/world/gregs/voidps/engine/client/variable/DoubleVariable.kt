package world.gregs.voidps.engine.client.variable

data class DoubleVariable(
    override val id: Int,
    override val type: Variable.Type,
    override val persistent: Boolean = false,
    override val defaultValue: Double = 0.0
) : Variable<Double> {
    override fun toInt(value: Double): Int {
        return value.toInt() * 10
    }
}