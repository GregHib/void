package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.Definition

class DefinitionModifications {
    private val modifications = mutableMapOf<String, (Any) -> Any>()
    private val additions = mutableListOf<(MutableMap<String, Any>) -> Unit>()
    private val transformations = mutableListOf<(Array<out Definition>, Map<Int, String>) -> Unit>()

    operator fun set(key: String, block: (Any) -> Any) {
        modifications[key] = block
    }

    fun transform(block: (Array<out Definition>, Map<Int, String>) -> Unit) {
        transformations.add(block)
    }

    @Suppress("UNCHECKED_CAST")
    fun map(key: String, block: (Map<String, Any>) -> Any) {
        set(key) { block(it as Map<String, Any>) }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmName("cast")
    operator fun <T : Any> set(key: String, block: (T) -> Any) {
        set(key) { block(it as T) }
    }

    fun add(block: (MutableMap<String, Any>) -> Unit) {
        additions.add(block)
    }

    fun modify(value: Map<String, Any>): Map<String, Any> {
        val map = value.toMutableMap()
        for ((mod, block) in modifications) {
            if (map.containsKey(mod)) {
                map[mod] = block(map[mod] ?: continue)
            }
        }
        append(map)
        return map
    }

    fun append(map: MutableMap<String, Any>): Map<String, Any> {
        for (addition in additions) {
            addition(map)
        }
        return map
    }

    fun apply(map: Map<String, Map<String, Any>>): Map<String, Map<String, Any>> {
        if (modifications.isEmpty() && additions.isEmpty()) {
            return map
        }
        return map.mapValues { (_, value) ->
            val copy = map[value["copy"]]
            if (copy != null) {
                val mut = copy.toMutableMap()
                for ((k, v) in value) {
                    mut[k] = v
                }
                modify(mut)
            } else {
                modify(value)
            }
        }
    }

    fun apply(array: Array<out Definition>, names: Map<Int, String>) {
        for (transform in transformations) {
            transform.invoke(array, names)
        }
    }
}