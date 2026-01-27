package content.bot.fact

sealed class MandatoryFact(priority: Int = 0) : Fact(priority)

data class HasSkillLevel(
    val id: String,
    val min: Int = 1,
    val max: Int = 120
) : MandatoryFact()


data class HasVariable(
    val id: String,
    val value: Any? = null
) : MandatoryFact()
