package content.entity.obj

import content.entity.player.bank.bank
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class MilkCow : Script {

    init {
        objectOperate("Milk", "prized_dairy_cow") {
            if (!holdsItem("bucket")) {
                message("You'll need an empty bucket to collect the milk.")
                return@objectOperate
            }
            if (quest("cooks_assistant") != "started") {
                statement("If you're after ordinary milk, you should use an ordinary dairy cow.")
            }
            if (holdsItem("top_quality_milk") || bank.contains("top_quality_milk")) {
                message("You've already got some top-quality milk; you should take it to the cook.")
                return@objectOperate
            }
            anim("milk_cow")
            sound("milk_cow")
            delay(5)
            inventory.replace("bucket", "top_quality_milk")
            message("You milk the cow for top-quality milk.")
        }

        objectOperate("Milk", "dairy_cow") {
            if (holdsItem("bucket")) {
                anim("milk_cow")
                sound("milk_cow")
                delay(5)
                inventory.replace("bucket", "bucket_of_milk")
                message("You milk the cow.")
            } else {
                npc<Laugh>("gillie_groats", "Tee hee! You've never milked a cow before, have you?")
                player<Quiz>("Erm...no. How could you tell?")
                npc<Laugh>("gillie_groats", "Because you're spilling milk all over the floor. What a waste! You need something to hold the milk.")
                player<Neutral>("Ah, yes, I really should have guessed that one, shouldn't I?")
                npc<Laugh>("gillie_groats", "You're from the city, aren't you? Try it again with an empty bucket.")
                player<Neutral>("Right, I'll do that.")
                message("You'll need an empty bucket to collect the milk.")
            }
        }
    }
}
