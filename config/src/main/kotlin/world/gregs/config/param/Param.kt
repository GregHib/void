package world.gregs.config.param

interface Param {
    val id: Int
    var stringId: String
    var params: Map<Int, Any>
}