package content.area.kandarin.ardougne.west_ardougne.temple_of_light

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

/**
 * Scotty and Sanchez, the S&S cleaners working in the Temple of Light after Within the Light.
 * https://runescape.wiki/w/Transcript:Scotty
 * https://runescape.wiki/w/Transcript:Sanchez
 */
class TempleOfLightCleaners : Script {

    init {
        npcOperate("Talk-to", "scotty_temple_of_light_2") {
            player<Happy>("Hello, you look new here. Welcome to the Death Guard!")
            npc<Neutral>("Death Guard? Nah, mate, I'm just a hired hand - a cleaner, ya know?")
            player<Quiz>("You're not...you know?")
            npc<Quiz>("...")
            player<Quiz>("You're not an elf under that gasmask?")
            npc<Neutral>("Elf? Nah, mate: human. You're talking like there's some sort of mass conspiracy going on.")
            player<Quiz>("Would you believe me if I said there was? What if you could take that gasmask off and be completely safe?")
            npc<Laugh>("Nice try! 'Hello, you look new here' - that ol' routine, eh? 'Take yer mask off,' you say. 'It's perfectly safe,' you say. Then, 'BOOM', Scotty gets the plague...and you'll steal my business!")
            player<Quiz>("You think I'd try to kill you just to become the new janitor?")
            npc<Shifty>("Of course! Everyone's trying to move in on us! You shoulda seen the fiasco when we were working some waiting rooms for a guy called Mandrith. Every other guy was coming at us with a dragon dagger!")
            player<Quiz>("Yet you live to tell the tale?")
            npc<Happy>("What can I say? This old broom's as lethal on people as it is on trash.")
        }

        npcOperate("Talk-to", "sanchez_temple_of_light_2") {
            npc<Happy>("What's up, mate?")
            player<Quiz>("You look new here, have I seen you before?")
            npc<Neutral>("Maybe, me and my bruv work all over the world - global cleaning business, global problems.")
            player<Quiz>("Global problems? What's that meant to mean?")
            npc<Neutral>("Oh, come on, mate, you must have travelled a bit? Surely you've seen the amount of stuff people just drop on the floor.")
            player<Neutral>("Hmm, I guess you have a point.")
            npc<Happy>("Yup, so me and my bruv' Scotty formed S&S cleaners. Got picked up right away by a guy called Mandrith - payed us 76k an hour, if you'd believe that? Turns out he relocated, though, so we ended up here.")
            player<Happy>("Good to hear it's working out for you.")
            npc<Happy>("Oh, yeah, totally. I'd better get back to it, in fact.")
        }
    }
}
