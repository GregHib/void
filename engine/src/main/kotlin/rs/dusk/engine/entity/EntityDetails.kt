package rs.dusk.engine.entity

import com.google.common.collect.BiMap
import org.koin.dsl.module
import rs.dusk.engine.entity.anim.detail.AnimationDetailsLoader
import rs.dusk.engine.entity.character.contain.detail.ContainerDetailsLoader
import rs.dusk.engine.entity.character.npc.detail.NPCDetailsLoader
import rs.dusk.engine.entity.gfx.detail.GraphicDetailsLoader
import rs.dusk.engine.entity.item.detail.ItemDetailsLoader
import rs.dusk.engine.entity.obj.detail.ObjectDetailsLoader

/**
 * Stores additional static information about an entity as well as a unique string identifier
 */
interface EntityDetails<T : EntityDetail> {
    val details: Map<Int, T>
    val names: BiMap<Int, String>

    fun getOrNull(id: Int): T?

    fun getOrNull(name: String): T? {
        return getOrNull(getIdOrNull(name) ?: return null)
    }

    fun get(id: Int): T

    fun get(name: String): T = get(getId(name))

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
        fun toIdentifier(name: String) = name.toLowerCase().replace(" ", "_").replace(regex, "").replace("\"", "").replace("\'", "").replace(",", "").replace("(", "").replace(")", "")
    }
}

val detailsModule = module {
    single(createdAtStart = true) { ObjectDetailsLoader(get()).run(getProperty("objectDetailsPath")) }
    single(createdAtStart = true) { NPCDetailsLoader(get()).run(getProperty("npcDetailsPath")) }
    single(createdAtStart = true) { ItemDetailsLoader(get(), get()).run(getProperty("itemDetailsPath")) }
    single(createdAtStart = true) { AnimationDetailsLoader(get()).run(getProperty("animationDetailsPath")) }
    single(createdAtStart = true) { GraphicDetailsLoader(get()).run(getProperty("graphicDetailsPath")) }
    single(createdAtStart = true) { ContainerDetailsLoader(get()).run(getProperty("containerDetailsPath")) }
}