package world.gregs.voidps.engine.entity

/**
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
data class Size(val width: Int, val height: Int) {
    companion object {
        val TILE = Size(1, 1)
    }
}