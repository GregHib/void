package content.bot.req

sealed interface MandatoryRequirement : Requirement

data class RequiresSkill(
    val id: String,
    val min: Int = 1,
    val max: Int = 120
) : MandatoryRequirement


data class RequiresVariable(
    val id: String,
    val value: Any? = null
) : MandatoryRequirement
