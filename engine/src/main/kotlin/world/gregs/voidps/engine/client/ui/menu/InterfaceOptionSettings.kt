package world.gregs.voidps.engine.client.ui.menu

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
        for (bit in 1..31) {
            if (hash ushr bit and 1 != 0) {
                list.add(bit - 1)
            }
        }
        return list
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(getIndices(2425982))
    }
}
