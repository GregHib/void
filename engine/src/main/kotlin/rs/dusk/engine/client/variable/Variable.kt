package rs.dusk.engine.client.variable

import rs.dusk.utility.get

interface Variable<T : Any> {
    val id: Int
    val defaultValue: T
    val type: Type
    val persistent: Boolean

    /**
     * Combined hash of the variable id and type
     * @see toHash
     */
    val hash: Int
        get() = toHash(id, type)

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
        val variables: Variables = get()
        variables.names[name] = hash
        variables.variables[hash] = this
    }

    companion object {

        fun toHash(id: Int, type: Type): Int {
            return type.ordinal + (id shl 2)
        }

        /**
         * Returns the variable id from a hash
         * @param hash The id and type combined
         * @return the unique variable id
         */
        fun idFrom(hash: Int): Int {
            return hash shr 2
        }

        /**
         * Returns the [Variable.Type] from a hash
         * @param hash The id and type combined
         * @return the variable [Type]
         */
        fun typeFrom(hash: Int): Type {
            return Type.values()[hash and 0x4]
        }
    }

}