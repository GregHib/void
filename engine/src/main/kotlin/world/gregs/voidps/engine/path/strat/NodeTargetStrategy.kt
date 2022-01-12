package world.gregs.voidps.engine.path.strat

abstract class NodeTargetStrategy {
    abstract fun reached(node: Any): Boolean
}