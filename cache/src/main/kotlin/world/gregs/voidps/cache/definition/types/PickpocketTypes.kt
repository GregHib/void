package world.gregs.voidps.cache.definition.types

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap

object PickpocketTypes : Types() {

    private val indices = Int2IntOpenHashMap()
    private lateinit var level: IntArray
    private lateinit var xp: DoubleArray
    private lateinit var stunHitMin: ByteArray
    private lateinit var stunHitMax: ByteArray
    private lateinit var stunTicks: ByteArray
    private lateinit var chanceMin: ByteArray
    private lateinit var chanceMax: ByteArray
    private lateinit var caught: Array<String?>
    private lateinit var dropTable: Array<String?>

    private const val SIZE = 55
    init {
        indices.defaultReturnValue(-1)
        set(SIZE)
    }
    fun index(id: Int) = indices.get(id)

    fun level(index: Int) = level[index]
    fun xp(index: Int) = xp[index]
    fun stunHit(index: Int) = stunHitMin[index]..stunHitMax[index]
    fun stunTicks(index: Int) = stunTicks[index]
    fun chance(index: Int) = chanceMin[index] until chanceMax[index]
    fun caughtMessage(index: Int) = caught[index] ?: DEFAULT_MESSAGE
    fun table(index: Int) = dropTable[index]

    private const val DEFAULT_LEVEL = 1
    private const val DEFAULT_XP = 0.0
    private const val DEFAULT_HIT_MIN = 0.toByte()
    private const val DEFAULT_HIT_MAX = 0.toByte()
    private const val DEFAULT_TICKS = 1.toByte()
    private const val DEFAULT_CHANCE_MIN = 1.toByte()
    private const val DEFAULT_CHANCE_MAX = 1.toByte()
    private const val DEFAULT_MESSAGE = "What do you think you're doing?"

    override fun set(size: Int) {
        index = 0
        level = IntArray(size) { DEFAULT_LEVEL }
        xp = DoubleArray(size) { DEFAULT_XP }
        stunHitMin = ByteArray(size) { DEFAULT_HIT_MIN }
        stunHitMax = ByteArray(size) { DEFAULT_HIT_MAX }
        stunTicks = ByteArray(size) { DEFAULT_TICKS }
        chanceMin = ByteArray(size) { DEFAULT_CHANCE_MIN }
        chanceMax = ByteArray(size) { DEFAULT_CHANCE_MAX }
        caught = arrayOfNulls(size)
        dropTable = arrayOfNulls(size)
    }

    override fun ints() = listOf(level)
    override fun doubles() = listOf(xp)
    override fun bytes() = listOf(stunHitMin, stunHitMax, stunTicks, chanceMin, chanceMax)
    override fun nullStrings() = listOf(caught, dropTable)

    var index = 0

    override fun load(key: String, value: Any, id: Int, section: String): Boolean {
        if (key != "pickpocket") {
            return false
        }
        val map = value as Map<String, Any>
        map["level"]?.let { level[index] = it as Int }
        map["xp"]?.let { xp[index] = it as Double }
        map["stun_hit"]?.let {
            val value = (it as Int).toByte()
            stunHitMin[index] = value
            stunHitMax[index] = value
        }
        map["stun_hit_min"]?.let { stunHitMin[index] = (it as Int).toByte() }
        map["stun_hit_max"]?.let { stunHitMax[index] = (it as Int).toByte() }
        map["stun_ticks"]?.let { stunTicks[index] = (it as Int).toByte() }
        map["stun_chance_min"]?.let { chanceMin[index] = (it as Int).toByte() }
        map["stun_chance_max"]?.let { chanceMax[index] = (it as Int).toByte() }
        map["caught_message"]?.let { caught[index] = (it as String) }
        map["table"]?.let { dropTable[index] = (it as String) }
        return true
    }

    override fun after(id: Int, section: String) {
        indices.put(id, index++)
    }
}