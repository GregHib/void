package world.gregs.voidps.tools.convert.osrs

import io.netty.buffer.Unpooled
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.property
import java.io.File

object ModelComparator {

    private class ModelWrapper(val model: Model) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ModelWrapper

            if (model.triangleVertex1 != null) {
                if (other.model.triangleVertex1 == null) return false
                if (!model.triangleVertex1.contentEquals(other.model.triangleVertex1)) return false
            } else if (other.model.triangleVertex1 != null) return false
            if (model.triangleVertex2 != null) {
                if (other.model.triangleVertex2 == null) return false
                if (!model.triangleVertex2.contentEquals(other.model.triangleVertex2)) return false
            } else if (other.model.triangleVertex2 != null) return false
            if (model.triangleVertex3 != null) {
                if (other.model.triangleVertex3 == null) return false
                if (!model.triangleVertex3.contentEquals(other.model.triangleVertex3)) return false
            } else if (other.model.triangleVertex3 != null) return false
            if (model.vertexPositionsX != null) {
                if (other.model.vertexPositionsX == null) return false
                if (!model.vertexPositionsX.contentEquals(other.model.vertexPositionsX)) return false
            } else if (other.model.vertexPositionsX != null) return false
            if (model.vertexPositionsY != null) {
                if (other.model.vertexPositionsY == null) return false
                if (!model.vertexPositionsY.contentEquals(other.model.vertexPositionsY)) return false
            } else if (other.model.vertexPositionsY != null) return false
            if (model.vertexPositionsZ != null) {
                if (other.model.vertexPositionsZ == null) return false
                if (!model.vertexPositionsZ.contentEquals(other.model.vertexPositionsZ)) return false
            } else if (other.model.vertexPositionsZ != null) return false
            return true
        }

        override fun hashCode(): Int {
            var result = 0
            result = 31 * result + model.vertexCount
            result = 31 * result + model.triangleCount
            result = 31 * result + (model.triangleVertex1?.contentHashCode() ?: 0)
            result = 31 * result + (model.triangleVertex2?.contentHashCode() ?: 0)
            result = 31 * result + (model.triangleVertex3?.contentHashCode() ?: 0)
            result = 31 * result + (model.vertexPositionsX?.contentHashCode() ?: 0)
            result = 31 * result + (model.vertexPositionsY?.contentHashCode() ?: 0)
            result = 31 * result + (model.vertexPositionsZ?.contentHashCode() ?: 0)
            return result
        }
    }
    @JvmStatic
    fun main(args: Array<String>) {
        val osrsCache = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\osrs-215-cache\\")
        val rs2Cache = CacheDelegate(property("cachePath"))
        val out = File("./temp/osrs/634-models-osrs.txt")

        val osrsModels = mutableMapOf<ModelWrapper, Int>()
        val index = Index.MODELS
        for (archive in osrsCache.archives(index)) {
            for (file in osrsCache.files(index, archive)) {
                val data = osrsCache.data(index, archive, file) ?: continue
                val model = Model.decode(file, Unpooled.wrappedBuffer(data), MeshDecodingOption.PreserveOriginalData, MeshDecodingOption.ScaleVersionedMesh)
                model.removeSkeletalInformation()
                model.removeTextures()
                osrsModels[ModelWrapper(model)] = archive
//                println("Model $archive $file ${model.type}")
            }
        }

        println("Comparing")
        var matches = 0
        for (archive in rs2Cache.archives(index)) {
            for (file in rs2Cache.files(index, archive)) {
                val data = rs2Cache.data(index, archive, file) ?: continue
                val model = Model.decode(file, Unpooled.wrappedBuffer(data), MeshDecodingOption.PreserveOriginalData, MeshDecodingOption.ScaleVersionedMesh)
                model.removeTextures()
                val wrapper = ModelWrapper(model)
                if (osrsModels.containsKey(wrapper)) {
                    out.appendText("${osrsModels[wrapper]}:${archive}\n")
                    println("Found $archive:${osrsModels[wrapper]}")
                    matches++
                }
            }
        }
        println("Matches: $matches")
    }
}