package content.entity.obj

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.quest.quest
import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.sound.playSound

objectOperate("Milk", "prized_dairy_cow") {
    if (!player.holdsItem("bucket")) {
        player.message("You'll need an empty bucket to collect the milk.")
        return@objectOperate
    }
    if (player.quest("cooks_assistant") != "started") {
        statement("If you're after ordinary milk, you should use an ordinary dairy cow.")
    }
    if (player.holdsItem("top_quality_milk") || player.bank.contains("top_quality_milk")) {
        player.message("You've already got some top-quality milk; you should take it to the cook.")
        return@objectOperate
    }
    player.anim("milk_cow")
    player.playSound("milk_cow")
    delay(5)
    player.inventory.replace("bucket", "top_quality_milk")
    player.message("You milk the cow for top-quality milk.")
}

objectOperate("Milk", "dairy_cow") {
    if (player.holdsItem("bucket")) {
        player.anim("milk_cow")
        player.playSound("milk_cow")
        delay(5)
        player.inventory.replace("bucket", "bucket_of_milk")
        player.message("You milk the cow.")
    } else {
        npc<Chuckle>("gillie_groats", "Tee hee! You've never milked a cow before, have you?")
        player<Quiz>("Erm...no. How could you tell?")
        npc<Chuckle>("gillie_groats", "Because you're spilling milk all over the floor. What a waste! You need something to hold the milk.")
        player<Talk>("Ah, yes, I really should have guessed that one, shouldn't I?")
        npc<Chuckle>("gillie_groats", "You're from the city, aren't you? Try it again with an empty bucket.")
        player<Talk>("Right, I'll do that.")
        player.message("You'll need an empty bucket to collect the milk.")
    }
}