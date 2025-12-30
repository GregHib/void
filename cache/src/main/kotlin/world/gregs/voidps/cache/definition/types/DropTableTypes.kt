package world.gregs.voidps.cache.definition.types

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlin.math.max

object DropTableTypes : Types() {

    private val ids = Object2IntOpenHashMap<String>()
    private lateinit var drops: Array<IntArray?>
    private lateinit var type: ByteArray
    private lateinit var roll: ShortArray
    private lateinit var chance: ShortArray
    private lateinit var table: Array<String?>
    private lateinit var item: Array<String?>
    private lateinit var owns: Array<String?>
    private lateinit var lacks: Array<String?>
    private lateinit var variable: Array<String?>
    private lateinit var equals: Array<Any?>
    private lateinit var negated: ByteArray
    private lateinit var default: Array<Any?>
    private lateinit var min: IntArray
    private lateinit var max: IntArray
    private lateinit var withinMin: IntArray
    private lateinit var withinMax: IntArray
    private lateinit var members: ByteArray

    private const val SIZE = 10_000

    init {
        set(SIZE)
    }

    fun get(id: String): Int = ids.getOrDefault(id, -1)

    fun drops(id: Int) = drops[id]
    fun dropAll(id: Int) = type[id] == TABLE_TYPE_ALL
    fun roll(id: Int) = roll[id].toInt()
    fun chance(id: Int) = chance[id].toInt()
    fun table(id: Int) = table[id]
    fun item(id: Int) = item[id]
    fun owns(id: Int) = owns[id]
    fun lacks(id: Int) = lacks[id]
    fun variable(id: Int) = variable[id]
    fun eq(id: Int) = equals[id]
    fun negated(id: Int) = negated[id] == 1.toByte()
    fun default(id: Int) = default[id]
    fun min(id: Int) = min[id]
    fun max(id: Int) = max[id]
    fun withinMin(id: Int) = withinMin[id]
    fun withinMax(id: Int) = withinMax[id]
    fun members(id: Int) = members[id] == 1.toByte()

    const val TABLE_TYPE_FIRST: Byte = 0
    const val TABLE_TYPE_ALL: Byte = 1
    private const val DEFAULT_ROLL: Short = 1
    private const val DEFAULT_CHANCE: Short = -1
    private const val DEFAULT_AMOUNT = 1

    override fun set(size: Int) {
        index = 0
        drops = arrayOfNulls(size)
        type = ByteArray(size) { TABLE_TYPE_FIRST }
        roll = ShortArray(size) { DEFAULT_ROLL }
        chance = ShortArray(size) { DEFAULT_CHANCE }
        table = arrayOfNulls(size)
        item = arrayOfNulls(size)
        owns = arrayOfNulls(size)
        lacks = arrayOfNulls(size)
        variable = arrayOfNulls(size)
        equals = arrayOfNulls(size)
        default = arrayOfNulls(size)
        min = IntArray(size) { DEFAULT_AMOUNT }
        max = IntArray(size) { DEFAULT_AMOUNT }
        withinMin = IntArray(size)
        withinMax = IntArray(size)
        members = ByteArray(size)
        negated = ByteArray(size)
    }

    override fun nullAnys() = listOf(equals, default)
    override fun shorts() = listOf(roll, chance)
    override fun ints() = listOf(min, max, withinMin, withinMax)
    override fun nullStrings() = listOf(table, item, owns, lacks, variable)
    override fun bytes() = listOf(type, members, negated)
    override fun nullIntArrays() = listOf(drops)

    var index = 0
    var largest = 0

    override fun before(section: String) {
        ids[section] = index
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(key: String, value: Any, id: Int, section: String): Boolean {
        when (key) {
            "roll" -> roll[index] = (value as Int).toShort()
            "type" -> type[index] = if (value as String == "all") TABLE_TYPE_ALL else TABLE_TYPE_FIRST
            "chance" -> chance[index] = (value as Int).toShort()
            "drops" -> {
                value as List<Map<String, Any>>
                val drops = IntArray(value.size) { index + 1 + it }
                var i = index + 1
                for (drop in value) {
                    drop["table"]?.let { table[i] = it as String }
                    drop["id"]?.let { item[i] = it as String }
                    drop["chance"]?.let { chance[i] = (it as Int).toShort() }
                    (drop["amount"] ?: drop["charges"])?.let {
                        val value = it as Int
                        min[i] = value
                        max[i] = value
                    }
                    drop["min"]?.let { min[i] = it as Int }
                    drop["max"]?.let { max[i] = it as Int }
                    drop["roll"]?.let { roll[i] = (it as Int).toShort() }
                    drop["lacks"]?.let { lacks[i] = it as String }
                    drop["owns"]?.let { owns[i] = it as String }
                    drop["members"]?.let { members[i] = if (it as Boolean) 1 else 0 }
                    drop["variable"]?.let { lacks[i] = it as String }
                    drop["equals"]?.let { equals[i] = it }
                    drop["not_equals"]?.let {
                        equals[i] = it
                        negated[i] = 1
                    }
                    drop["default"]?.let { default[i] = it }
                    drop["within_min"]?.let { withinMin[i] = it as Int }
                    drop["within_max"]?.let { withinMax[i] = it as Int }
                    i++
                }
                largest = i
                this.drops[index] = drops
            }
        }
        return true
    }

    override fun after(id: Int, section: String) {
        index = max(largest, index)
    }
}
