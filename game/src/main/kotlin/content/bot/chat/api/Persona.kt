package content.bot.chat.api

import kotlin.random.Random

/**
 * Personality knobs, all 0..1. Changes which of several valid replies a bot picks, not whether it's correct.
 */
data class Persona(
    // Warm replies vs curt ones
    val friendliness: Float = 0.5f,
    // Likelihood of agreeing to request's
    val helpfulness: Float = 0.5f,
    // Replies when not directly addressed and to small talk
    val chattiness: Float = 0.5f,
    // "lol", "np", "yw" vs "Haha!", "No problem.", "You're welcome."
    val slang: Float = 0.5f,
    // Number of rude or repeated messages tolerated before ignoring
    val patience: Int = 3,
) {
    companion object {
        /**
         * Stable per account so a bot keeps the same personality between logins without being saved
         */
        fun of(name: String): Persona {
            val random = Random(name.hashCode())
            return Persona(
                friendliness = random.nextFloat(),
                helpfulness = random.nextFloat(),
                chattiness = 0.2f + random.nextFloat() * 0.6f,
                slang = random.nextFloat(),
                patience = random.nextInt(1, 5),
            )
        }
    }
}
