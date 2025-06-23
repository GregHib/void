package world.gregs.voidps.engine.client.ui.menu

import kotlin.math.pow

object InterfaceOptionSettings {
    fun getHash(vararg indices: Int): Int {
        var settings = 0
        for (slot in indices) {
            settings += (2 shl slot)
        }
        return settings
    }

    fun getIndices(hash: Int): List<Int> {
        val list = mutableListOf<Int>()
        var remainder = hash
        var index = -1
        while (remainder > 0) {
            val power = 2.0.pow(index + 1).toInt()
            if (hash and power != 0) {
                remainder -= 2 shl index
                list.add(index)
            }
            index++
        }
        return list
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(getIndices(2425982))
    }
}
