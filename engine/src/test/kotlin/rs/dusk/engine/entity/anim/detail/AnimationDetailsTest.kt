package rs.dusk.engine.entity.anim.detail

import com.google.common.collect.BiMap
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.EntityDetailsTest

internal class AnimationDetailsTest : EntityDetailsTest<AnimationDetail, AnimationDetails>() {

    override fun map(id: Int): Map<String, Any> {
        return mapOf("id" to id)
    }

    override fun detail(id: Int): AnimationDetail {
        return AnimationDetail(id)
    }

    override fun details(id: Map<Int, AnimationDetail>, names: BiMap<Int, String>): AnimationDetails {
        return AnimationDetails(id, names)
    }

    override fun loader(loader: FileLoader): TimedLoader<AnimationDetails> {
        return AnimationDetailsLoader(loader)
    }

}