package world.gregs.voidps.tools.map.xtea

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.cache.Indices
import java.io.File
import java.io.RandomAccessFile

object XteaPacker {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val xteas = XteaLoader().run("./xteas/")//"${System.getProperty("user.home")}\\Downloads\\rs634_cache\\634\\")
        val binary = RandomAccessFile("./xteas.dat", "rw")
        xteas.forEach { region, keys ->
            binary.writeShort(region)
            keys.forEach { key ->
                binary.writeInt(key)
            }
        }
    }
}