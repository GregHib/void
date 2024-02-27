package world.gregs.voidps.tools.convert.osrs

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.AnimationDefinitionFull
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.AnimationDecoderFull
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.tools.property
import world.gregs.yaml.Yaml
import java.io.File

object AnimationComparator {

    private class AnimWrapper(val definition: AnimationDefinitionFull) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AnimWrapper

            if (definition.durations != null) {
                if (other.definition.durations == null) return false
                if (!definition.durations.contentEquals(other.definition.durations)) return false
            } else if (other.definition.durations != null) return false
            if (definition.frames != null) {
                if (other.definition.frames == null) return false
                if (!definition.frames.contentEquals(other.definition.frames)) return false
            } else if (other.definition.frames != null) return false
            if (definition.expressionFrames != null) {
                if (other.definition.expressionFrames == null) return false
                if (!definition.expressionFrames.contentEquals(other.definition.expressionFrames)) return false
            } else if (other.definition.expressionFrames != null) return false
            return true
        }

        override fun hashCode(): Int {
            var result = (definition.durations?.contentHashCode() ?: 0)
            result = 31 * result + (definition.frames?.contentHashCode() ?: 0)
            result = 31 * result + (definition.expressionFrames?.contentHashCode() ?: 0)
            return result
        }
    }
    @JvmStatic
    fun main(args: Array<String>) {
        val osrsNames = File("./temp/osrs/mappings/animation.rscm").readLines().filter { it.contains(":") }.associate {
            val (name, id) = it.split(":")
            id.toInt() to name
        }
        val osrsCache = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\osrs-215-cache\\")
        val osrsDecoder = AnimationDecoderFullOSRS().load(osrsCache)
        val osrsAnimations = mutableMapOf<AnimWrapper, Int>()
        for (definition in osrsDecoder) {
            if (definition.frames == null && definition.expressionFrames == null) {
                continue
            }
            osrsAnimations[AnimWrapper(definition)] = definition.id
        }

        println(osrsDecoder.size)
        val cache = CacheDelegate(property("cachePath"))
        val decoder = AnimationDecoderFull().load(cache)
        val definitions = AnimationDefinitions(AnimationDecoder().load(cache)).load(Yaml(), property("animationDefinitionsPath"))
        val output = File("./temp/osrs/634-animation-names.txt")
        val outOsrs = File("./temp/osrs/osrs-animation-names.txt")

        var matches = 0
        var notNull = 0
        for (definition in decoder) {
            if (definition.frames == null && definition.expressionFrames == null) {
                continue
            }
            val wrapper = AnimWrapper(definition)
            if (osrsAnimations.containsKey(wrapper)) {
                val osrsId = osrsAnimations[wrapper]
                val def = definitions.get(definition.id)
                if (osrsNames.containsKey(osrsId)) {
                    output.appendText("${osrsNames[osrsId]}:${definition.id}\n")
                    notNull++
                } else if (def.stringId.isNotBlank()) {
                    outOsrs.appendText("${def.stringId}:$osrsId\n")
                }

                println("Found ${definition.id} = $osrsId")
                    matches++
//                }
            }
        }
        println("Matches: ${matches} $notNull")
    }
}