package world.gregs.voidps.type

interface Coordinate3D<T> {

    val x: Int
    val y: Int
    val level: Int

    fun copy(x: Int = this.x, y: Int = this.y, level: Int = this.level): T

    fun add(x: Int = 0, y: Int = 0, level: Int = 0) = copy(this.x + x, this.y + y, this.level + level)
    fun minus(x: Int = 0, y: Int = 0, level: Int = 0) = add(-x, -y, -level)
    fun delta(x: Int = 0, y: Int = 0, level: Int = 0) = Delta(this.x - x, this.y - y, this.level - level)

    fun add(value: Coordinate3D<*>) = add(value.x, value.y, value.level)
    fun minus(value: Coordinate3D<*>) = minus(value.x, value.y, value.level)
    fun delta(value: Coordinate3D<*>) = delta(value.x, value.y, value.level)

    fun add(direction: Direction) = add(direction.delta)
    fun minus(direction: Direction) = minus(direction.delta)
    fun delta(direction: Direction) = minus(direction.delta)

    fun addX(value: Int) = add(value, 0, 0)
    fun addY(value: Int) = add(0, value, 0)
    fun addLevel(value: Int) = add(0, 0, value)
}
