package content.bot.interact.path

abstract class NodeTargetStrategy {
    abstract fun reached(node: Any): Boolean
}