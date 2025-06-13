package world.gregs.voidps.engine.inv.remove

interface ItemAmountBounds {

    fun minimum(index: Int = -1): Int = 0

    fun maximum(index: Int = -1): Int = Int.MAX_VALUE
}
