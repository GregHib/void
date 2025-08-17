package content.area.misthalin.varrock.grand_exchange

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player

npcOperate("Talk-to", "farid_morrisane") {
    if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
        player<Talk>("Hello.")
        npc<Talk>("Hmmm, a new trader. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting and complete lesson and the Tutor will give a smaller, plain lesson.")
        return@npcOperate
    }
    player<Happy>("Hello, little boy.")
    npc<Talk>("I would prefer it if you didn't speak to me in such a manner. I'll have you know I'm an accomplished merchant.")
    choice {
        calmDown()
        oresAndBars()
        bye()
    }
}

npcOperate("Info-ores", "farid_morrisane") {
    if (Settings["grandExchange.tutorial.required", false] && !player.questCompleted("grand_exchange_tutorial")) {
        npc<Talk>("Hmmm, a new trader. I suggest you speak with Brugsen Bursen or the Grand Exchange Tutor near the entrance for a lesson. Brugsen will give an interesting and complete lesson and the Tutor will give a smaller, plain lesson.")
        return@npcOperate
    }
    player["common_item_costs"] = "ores"
    player.open("common_item_costs")
}

fun ChoiceBuilder<NPCOption<Player>>.oresAndBars() {
    option<Quiz>("Can you show me the prices of ores and bars?") {
        player["common_item_costs"] = "ores"
        player.open("common_item_costs")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.bye() {
    option<Talk>("I best go and speak with someone more my height.") {
        npc<Talk>("Then I shall not stop you. I've too much work to do.")
    }
}

fun ChoiceBuilder<NPCOption<Player>>.calmDown() {
    option<Talk>("Calm down, junior.") {
        npc<Talk>("Don't tell me to calm down! And don't call me 'junior'.")
        npc<Talk>("I'll have you know I am Farid Morrisane, son of Ali Morrisane, the world's greatest merchant!")
        player<Quiz>("Then why are you here and not him?")
        npc<Talk>("My dad has given me the responsibility of expanding our business here.")
        player<Quiz>("And you're up to the task? What a grown up boy you are! Mummy and daddy must be very pleased!")
        npc<Talk>("Look, mate - I may be young, I may be short, but I'm a respected merchant around here and don't have time to deal with simpletons like you.")
        choice {
            oresAndBars()
            bye()
        }
    }
}
