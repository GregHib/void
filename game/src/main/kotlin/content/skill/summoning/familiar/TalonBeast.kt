package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class TalonBeast : Script {
    init {
        npcOperate("Interact", "talon_beast_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Is this all you apes do all day, then?")
                    player<Happy>("Well, we do a lot of other things, too.")
                    npc<Neutral>("That’s dull. Lets go find something and bite it.")
                    player<Happy>("I wouldn’t want to spoil my dinner.")
                    npc<Neutral>("So, I have to watch you trudge about again? Talk about boring.")
                }
                1 -> {
                    npc<Neutral>("This place smells odd…")
                    player<Happy>("Odd?")
                    npc<Neutral>("Yes, not enough is rotting…")
                    player<Happy>("For which I am extremely grateful.")
                }
                2 -> {
                    npc<Neutral>("Hey!")
                    player<Happy>("Aaaargh!")
                    npc<Neutral>("Why d’you always do that?")
                    player<Happy>("I don’t think I’ll ever get used to having a huge, ravenous feline sneaking around behind me all the time.")
                    npc<Neutral>("That’s okay, I doubt I’ll get used to following an edible, furless monkey prancing in front of me all the time either.")
                }
                3 -> {
                    npc<Neutral>("C’mon! Lets go fight stuff!")
                    player<Happy>("What sort of stuff?")
                    npc<Neutral>("I dunno? Giants, monsters, vaguely-defined philosophical concepts. You know: stuff.")
                    player<Happy>("How are we supposed to fight a philosophical concept?")
                    npc<Neutral>("With subtle arguments and pointy sticks!")
                    player<Happy>("Well, I can see you’re going to go far in debates.")
                }
            }
        }
    }
}
