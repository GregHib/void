package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.utility.get

interface Variable<T : Any> {
    val id: Int
    val defaultValue: T
    val type: Type
    val persistent: Boolean

    /**
     * Converts value to an integer for sending to the client
     * @param value The value to convert
     * @return integer value
     */
    fun toInt(value: T): Int {
        return -1
    }

    /**
     * The variable client type
     */
    enum class Type {
        VARBIT, VARP, VARC, VARCSTR
    }

    /**
     * Registers the variable into the global list [variables] with identifier [name]
     */
    fun register(name: String) {
        get<VariableManager>().register(name, this)
    }

}