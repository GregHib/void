package rs.dusk.engine.entity

import com.google.common.collect.BiMap
import org.koin.dsl.module
import rs.dusk.engine.entity.character.npc.detail.NPCDetailsLoader
import rs.dusk.engine.entity.item.detail.ItemDetailsLoader
import rs.dusk.engine.entity.obj.detail.ObjectDetailsLoader

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface EntityDetails {
    val details: Map<Int, EntityDetail>
    val names: BiMap<Int, String>

    fun getOrNull(id: Int): EntityDetail?

    fun get(id: Int): EntityDetail

    fun getNameOrNull(id: Int): String? {
        return names[id]
    }

    fun getName(id: Int): String = getNameOrNull(id) ?: ""

    fun getIdOrNull(name: String): Int? {
        return names.inverse()[name]
    }

    fun getId(name: String): Int = getIdOrNull(name) ?: -1

    companion object {
        private val regex = Regex("<.*>")
        fun toIdentifier(name: String) = name.toLowerCase().replace(" ", "_").replace(regex, "").replace("\"", "")
    }
}

val detailsModule = module {
    single(createdAtStart = true) { ObjectDetailsLoader(get()).run(getProperty("objectDetailsPath")) }
    single(createdAtStart = true) { NPCDetailsLoader(get()).run(getProperty("npcDetailsPath")) }
    single(createdAtStart = true) { ItemDetailsLoader(get()).run(getProperty("itemDetailsPath")) }
}