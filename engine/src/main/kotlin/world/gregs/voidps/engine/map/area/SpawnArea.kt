package world.gregs.voidps.engine.map.area

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import world.gregs.voidps.engine.entity.Direction

@JsonDeserialize(builder = SpawnArea.Builder::class)
data class SpawnArea(
    val name: String,
    val area: Area,
    val spawns: List<Spawn>,
    val delay: Int = 0
) {

    data class Spawn(
        val name: String,
        val weight: Int = 1,
        val limit: Int = 1,
        val amount: Int = 1,
        val delay: Int = 0,
        val direction: Direction = Direction.NONE
    )

    data class Builder(
        val name: String,
        val area: SpawnArea,
        val spawns: List<Spawn>,
        val delay: Int = 60
    ) {

        fun build() = SpawnArea(
            name = name,
            area = area.let {
                if (it.x.size <= 2) {
                    Rectangle(it.x.first(), it.y.first(), it.x.last(), it.y.last(), it.plane)
                } else {
                    Polygon(it.x, it.y, it.plane)
                }
            },
            spawns = spawns,
            delay = delay
        )

        data class SpawnArea(val x: IntArray, val y: IntArray, val plane: Int = 0) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as SpawnArea

                if (!x.contentEquals(other.x)) return false
                if (!y.contentEquals(other.y)) return false
                if (plane != other.plane) return false

                return true
            }

            override fun hashCode(): Int {
                var result = x.contentHashCode()
                result = 31 * result + y.contentHashCode()
                result = 31 * result + plane
                return result
            }
        }
    }
}