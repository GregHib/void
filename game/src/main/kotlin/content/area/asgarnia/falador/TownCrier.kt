package content.area.asgarnia.falador

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

class TownCrier : Script {

    /**
     * Toggle to enable historical overheads
     */
    private val enableHistoricalOverheads = false

    /**
     * Current overheads
     */
    private val currentOverheads = listOf(
        "The Grand Exchange is now open! Buy and sell items with other players!",
        "New content is being actively developed — stay tuned for updates!",
    )

    /**
     * Historical overheads (earlier 2010 announcements)
     */
    private val historicalOverheads = listOf(
        "Have you checked out the latest game updates?",
        "Have you tried Player-Owned Houses yet?",
    )

    /**
     * Helpful gameplay tips (2010-2011 era)
     */
    private val tips = listOf(
        "Beware of players trying to lure you into the wilderness. Your items cannot be returned if you lose them!",
        "Did you know having a bank pin can help you secure your valuable items?",
        "Did you know most skills have right click 'Make-X' options to help you train faster?",
        "Did you know that mithril equipment is very light?",
        "Did you know that you can wear a shield with a crossbow?",
        "Did you know you burn food less often on the range in Lumbridge castle than other ranges?",
        "Did you know? Superheat Item means you never fail to smelt ore!",
        "Did you know? You burn food less often on a range than on a fire!",
        "Don't use your ${Settings["server.name"]} password on other sites. Keep your account safe!",
        "Feeling harassed? Don't forget your ignore list can be especially useful if a player seems to be picking on you!",
        "If a player isn't sure of the rules, send them to me! I'll be happy to remind them!",
        "If the chat window is moving too quickly to report a player accurately, run to a quiet spot and review the chat at your leisure!",
        "If you see someone breaking the rules, report them!",
        "If you think someone knows your password - change it!",
        "If you're lost and have no idea where to go, use the Home Teleport spell for free!",
        "${Settings["server.company"]} will never email you asking for your log-in details.",
        "Make your recovery questions and answers hard to guess but easy to remember.",
        "Melee armour actually gives you disadvantages when using magic attacks. It may be better to take off your armour entirely!",
        "Never let anyone else use your account.",
        "Never question a penguin.",
        "Never tell your password to anyone, not even your best friend!",
        "Players cannot trim armour. Don't fall for this popular scam!",
        "The squirrels! The squirrels are coming! Noooo, get them out of my head!",
        "Take time to check the second trade window carefully. Don't be scammed!",
        "There are no cheats in RuneScape! Never visit websites promising otherwise!",
        "Summoning familiars can help you in combat and skilling!",
        "Have you tried Dungeoneering? It's a great way to train all your skills!",
    )

    private val activeOverhead: String
        get() {
            val currentDate = LocalDate.now()
            val currentMonth = currentDate.month

            return when {
                currentMonth == Month.OCTOBER -> "It's Halloween!"
                currentMonth == Month.DECEMBER -> "It's Christmas time!"
                currentMonth == Month.APRIL -> "Happy Easter!"
                else -> {
                    val availableOverheads = if (enableHistoricalOverheads) {
                        currentOverheads + historicalOverheads
                    } else {
                        currentOverheads
                    }
                    availableOverheads.random()
                }
            }
        }
    init {
        npcSpawn("town_crier_falador") {
            softTimers.start("town_crier_overhead")
        }
        npcTimerStart("town_crier_overhead") {
            TimeUnit.SECONDS.toTicks(10)
        }
        npcTimerTick("town_crier_overhead") {
            anim("bell_ring")
            say(activeOverhead)
            Timer.CONTINUE // So he repeats it
        }
        npcOperate("Talk-to", "town_crier_falador") {
            npc<Neutral>("Hear ye! Hear ye! <br>Player Moderators are a massive help to RuneScape!")
            npc<Neutral>("Oh, hello citizen. Are you here to find out about <br>Player Moderators? Or perhaps would you like to know about the laws of the land?")
            choice {
                option<Quiz>("Tell me about Player Moderators.") {
                    npc<Neutral>("Of course. What would you like to know?")
                    choice {
                        option<Quiz>("What is a Player Moderator?") {
                            npc<Neutral>("Player Moderators are normal players of the game, just like you. However, since they have shown themselves to be trustworthy and active reporters, they have been invited by Jagex to monitor the game and take appropriate")
                            npc<Neutral>("action when they see rule breaking. You can spot a Player Moderator in game by looking at the chat screen - when a Player Moderator speaks, a silver crown appears to the left of their name. Remember, if there's no silver crown")
                            npc<Neutral>("there, they are not a Player Moderator! <br>You can check out the website if you'd like more information.")
                            player<Neutral>("Thanks!")
                            npc<Neutral>("Is there anything else you'd like to know?")
                        }
                        option<Quiz>("What can Player Moderators do?") {
                            npc<Neutral>("Player Moderators, or 'P-mods', have the ability to mute rule breakers and Jagex view their reports as a priority so that action is taken as quickly as possible. P-mods also have access to the Player Moderator Centre. Within the")
                            npc<Neutral>("Centre are tools to help them Moderate RuneScape. These tools include dedicated forums, the Player Moderator Guidelines and the Player Moderator Code of Conduct.")
                            player<Neutral>("Thanks!")
                            npc<Neutral>("Is there anything else you'd like to know?")
                        }
                        option<Quiz>("How do I become a Player Moderator?") {
                            npc<Neutral>("Jagex picks players who spend their time and effort to help better the RuneScape community. To increase your chances of becoming a Player Moderator:")
                            npc<Neutral>("Keep your account secure! This is very important, as a player with poor security will never be a P-Mod. Read our Security Tips for more information.")
                            npc<Neutral>("Play by the rules! The rules of RuneScape are enforced for a reason, to make the game a fair and enjoyable environment for all.")
                            npc<Neutral>("Report accurately! When Jagex consider an account for review they look for quality, not quantity. Ensure your reports are of a high quality by following the report guidelines.")
                            npc<Neutral>("Be excellent to each other! Treat others as you would want to be treated yourself. Respect your fellow player. More information can be found on the website.")
                            player<Neutral>("Thanks!")
                            npc<Neutral>("Is there anything else you'd like to know?")
                        }
                        option<Quiz>("What can Player Moderators not do?") {
                            npc<Neutral>("P-Mods cannot ban your account - they can only report offences. Jagex then takes action based on the evidence received. If you lose your password or get scammed by another player, P-Mods cannot help you get your account")
                            npc<Neutral>("back. All they can do is recommend you go to Player Support. They cannot retrieve any items you may have lost and they certainly do not receive any free items from Jagex for moderating the game. They are players")
                            npc<Neutral>("who give their all to help the community, out of the goodness of their hearts! P-Mods do not work for Jagex and therefore cannot make you a Moderator, or recommend other accounts to become Moderators. If you wish to become a Moderator, feel free to ask me!")
                            player<Neutral>("Thanks!")
                            npc<Neutral>("Is there anything else you'd like to know?")
                        }
                        option("Something else.") {
                            // Returns to main menu
                            npc<Neutral>("Oh, hello citizen. Are you here to find out about Player Moderators? Or perhaps would you like to know about the laws of the land?")
                        }
                    }
                }
                option<Quiz>("Tell me about the Rules of RuneScape.") {
                    npc<Neutral>("At once. Take a look at my book here.")
                    // TODO: Open Rules of RuneScape book interface
                }
                option<Quiz>("Can you give me a handy tip please?") {
                    npc<Neutral>(tips.random())
                    npc<Neutral>("Is there anything else you'd like to know?")
                }
                option<Quiz>("No thanks.") {
                    npc<Neutral>("Very well. Fare thee well, citizen!")
                }
            }
        }
    }
}
