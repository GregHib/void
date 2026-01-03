package content.entity.player.dialogue.type

import content.entity.player.dialogue.Expression
import world.gregs.voidps.engine.entity.character.player.Player

class ChoiceOption {

    val strings = mutableListOf<String>()
    val options = mutableListOf<suspend Player.() -> Unit>()

    /**
     * Displays option [text] when [filter] is true and if selected invokes [block]
     */
    fun option(text: String, block: suspend Player.() -> Unit = {}) {
        strings.add(text)
        options.add(block)
    }

    /**
     * Same as [option] but also repeats [text] as player dialogue with [Expression] [E] before invoking [block]
     */
    @JvmName("optionInline")
    inline fun <reified E : Expression> option(text: String, noinline block: suspend Player.() -> Unit = {}) {
        strings.add(text)
        options.add {
            player<E>(text)
            block.invoke(this)
        }
    }

    suspend fun invoke(index: Int, context: Player) {
        check(index in options.indices) { "Invalid choice: $index in $strings" }
        options[index].invoke(context)
    }

    fun build(): List<String> = strings
}
