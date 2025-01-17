package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.dialogue.Expression

typealias PlayerChoice = ChoiceBuilder<out Context<Player>>

class ChoiceBuilder<Context : world.gregs.voidps.engine.event.Context<Player>> {

    val values = mutableListOf<Option<Context>>()

    data class Option<Context : world.gregs.voidps.engine.event.Context<Player>>(
        val text: String,
        val filter: Context.() -> Boolean,
        val block: suspend Context.() -> Unit
    )

    /**
     * Displays option [text] when [filter] is true and if selected invokes [block]
     */
    fun option(text: String, filter: Context.() -> Boolean = { true }, block: suspend Context.() -> Unit = {}) {
        values.add(Option(text, filter, block))
    }

    /**
     * Same as [option] but also repeats [text] as player dialogue with [Expression] [E] before invoking [block]
     */
    @JvmName("optionInline")
    inline fun <reified E : Expression> option(text: String, noinline filter: Context.() -> Boolean = { true }, noinline block: suspend Context.() -> Unit = {}) {
        values.add(Option(text, filter) {
            player<E>(text)
            block.invoke(this)
        })
    }

    suspend fun invoke(index: Int, context: Context) {
        check(index in values.indices) { "Invalid choice: $index in ${values.map { it.text }}" }
        values[index].block.invoke(context)
    }

    fun build(context: Context): List<String> {
        values.removeIf { !it.filter(context) }
        return values.map { it.text }
    }
}