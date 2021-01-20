package world.gregs.void.engine.client.variable

import world.gregs.void.utility.func.toInt

data class NegativeBooleanVariable(override val id: Int, override val type: Variable.Type, override val persistent: Boolean = false, override val defaultValue: Boolean = false) : Variable<Boolean> {
    override fun toInt(value: Boolean): Int {
        return (!value).toInt()
    }
}