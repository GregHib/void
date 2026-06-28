package content.skill.summoning.familiar

import content.skill.summoning.follower
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class UnicornStallion : Script {
    init {
        npcOperate("Interact", "unicorn_stallion_familiar") { (target) ->
            if (target != follower) {
                return@npcOperate
            }
            if (levels.get(Skill.Constitution) < levels.getMax(Skill.Constitution) * 0.6) {
                npc<Neutral>("Whicker snort! Whinny whinny whinny. (You're hurt! Let me try to heal you.)")
                player<Happy>("Yes, please do!")
                npc<Neutral>("Snuffle whicker whicker neigh neigh... (Okay, we'll begin with acupuncture and some reiki, then I'll get my crystals...)")
                player<Happy>("Or you could use some sort of magic...like the other unicorns...")
                npc<Neutral>("Whicker whinny whinny neigh. (Yes, but I believe in alternative medicine.)")
                player<Happy>("Riiight. Don't worry about it, then; I'll be fine.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Neigh neigh neighneigh snort? (Isn't everything so awesomely wonderful?)")
                    player<Happy>("Err...yes?")
                    npc<Neutral>("Whicker whicker snuffle. (I can see you're not tuning in, $name.)")
                    player<Happy>("No, no, I'm completely at one with...you know...everything.")
                    npc<Neutral>("Whicker! (Cosmic.)")
                }
                1 -> {
                    npc<Neutral>("Whicker whicker. Neigh, neigh, whinny. (I feel so, like, enlightened. Let's meditate and enhance our auras.)")
                    player<Happy>("I can't do that! I barely even know you.")
                    npc<Neutral>("Whicker... (Bipeds...)")
                }
                2 -> {
                    npc<Neutral>("Whinny whinny whinny. (I think I'm astrally projecting.)")
                    player<Happy>("Okay... Hang on. Seeing as I summoned you here, wouldn't that mean you are physically projecting instead?")
                    npc<Neutral>("Whicker whicker whicker. (You're, like, no fun at all, man.)")
                }
                3 -> {
                    npc<Neutral>("Whinny, neigh! (Oh, happy day!)")
                    player<Happy>("Happy day? Is that some sort of holiday or something?")
                    npc<Neutral>("Snuggle whicker (Man, you're totally, like, uncosmic, $name.)")
                }
            }
        }

        npcOperate("Cure", "unicorn_stallion_familiar") { (target) ->
            if (target != follower) {
                message("This isn't your familiar.")
                return@npcOperate
            }
            if (levels.get(Skill.Summoning) < 2) {
                message("You do not have enough summoning points to do this.")
                return@npcOperate
            }
            if (!this["poisoned", false]) {
                message("You're not suffering from poison!")
                return@npcOperate
            }
            follower?.anim("unicorn_stallion_cure")
            follower?.gfx("unicorn_stallion_cure")
            clear("poisoned")
            levels.drain(Skill.Summoning, 2)
        }
    }
}
