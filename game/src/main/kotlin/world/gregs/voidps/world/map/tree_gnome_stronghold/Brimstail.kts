package world.gregs.voidps.world.map.tree_gnome_stronghold

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.activity.quest.questComplete
import world.gregs.voidps.world.activity.skill.runecrafting.EssenceMine
import world.gregs.voidps.world.interact.dialogue.Happy
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.obj.teleportTakeOff

npcOperate("Talk-to", "brimstail") {
    if (!player.questComplete("rune_mysteries")) {
        npc<Happy>("Hello adventurer, what can I do for you?")
        player<Quiz>("What's that cute creature wandering around?")
        npc<Talk>("Oh Izzie? He's my pet.")
        player<Happy>("What kind of creature is Izzie?")
        npc<Talk>("I'm not sure. He's very cute though don't you think? It's very restful having a pet, especially when you are stuck in a cave for as long as I am!")
        player<Happy>("So, what do you do down here anyway?")
        npc<Talk>("Isn't that obvious? I investigate Thaumaturgy, I research the Arcane, I attempt to explain the inexplicable.")
        npc<Talk>("Anyway, on that note, it's time for me to get back to work.")
        return@npcOperate
    }
    npc<Happy>("Hello adventurer, Sedridor has told me all about you! What can I do for you?")
    choice {
        option<Quiz>("Can you teleport me to the Rune Essence Mine?") {
            npc<Happy>("Okay. Hold onto your hat!")
            EssenceMine.teleport(target, player)
        }
        option<Happy>("Nothing for now, thanks!") {
            npc<Happy>("Okay. Just remember that a friend of a wizard is a friend of mine!")
        }
    }
}

npcOperate("Teleport", "brimstail") {
    if (player.questComplete("rune_mysteries")) {
        EssenceMine.teleport(target, player)
    } else {
        player.message("You need to have completed the Rune Mysteries Quest to use this feature.")
    }
}

teleportTakeOff("Enter", "brimstails_cave_entrance") {
    player.message("You duck down as you enter this small door.")
}

teleportTakeOff("Exit", "brimstails_cave_exit_*") {
    player.message("You crouch your way through a cramped tunnel.")
}