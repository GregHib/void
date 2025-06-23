package world.gregs.voidps.tools.map.render.load

object MapConstants {

    val TILE_TYPE_HEIGHT_OVERRIDE = arrayOf(
        booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true, true, true),
        booleanArrayOf(true, true, true, false, false, false, true, true, false, false, false, false, true),
        booleanArrayOf(true, false, false, false, false, true, true, true, false, false, false, false, false),
        booleanArrayOf(false, false, true, true, true, true, false, false, false, false, false, false, false),
        booleanArrayOf(true, true, true, true, true, true, false, false, false, false, false, false, false),
        booleanArrayOf(true, true, true, false, false, true, true, true, false, false, false, false, false),
        booleanArrayOf(true, true, false, false, false, true, true, true, false, false, false, false, true),
        booleanArrayOf(true, true, false, false, false, false, false, true, false, false, false, false, false),
        booleanArrayOf(false, true, true, true, true, true, true, true, false, false, false, false, false),
        booleanArrayOf(true, false, false, false, true, true, true, true, true, true, false, false, false),
        booleanArrayOf(true, true, true, true, true, false, false, false, true, true, false, false, false),
        booleanArrayOf(true, true, true, false, false, false, false, false, false, false, true, true, false),
        BooleanArray(13),
        booleanArrayOf(true, true, true, true, true, true, true, true, true, true, true, true, true),
        BooleanArray(13),
    )

    const val SIZE = 4

    const val TILE_WATER = false
    const val TILE_LIGHTING = false
    const val SCENERY_SHADOWS = -1
    const val GROUND_BLENDING = -1
    const val A_BOOLEAN_8715 = true
    const val A_BOOLEAN_10563 = false
    const val WATER_MOVEMENT = false // method2810

    val tileYOffsets = intArrayOf(0, 0, 0, 256, 512, 512, 512, 256, 256, 384, 128, 128, 256)
    val tileXOffsets = intArrayOf(0, 256, 512, 512, 512, 256, 0, 0, 128, 256, 128, 384, 256)

    val firstTileTypeVertices = arrayOf(
        intArrayOf(0, 2),
        intArrayOf(0, 2),
        intArrayOf(0, 0, 2),
        intArrayOf(2, 0, 0),
        intArrayOf(0, 2, 0),
        intArrayOf(0, 0, 2),
        intArrayOf(0, 5, 1, 4),
        intArrayOf(0, 4, 4, 4),
        intArrayOf(4, 4, 4, 0),
        intArrayOf(6, 6, 6, 2, 2, 2),
        intArrayOf(2, 2, 2, 6, 6, 6),
        intArrayOf(0, 11, 6, 6, 6, 4),
        intArrayOf(0, 2),
        intArrayOf(0, 4, 4, 4),
        intArrayOf(0, 4, 4, 4),
    )
    val thirdTileTypeVertices = arrayOf(
        intArrayOf(6, 6),
        intArrayOf(6, 6),
        intArrayOf(6, 5, 5),
        intArrayOf(5, 6, 5),
        intArrayOf(5, 5, 6),
        intArrayOf(6, 5, 5),
        intArrayOf(5, 0, 4, 1),
        intArrayOf(7, 7, 1, 2),
        intArrayOf(7, 1, 2, 7),
        intArrayOf(8, 9, 4, 0, 8, 9),
        intArrayOf(0, 8, 9, 8, 9, 4),
        intArrayOf(11, 0, 10, 11, 4, 2),
        intArrayOf(6, 6),
        intArrayOf(7, 7, 1, 2),
        intArrayOf(7, 7, 1, 2),
    )
    val secondTileTypeVertices = arrayOf(
        intArrayOf(2, 4),
        intArrayOf(2, 4),
        intArrayOf(5, 2, 4),
        intArrayOf(4, 5, 2),
        intArrayOf(2, 4, 5),
        intArrayOf(5, 2, 4),
        intArrayOf(1, 6, 2, 5),
        intArrayOf(1, 6, 7, 1),
        intArrayOf(6, 7, 1, 1),
        intArrayOf(0, 8, 9, 8, 9, 4),
        intArrayOf(8, 9, 4, 0, 8, 9),
        intArrayOf(2, 10, 0, 10, 11, 11),
        intArrayOf(2, 4),
        intArrayOf(1, 6, 7, 1),
        intArrayOf(1, 6, 7, 1),
    )
    val underlaySizes = intArrayOf(0, 1, 2, 2, 1, 1, 2, 3, 1, 3, 3, 4, 2, 0, 4)
    val overlaySizes = intArrayOf(2, 1, 1, 1, 2, 2, 2, 1, 3, 3, 3, 2, 0, 4, 0)
}
