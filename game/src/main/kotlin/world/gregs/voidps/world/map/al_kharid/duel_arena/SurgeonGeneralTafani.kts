package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.Unsure
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ npc.id == "surgeon_general_tafani" && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("Hi!")
    npc<Cheerful>("Hi. How can I help?")
    val choice = choice("""
        Can you heal me?
        Do you see a lot of injured fighters?
        Do you come here often?
    	Can you tell me about your cape?
    """)
    when (choice) {
        1 -> {
            player<Uncertain>("Can you heal me?")
            heal()
        }
        2 -> fighters()
        3 -> often()
        4 -> skillcape()
    }
}

suspend fun NPCOption.skillcape() {
    player<Unsure>("Can you tell me about your cape?")
    npc<Cheerful>("""
        Certainly! Skillcapes are a symbol of achievement. Only
        people who have mastered a skill and reached level 99
        can get their hands on them and gain the benefits they
        carry.
    """)
    npc<Talking>("""
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
            player<Uncertain>("Can you heal me?")
            heal()
        }
        2 -> fighters()
        3 -> often()
    }
}

on<NPCOption>({ npc.id == "surgeon_general_tafani" && option == "Heal" }) { player: Player ->
    heal()
}