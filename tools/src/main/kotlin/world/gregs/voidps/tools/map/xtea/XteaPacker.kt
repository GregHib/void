package world.gregs.voidps.tools.map.xtea

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.tools.cache.Xteas
import java.io.RandomAccessFile

object XteaPacker {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val xteas = Xteas().load("./xteas/") // "${System.getProperty("user.home")}\\Downloads\\rs634_cache\\634\\")
        val binary = RandomAccessFile("./xteas.dat", "rw")
        xteas.forEach { region, keys ->
            binary.writeShort(region)
            keys.forEach { key ->
                binary.writeInt(key)
            }
        }
    }
}
