package rs.dusk.engine.client.ui.dialogue

import rs.dusk.engine.entity.Entity

data class DialogueBuilder(
    val target: Entity,
    var text: String = "",
    var expression: Expression = Expression.Talking,
    var title: String? = null,
    var large: Boolean = false,
    var clickToContinue: Boolean = true
) {
    fun lines() = text.trimIndent().lines()
}