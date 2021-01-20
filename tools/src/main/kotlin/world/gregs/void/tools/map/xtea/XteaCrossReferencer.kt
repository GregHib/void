package world.gregs.void.tools.map.xtea

import com.displee.cache.CacheLibrary
import io.netty.buffer.ByteBuf
import world.gregs.void.engine.map.region.Region
import world.gregs.void.engine.map.region.XteaLoader
import world.gregs.void.engine.map.region.Xteas
import world.gregs.void.cache.Indices
import world.gregs.void.cache.secure.Xtea
import java.io.File
import java.io.RandomAccessFile

object XteaCrossReferencer {

    fun all(dir: String): MutableMap<Int, Xteas> {
        val file = RandomAccessFile(dir, "r")
        var count = 0
        val map = mutableMapOf<Int, Xteas>()
        while (file.filePointer < file.length()) {
            val revision = file.readUnsignedShort()
            val xteas = mutableMapOf<Int, IntArray>()
            while (true) {
                val region = file.readShort().toInt()
                if (region == -1) {
                    break
                }
                xteas[region] = IntArray(4) { file.readInt() }
            }
            map[revision] = Xteas(xteas)
            count++
        }
        file.close()
        return map
    }

    fun more(map: MutableMap<Int, Xteas>) {
        var extra = 0
        File("${System.getProperty("user.home")}\\Downloads\\rs634_cache\\xteas\\").listFiles()?.forEach {
            if (it.isDirectory) {
                map[extra++] = XteaLoader().run(it.absolutePath)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val library = CacheLibrary("${System.getProperty("user.home")}\\Downloads\\rs634_cache\\")
        val xteas = XteaLoader().run("${System.getProperty("user.home")}\\Downloads\\rs634_cache\\634\\")

        val xteasList = all("${System.getProperty("user.home")}\\Downloads\\rs634_cache\\xteas\\xteas.dat")
        more(xteasList)
        val allXteas = xteasList.flatMap { it.value.values }.distinctBy { it.toList() }.filterNot { blank(it) }
        println("All xteas ${allXteas.size}")

        val decrypted = mutableMapOf<Int, IntArray>()

        xteas.forEach { regionId, keys ->
            val region = Region(regionId)
            if (library.data(Indices.MAPS, "l${region.x}_${region.y}", keys) != null) {
                decrypted[region.id] = keys
            }
        }

        println("${decrypted.size} decrypted from original keys.")

        val index = library.index(Indices.MAPS)
        val archives = index.archiveIds()
        val missing = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val archiveId = index.archiveId("l${regionX}_${regionY}")
                if (!archives.contains(archiveId)) {
                    continue
                }
                val region = Region(regionX, regionY)
                if (decrypted.containsKey(region.id)) {
                    continue
                }
                var found = false
                for (keys in allXteas) {
                    if (isReal(library, archiveId, keys)) {
                        println("Found key ${region.id} $archiveId ${keys.toList()}")
                        decrypted[region.id] = keys
                        found = true
                        break
                    }
                }
                if (!found) {
                    missing.add(region)
                }
            }
        }
        println("Found ${decrypted.size} missing ${missing.size}")
        val dir = File("./xteas/")
        if (dir.exists()) {
            dir.delete()
        }
        dir.mkdir()
        decrypted.forEach { (region, keys) ->
            val file = File("./xteas/${region}.txt")
            file.writeText(keys.joinToString("\n"))
        }
//        File("./xteas634.txt").writeText(decrypted.joinToString("\n") { "${it.first.id}, ${it.second.joinToString()}" })
        File("./missing634.txt").writeText(missing.map { it.id }.joinToString("\n"))
    }

    fun blank(keys: IntArray) = keys[0] == 0 && keys[1] == 0 && keys[2] == 0 && keys[3] == 0

    fun isReal(library: CacheLibrary, archiveId: Int, keys: IntArray): Boolean {
        return library.data(Indices.MAPS, archiveId, 0, keys) != null
    }

    fun isValid(buffer: ByteBuf, sector: ByteArray, keys: IntArray): Boolean {
        buffer.readerIndex(0)
        buffer.writerIndex(0)
        buffer.writeBytes(sector)
        buffer.readerIndex(0)
        if (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || keys[3] != 0) {
            Xtea.decipher(buffer, keys, 5)
        }
        return when (buffer.readUnsignedByte().toInt()) {
            GZIP -> buffer.getByte(9).toInt() == 31 && buffer.getByte(10).toInt() == -117
            else -> false
        }
    }

    private const val NONE = 0
    private const val BZIP2 = 1
    private const val GZIP = 2
    private const val LZMA = 3

}