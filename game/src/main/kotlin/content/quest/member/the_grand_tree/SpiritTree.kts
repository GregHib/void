package content.quest.member.the_grand_tree

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.TreeHappy
import content.entity.player.dialogue.TreeTalk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Talk-to", "spirit_tree") {
    if (!player.questCompleted("the_grand_tree")) {
        statement("The tree doesn't feel like talking.")
        return@objectOperate
    }
    npc<TreeTalk>("Hello gnome friend. Where would you like to go?")
    player.open("spirit_tree")
}

objectOperate("Talk-to", "spirit_tree_gnome") {
    if (!player.questCompleted("the_grand_tree")) {
        statement("The tree doesn't feel like talking.")
        return@objectOperate
    }
    npc<TreeHappy>("You friend of gnome people, you friend of mine. Would you like me to take you somewhere?")
    choice {
        option<Talk>("No thanks, old tree.")
        option<Quiz>("Where can I go?") {
            npc<TreeTalk>("You can travel to the trees which are related to me.")
            player.open("spirit_tree")
        }
    }
}

objectOperate("Teleport", "spirit_tree*") {
    player.open("spirit_tree")
}

interfaceOpen("spirit_tree") { player ->
    player.interfaceOptions.unlockAll(id, "text", 0 until 5)

//    npc<TreeTalk>("You're already here.")
}

interfaceOption("*", "text", "spirit_tree") {
    player.message("You feel at one with the spirit tree.")
}