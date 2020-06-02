package rs.dusk.engine.path

sealed class TraversalType(val shift: Int) {
    object Land : TraversalType(0)
    object Sky : TraversalType(9)
    object Ignored : TraversalType(22)
}