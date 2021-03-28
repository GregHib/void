package world.gregs.voidps.world.interact.entity.bot

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.ai.DecisionMaker
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.utility.inject

val decisionMaker = DecisionMaker()
val players: Players by inject()
val logger = InlineLogger()

on<World, AiTick> {
    runBlocking {
        coroutineScope {
            players.forEach { bot ->
                if (bot.isBot) {
                    launch(Contexts.Updating) {
                        val last = bot.context.last
                        val decision = decisionMaker.decide(bot.context, bot.botOptions)
                        decision?.invoke()
                        if (last != decision) {
                            logger.debug {
                                bot.context.last
                                val builder = StringBuilder()
                                builder.append("Decision made: ${(decision?.option as? SimpleBotOption)?.name} ${decision?.score} ${decision?.target}").appendLine()
                                if (logger.isTraceEnabled) {
                                    for (option in bot.botOptions) {
                                        builder.append(option.name).appendLine()
                                        for (score in option.getScores(bot.context)) {
                                            builder.append(score).appendLine()
                                        }
                                    }
                                }
                                builder.toString()
                            }
                        }
                    }
                }
            }
        }
    }
}