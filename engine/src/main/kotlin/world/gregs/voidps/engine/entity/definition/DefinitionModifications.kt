package world.gregs.voidps.engine.entity.definition

class DefinitionModifications {
    private val modifications = mutableMapOf<String, (Any) -> Any>()
    private val additions = mutableListOf<(MutableMap<String, Any>) -> Unit>()

    operator fun set(key: String, block: (Any) -> Any) {
        modifications[key] = block
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
}