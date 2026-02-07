package content.bot.fact

sealed interface Value<T> {
    fun resolve(context: Map<String, Any>): T
}

data class Literal<T>(val value: T)

data class Ref<T>(val key: String)