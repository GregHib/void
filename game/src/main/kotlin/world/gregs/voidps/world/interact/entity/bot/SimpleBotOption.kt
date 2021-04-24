package world.gregs.voidps.world.interact.entity.bot

import world.gregs.voidps.ai.Option

open class SimpleBotOption<T : Any>(
    val name: String,
    override val targets: BotContext.() -> List<T>,
    override val considerations: List<BotContext.(T) -> Double> = listOf(),
    override val momentum: Double = 1.25,
    override val weight: Double = 1.0,
    override val action: BotContext.(T) -> Unit
) : Option<BotContext, T>