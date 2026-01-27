package content.bot.req

sealed class MandatoryRequirement(priority: Int) : Requirement(priority)

data class RequiresSkill(
    val id: String,
    val min: Int = 1,
    val max: Int = 120
) : MandatoryRequirement(0)


data class RequiresVariable(
    val id: String,
    val value: Any? = null
) : MandatoryRequirement(0)
