package world.gregs.voidps.engine.client.variable

class VariableData(
    private val variables: MutableMap<String, Any>,
    private val temporaryVariables: MutableMap<String, Any> = mutableMapOf()
) : MutableMap<String, Any> {

    var persist = true

    private val map: MutableMap<String, Any>
        get() = if (persist) variables else temporaryVariables

    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() = map.entries
    override val keys: MutableSet<String>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: MutableCollection<Any>
        get() = map.values

    override fun clear() = map.clear()

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun remove(key: String): Any? = map.remove(key)

    override fun putAll(from: Map<out String, Any>) = map.putAll(from)

    override fun put(key: String, value: Any): Any? = map.put(key, value)

    override fun get(key: String): Any? = map[key]

    override fun containsValue(value: Any): Boolean = map.containsValue(value)

    override fun containsKey(key: String): Boolean = map.containsKey(key)

}