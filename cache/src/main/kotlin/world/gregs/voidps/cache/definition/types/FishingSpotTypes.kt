package world.gregs.voidps.cache.definition.types

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

object FishingSpotTypes : Types() {

    private val indices = Int2IntOpenHashMap()
    private lateinit var option: Array<String?>
    private lateinit var tackle: Array<String?>
    private lateinit var alternative: Array<String?>
    private lateinit var baitType: Array<Array<String>?>
    private lateinit var bait: Array<Array<String>?>
    private lateinit var secondary: Array<Array<String>?>

    private const val SIZE = 60

    init {
        indices.defaultReturnValue(-1)
        set(SIZE)
    }

    fun index(id: Int) = indices.get(id)

    fun option(id: Int) = option[id]
    fun tackle(id: Int) = tackle[id]
    fun alternative(id: Int) = alternative[id]
    fun baitType(id: Int) = baitType[id]
    fun bait(id: Int) = bait[id]
    fun secondary(id: Int) = secondary[id]

    override fun set(size: Int) {
        index = 0
        option = arrayOfNulls(size)
        tackle = arrayOfNulls(size)
        alternative = arrayOfNulls(size)
        baitType = arrayOfNulls(size)
        bait = arrayOfNulls(size)
        secondary = arrayOfNulls(size)
    }

    override fun nullStrings() = listOf(option, tackle, alternative)
    override fun nullStringArrays() = listOf(baitType, bait, secondary)

    var index = 0

    @Suppress("UNCHECKED_CAST")
    override fun load(key: String, value: Any, id: Int, section: String): Boolean {
        if (!key.startsWith("fishing_")) {
            return false
        }
        option[index] = key.substringAfter("fishing_").replaceFirstChar { it.uppercase() }
        value as Map<String, Any>
        value["items"]?.let {
            it as List<String>
            tackle[index] = it[0]
            alternative[index] = it.getOrNull(1)
        }
        value["bait"]?.let { map ->
            map as Map<String, Any>
            var primary = true
            baitType[index] = map.keys.toTypedArray()
            require(map.size <= 2) { "Expected at most 2 bait types for spot $id" }
            for ((_, value) in map) {
                if (primary) {
                    primary = false
                    bait[index] = (value as List<String>).toTypedArray()
                } else {
                    secondary[index] = (value as List<String>).toTypedArray()
                }
            }
        }
        return true
    }

    override fun after(id: Int, section: String) {
        indices.put(id, PickpocketTypes.index++)
    }
}
