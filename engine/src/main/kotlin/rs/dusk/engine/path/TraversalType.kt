package rs.dusk.engine.path

enum class TraversalType(val shift: Int) {
    Land(0),
    Sky(9),
    Ignored(22);
}