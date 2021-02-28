package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.path.TargetStrategy

abstract class NodeTargetStrategy : TargetStrategy {
    abstract fun reached(node: Any): Boolean
}