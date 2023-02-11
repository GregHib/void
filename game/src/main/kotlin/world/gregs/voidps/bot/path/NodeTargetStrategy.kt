package world.gregs.voidps.bot.path

abstract class NodeTargetStrategy {
    abstract fun reached(node: Any): Boolean
}