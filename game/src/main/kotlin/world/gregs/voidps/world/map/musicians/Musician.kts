package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && npc.id.startsWith("musician") && option == "Talk-to" }) { player: Player ->
    choice()
}

suspend fun Interaction.choice() {
    choice {
        option<Unsure>("Who are you?") {
            npc<Cheerful>("""
                Me? I'm a musician Let me help you relax: sit down,
                rest your weary limbs and allow me to wash away the
                troubles of the day.
            """)
            npc<Cheerful>("""
                After a long trek, what could be better than some
                music to give you the energy to continue? 
            """)
            choice()
        }
        option("Can I ask you some questions about resting?") {
            resting()
        }
        exit()
    }
}

suspend fun Interaction.resting() {
    choice("Can I ask you some questions about resting?") {
        option("How does resting work?") {
            player<Unsure>("So how does resting work?")
            npc<Cheerful>("""
                Have you ever been on a long journey, and simply
                wanted to have a rest? When you're running from
                city to city, it's so easy to run out of breath, don't you
                find?
            """)
            player<Unsure>("Yes, I can never run as far as I'd like.")
            npc<Cheerful>("""
                Well, you may rest anywhere, simply choose the Rest
                option on the run buttons.
            """)
            npc<Cheerful>("""
                When you are nice and relaxed, you will recharge your
                run energy more quickly and your life points twice as fast
                as you would do so normally.
            """)
            npc<Talk>("""
                Of course, you can't do anything else while you're
                resting, other than talk.
            """)
            player<Unsure>("Why not?")
            npc<Cheerful>("""
                Well, you wouldn't be resting, now would you?
                Also, you should know that resting by a musician, has
                a similar effect but the benefits are greater.
            """)
            resting()
        }
        option<Happy>("What's special about resting by a musician?") {
            npc<Cheerful>("""
                The effects of resting are enhanced by music. Your
                run energy will recharge many times the normal rate,
                and your life points three times as fast.
            """)
            npc<Cheerful>("""
                Simply sit down and rest as you would normally, nice
                and close to the musician. You'll turn to face the
                musician and hear the music. Like resting anywhere, if
                you do anything other than talk, you will stop resting.
            """)
            resting()
        }
        option<Happy>("Can you summarise the effects for me?") {
            npc<Cheerful>("""
                Certainly. You can rest anywhere, simply choose the Rest
                option on the run buttons.
            """)
            npc<Cheerful>("""
                Resting anywhere will replenish your run energy more
                quickly than normal, your life points will replenish
                twice as fast as well! 
            """)
            npc<Cheerful>("""
                Resting by a musician will replenish your run energy
                many times faster than normal, and your life points will
                also replenish three times as fast.
            """)
            resting()
        }
        exit()
    }
}

suspend fun PlayerChoice.exit(): Unit = option<Unsure>("That's all for now.") {
    npc<Cheerful>("Well, don't forget to have a rest every now and again.")
}