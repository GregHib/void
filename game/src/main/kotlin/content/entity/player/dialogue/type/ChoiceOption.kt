package content.entity.player.dialogue.type

import content.entity.player.dialogue.Expression
import world.gregs.voidps.engine.entity.character.player.Player

class ChoiceOption {

    val values = mutableListOf<Option>()

    data class Option(
        val text: String,
        val block: suspend Player.() -> Unit,
    )

    /**
     * Displays option [text] when [filter] is true and if selected invokes [block]
     */
    fun option(text: String, block: suspend Player.() -> Unit = {}) {
        values.add(Option(text, block))
    }

    /**
     * Same as [option] but also repeats [text] as player dialogue with [Expression] [E] before invoking [block]
     */
    @JvmName("optionInline")
    inline fun <reified E : Expression> option(text: String, noinline block: suspend Player.() -> Unit = {}) {
        values.add(
            Option(text) {
                player<E>(text)
                block.invoke(this)
            },
        )
    }

    suspend fun invoke(index: Int, context: Player) {
        check(index in values.indices) { "Invalid choice: $index in ${values.map { it.text }}" }
        values[index].block.invoke(context)
    }

    fun build(): List<String> = values.map { it.text }
}
