package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playSound

on<NPCOption>({ npc.id == "surgeon_general_tafani" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        player("cheerful", "Hi!")
        npc("cheerful", "Hi. How can I help?")
        val choice = choice("""
            Can you heal me?
            Do you see a lot of injured fighters?
            Do you come here often?
			Can you tell me about your cape?
        """)
        when (choice) {
            1 -> {
                player("uncertain", "Can you heal me?")
                heal(player, npc)
            }
            2 -> fighters()
            3 -> often()
            4 -> skillcape( npc)
        }
    }
}



fun heal(player: Player, npc: NPC) {
    player.talkWith(npc) {
        val heal = player.levels.getMax(Skill.Constitution)
        if (player.levels.get(Skill.Constitution) < heal) {
            npc.setAnimation("pick_pocket")
            player.playSound("heal")
            player.levels.restore(Skill.Constitution, heal)
            player.message("You feel a little better.")
            return@talkWith
        }
        npc("cheerful", "You look healthy to me!")
    }
}

suspend fun DialogueContext.fighters() {
    player("uncertain", "Do you see a lot of injured fighters?")
    npc("talking", """
        Yes I do. Thankfully we can cope with almost anything.
	    Jaraah really is a wonderful surgeon, his methods are a
	    little unorthodox but he gets the job done.
    """)
    npc("talking", """
	    I shouldn't tell you this but his nickname is 'The
	    Butcher'.
	""")
    player("uncertain", "That's reassuring.")
}

suspend fun DialogueContext.often() {
    player("uncertain", "Do you come here often?")
    npc("cheerful", "I work here, so yes!")
    npc("laugh", "You're silly!")
}

suspend fun DialogueContext.skillcape(npc: NPC) {
    player("unsure", "Can you tell me about your cape?")
    npc("cheerful", """
        Certainly! Skillcapes are a symbol of achievement. Only
        people who have mastered a skill and reached level 99
        can get their hands on them and gain the benefits they
        carry.
    """)
    npc("talking", """
        The Cape of Constitution doubles the speed of your
        constitution replenishing when worn. Is there anything else
        I can help you with?
    """)
    val choice = choice("""
            Can you heal me?
            Do you see a lot of injured fighters?
            Do you come here often?
        """)
    when (choice) {
        1 -> {
            player("uncertain", "Can you heal me?")
            heal(player, npc)
        }
        2 -> fighters()
        3 -> often()
    }
}

on<NPCOption>({ npc.id == "surgeon_general_tafani" && option == "Heal" }) { player: Player ->
    heal(player, npc)
}