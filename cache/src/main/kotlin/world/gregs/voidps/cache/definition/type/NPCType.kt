package world.gregs.voidps.cache.definition.type

import world.gregs.voidps.cache.definition.types.NPCTypes

@Suppress("UNCHECKED_CAST")
@JvmInline
value class NPCType(val id: Int) {
    val stringId: String
        get() = NPCTypes.id(id)
    val name: String
        get() = NPCTypes.name(id)
    val size: Int
        get() = NPCTypes.size(id)
    val options: Array<String?>
        get() = NPCTypes.options(id)
    val combat: Int
        get() = NPCTypes.combat(id)
    val varbit: Int
        get() = NPCTypes.varbit(id)
    val varp: Int
        get() = NPCTypes.varp(id)
    val transforms: IntArray?
        get() = NPCTypes.transforms(id)
    val walkMode: Int
        get() = NPCTypes.walkMode(id)
    val renderEmote: Int
        get() = NPCTypes.renderEmote(id)
    val idleSound: Int
        get() = NPCTypes.idleSound(id)
    val crawlSound: Int
        get() = NPCTypes.crawlSound(id)
    val walkSound: Int
        get() = NPCTypes.walkSound(id)
    val runSound: Int
        get() = NPCTypes.runSound(id)
    val soundDistance: Int
        get() = NPCTypes.soundDistance(id)
    val extras: Map<String, Any>?
        get() = NPCTypes.extras(id)

    operator fun <T : Any> get(key: String): T = extras!!.getValue(key) as T
    fun contains(key: String?) = extras?.containsKey(key) ?: false
    fun <T : Any> getOrNull(key: String) = extras?.get(key) as? T
    operator fun <T : Any> get(key: String, defaultValue: T) = getOrNull(key) as? T ?: defaultValue

    companion object {
        val EMPTY = NPCType(0)
    }
}