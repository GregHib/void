package world.gregs.voidps.engine.client.variable

interface Variable {

    val variables: Variables

    fun sendVariable(key: String) = variables.send(key)

    fun addVarbit(key: String, value: Any, refresh: Boolean = true) =
        variables.bits.set(key, value, refresh)

    fun removeVarbit(key: String, value: Any, refresh: Boolean = true) =
        variables.bits.remove(key, value, refresh)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> remove(key: String, refresh: Boolean = true) =
        variables.clear(key, refresh) as? T

    fun clear(key: String, refresh: Boolean = true) =
        variables.clear(key, refresh)

    fun toggle(key: String, refresh: Boolean = true): Boolean {
        val value = variables.get(key, false)
        variables.set(key, !value as Any, refresh)
        return !value
    }

    fun inc(key: String, amount: Int = 1, refresh: Boolean = true): Int {
        val value: Int = variables.get(key, 0)
        variables.set(key, value + amount, refresh)
        return value + amount
    }

    fun dec(key: String, amount: Int = 1, refresh: Boolean = true): Int {
        val value: Int = variables.get(key, 0)
        variables.set(key, value - amount, refresh)
        return value - amount
    }

    operator fun set(key: String, refresh: Boolean, value: Any) = variables.set(key, value, refresh)

    operator fun set(key: String, value: Any) = variables.set(key, value)

    fun containsVarbit(key: String, id: Any): Boolean {
        return variables.bits.contains(key, id)
    }

    fun contains(key: String): Boolean {
        return variables.contains(key)
    }

    operator fun <T : Any> get(key: String): T {
        return variables.get(key)
    }

    operator fun <T : Any> get(key: String, default: T): T {
        return variables.get(key, default)
    }

    fun <T : Any> getOrNull(key: String): T? {
        return variables.getOrNull(key)
    }

    fun <T : Any> getOrPut(key: String, block: () -> T): T {
        return variables.getOrPut(key, block)
    }
}