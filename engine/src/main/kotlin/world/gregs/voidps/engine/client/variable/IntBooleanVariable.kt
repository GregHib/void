package world.gregs.voidps.engine.client.variable

data class IntBooleanVariable(
    override val id: Int,
    override val type: Variable.Type,
    override val persistent: Boolean = false,
    val trueIntValue: Int = 1,
    val falseIntValue: Int = 0,
    override val defaultValue: Boolean = false
) : Variable<Boolean> {
    override fun toInt(value: Boolean): Int {
        return if (value) trueIntValue else falseIntValue
    }
}