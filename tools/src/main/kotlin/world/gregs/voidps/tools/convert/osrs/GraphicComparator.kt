package world.gregs.voidps.tools.convert.osrs

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.tools.property
import world.gregs.yaml.Yaml
import java.io.File

object GraphicComparator {

    private class GraphicWrapper(val definition: GraphicDefinition) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GraphicWrapper

            if (definition.modelId != other.definition.modelId) return false
            if (definition.animationId != other.definition.animationId) return false
            if (definition.sizeXY != other.definition.sizeXY) return false
            if (definition.sizeZ != other.definition.sizeZ) return false
            if (definition.rotation != other.definition.rotation) return false
            if (definition.ambience != other.definition.ambience) return false
            if (definition.contrast != other.definition.contrast) return false
            if (definition.aByte2381 != other.definition.aByte2381) return false
            if (definition.anInt2385 != other.definition.anInt2385) return false
            if (definition.aBoolean2402 != other.definition.aBoolean2402) return false
            if (definition.originalColours != null) {
                if (other.definition.originalColours == null) return false
                if (!definition.originalColours.contentEquals(other.definition.originalColours)) return false
            } else if (other.definition.originalColours != null) return false
            if (definition.modifiedColours != null) {
                if (other.definition.modifiedColours == null) return false
                if (!definition.modifiedColours.contentEquals(other.definition.modifiedColours)) return false
            } else if (other.definition.modifiedColours != null) return false
            if (definition.originalTextureColours != null) {
                if (other.definition.originalTextureColours == null) return false
                if (!definition.originalTextureColours.contentEquals(other.definition.originalTextureColours)) return false
            } else if (other.definition.originalTextureColours != null) return false
            if (definition.modifiedTextureColours != null) {
                if (other.definition.modifiedTextureColours == null) return false
                if (!definition.modifiedTextureColours.contentEquals(other.definition.modifiedTextureColours)) return false
            } else if (other.definition.modifiedTextureColours != null) return false
            return true
        }

        override fun hashCode(): Int {
            var result = 0
//            result = 31 * result + definition.modelId
            result = 31 * result + definition.animationId
            result = 31 * result + definition.sizeXY
            result = 31 * result + definition.sizeZ
            result = 31 * result + definition.rotation
            result = 31 * result + definition.ambience
            result = 31 * result + definition.contrast
            result = 31 * result + definition.aByte2381
            result = 31 * result + definition.anInt2385
            result = 31 * result + definition.aBoolean2402.hashCode()
            result = 31 * result + (definition.originalColours?.contentHashCode() ?: 0)
            result = 31 * result + (definition.modifiedColours?.contentHashCode() ?: 0)
            result = 31 * result + (definition.originalTextureColours?.contentHashCode() ?: 0)
            result = 31 * result + (definition.modifiedTextureColours?.contentHashCode() ?: 0)
            return result
        }
    }
    @JvmStatic
    fun main(args: Array<String>) {
        val modelMap = File("./temp/osrs/634-models-osrs.txt").readLines().associate {
            val (osrs, rs2) = it.split(":")
            osrs.toInt() to rs2.toInt()
        }
        val osrsNames = File("./temp/osrs/mappings/graphics.rscm").readLines().filter { it.contains(":") }.associate {
            val (name, id) = it.split(":")
            id.toInt() to name
        }
        val osrsCache = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\osrs-215-cache\\")
        val osrsDecoder = GraphicDecoderOSRS().load(osrsCache)
        val osrsGraphics = mutableMapOf<GraphicWrapper, Int>()
        for (definition in osrsDecoder) {
            definition.modelId = modelMap[definition.modelId] ?: continue
            osrsGraphics[GraphicWrapper(definition)] = definition.id
        }

        println(osrsDecoder.size)
        val cache = CacheDelegate(property("cachePath"))

        val decoder = GraphicDecoder().load(cache)
        GraphicDefinitions(decoder).load(Yaml(), property("graphicDefinitionsPath"))
        val output = File("./temp/osrs/634-graphic-names.txt")
        val osrsMissing = File("./temp/osrs/osrs-graphic-names.txt")

        var matches = 0
        var named = 0
        for (definition in decoder) {
            val wrapper = GraphicWrapper(definition)
            if (osrsGraphics.containsKey(wrapper)) {
                val osrsId = osrsGraphics[wrapper]
                if (osrsNames.containsKey(osrsId)) {
                    output.appendText("${osrsNames[osrsId]}:${definition.id}\n")
                    named++
                } else if(definition.stringId.isNotBlank()) {
                    osrsMissing.appendText("${definition.stringId}:${osrsId}\n")
                }
                matches++
            }
        }
        println("Matches: $matches $named")
    }
}