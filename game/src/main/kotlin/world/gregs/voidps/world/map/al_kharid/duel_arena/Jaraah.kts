package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.turn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

on<NPCOption>({ npc.id == "jaraah" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        player("cheerful", "Hi!")
        npc("angry", "What? Can't you see I'm busy?!")
        val choice = choice("""
            Can you heal me?
            You must see some gruesome things?
            Why do they call you 'The Butcher'?
        """)
        when (choice) {
            1 -> {
                player("uncertain", "Can you heal me?")
                val heal = player.levels.getMax(Skill.Constitution)
                if (player.levels.get(Skill.Constitution) < heal) {
                    npc.setAnimation("pick_pocket")
                    player.playSound("heal")
                    player.levels.restore(Skill.Constitution, heal)
                    player.message("You feel a little better.")
                    return@talkWith
                }
                player.talkWith(npc) {
                    npc("cheerful", "You look healthy to me!")
                }
            }
            2 -> {
                player("uncertain", "You must see some gruesome things?")
                npc("angry", """
                    It's a gruesome business and with the tools they give
                    me it gets more gruesome before it gets better!
                """)
                player("laugh", "Really?")
                npc("laugh", "It beats being stuck in the monastery!")
            }
            3 -> {
                player("uncertain", "Why do they call you 'The Butcher'?")
                npc("laugh", "'The Butcher'?")
                npc("angry", "Ha!")
                npc("angry", "Would you like me to demonstrate?")
                player("surprised", "Er...I'll give it a miss, thanks.")
            }
        }
    }
}

on<NPCOption>({ npc.id == "jaraah" && option == "Heal" }) { player: Player ->
    npc.turn(player)
    val heal = player.levels.getMax(Skill.Constitution)
    if (player.levels.get(Skill.Constitution) < heal) {
        npc.setAnimation("pick_pocket")
        player.playSound("heal")
        player.levels.restore(Skill.Constitution, heal)
        player.message("You feel a little better.")
        return@on
    }
    player.talkWith(npc) {
        npc("cheerful", "You look healthy to me!")
    }
}