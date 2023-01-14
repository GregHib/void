package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.map.Tile

object InteractDestination {
    /**
     * Calculates coordinates for [source] to move to interact with [target]
     * We first determine the cardinal direction of the source relative to the target by comparing if
     * the source lies to the left or right of diagonal \ and anti-diagonal / lines.
     * \ <= North <= /
     *  +------------+  >
     *  |            |  East
     *  +------------+  <
     * / <= South <= \
     * We then further bisect the area into three section relative to the south-west tile (zero):
     * 1. Greater than zero: follow their diagonal until the target side is reached (clamped at the furthest most tile)
     * 2. Less than zero: zero minus the size of the source
     * 3. Equal to zero: move directly towards zero / the south-west coordinate
     *
     * <  \ 0 /   <   /
     *     +---------+
     *     |         |
     *     +---------+
     * This method is equivalent to returning the last coordinate in a sequence of steps towards south-west when moving
     * ordinal then cardinally until entity side comes into contact with another.
     */
    fun calculateDestination(source: Tile, sourceWidth: Int, sourceHeight: Int, target: Tile, targetWidth: Int, targetHeight: Int): Tile {
        val diagonal = (source.x - target.x) + (source.y - target.y)
        val anti = (source.x - target.x) - (source.y - target.y)
        val southWestClockwise = anti < 0
        val northWestClockwise = diagonal >= (targetHeight - 1) - (sourceWidth - 1)
        val northEastClockwise = anti > sourceWidth - sourceHeight
        val southEastClockwise = diagonal <= (targetWidth - 1) - (sourceHeight - 1)

        if (southWestClockwise && !northWestClockwise) {
            val targetY = when { // West
                diagonal >= -sourceWidth -> (diagonal + sourceWidth).coerceAtMost(targetHeight - 1)
                anti > -sourceWidth -> -(sourceWidth + anti)
                else -> 0
            }
            return target.add(-sourceWidth, targetY)
        } else if (northWestClockwise && !northEastClockwise) {
            val targetX = when { // North
                anti >= -targetHeight -> (anti + targetHeight).coerceAtMost(targetWidth - 1)
                diagonal < targetHeight -> (diagonal - targetHeight).coerceAtLeast(-(sourceWidth - 1))
                else -> 0
            }
            return target.add(targetX, targetHeight)
        } else if (northEastClockwise && !southEastClockwise) {
            val targetY = when { // East
                anti <= targetWidth -> targetHeight - anti
                diagonal < targetWidth -> (diagonal - targetWidth).coerceAtLeast(-(sourceHeight - 1))
                else -> 0
            }
            return target.add(targetWidth, targetY)
        } else if (southEastClockwise && !southWestClockwise) {
            val targetX = when { // South
                diagonal > -sourceHeight -> (diagonal + sourceHeight).coerceAtMost(targetWidth - 1)
                anti < sourceHeight -> (anti - sourceHeight).coerceAtLeast(-(sourceHeight - 1))
                else -> 0
            }
            return target.add(targetX, -sourceHeight)
        }
        return Tile(0, 0) // Impossible
    }
}