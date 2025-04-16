package content.quest.free.cooks_assistant

import world.gregs.voidps.engine.inv.holdsItem
import content.entity.player.bank.bank
import content.entity.player.modal.tab.questJournalOpen
import content.quest.quest
import content.quest.questJournal

questJournalOpen("cooks_assistant") {
    val lines = when (player.quest("cooks_assistant")) {
        "completed" -> listOf(
            "<str>It was the Duke of Lumbridge's birthday, but his cook had",
            "<str>forgotten to buy the ingredients he needed to bake a ",
            "<str>birthday cake. I brought the cook an egg, a pot of flour",
            "<str>and a bucket of milk and the cook made a delicious-looking",
            "<str>cake with them",
            "",
            "<str>As a reward he now lets me use his high quality range",
            "<str>which lets me burn things less whenever I wish to cook there",
            "",
            "<red>QUEST COMPLETE!"
        )
        "started" -> {
            val list = mutableListOf(
                "<navy>It's the <maroon>Duke of Lumbridge's <navy>birthday and I have to help",
                "<navy>his <maroon>Cook <navy>make him a <maroon>birthday cake. <navy>To do this I need to",
                "<navy>bring the cook the following ingredients:",
                "",
            )
            if (player["cooks_assistant_milk", 0] == 1) {
                list.add("<str>I have given the cook a bucket of top-quality milk.")
            } else if (player.holdsItem("top_quality_milk")) {
                list.add("<navy>I have found a <maroon>bucket of top-quality milk <navy>to give to the cook.")
            } else if (player.bank.contains("top_quality_milk")) {
                list.add("<navy>I have a <maroon>bucket of top-quality milk <navy>to give to the cook. it's in my <maroon>bank.")
            } else {
                list.add("<navy>I need to find a <maroon>bucket of top-quality milk.")
            }


            if (player["cooks_assistant_flour", 0] == 1) {
                list.add("<str>I have given the cook a pot of extra fine flour.")
            } else if (player.holdsItem("extra_fine_flour")) {
                list.add("<navy>I have found a <maroon>pot of extra fine flour <navy>to give to the cook.")
            } else if (player.bank.contains("extra_fine_flour")) {
                list.add("<navy>I have a <maroon>pot of extra fine flour <navy>to give to the cook. it's in my <maroon>bank.")
            } else {
                list.add("<navy>I need to find a <maroon>pot of extra fine flour.")
            }


            if (player["cooks_assistant_egg", 0] == 1) {
                list.add("<str>I have given the cook a super large egg.")
            } else if (player.holdsItem("super_large_egg")) {
                list.add("<navy>I have found a <maroon>super large egg <navy>to give to the cook.")
            } else if (player.bank.contains("super_large_egg")) {
                list.add("<navy>I have a <maroon>super large egg <navy>to give to the cook. it's in my <maroon>bank.")
            } else {
                list.add("<navy>I need to find a <maroon>super large egg.")
            }

            if (player["cooks_assistant_milk", 0] == 1 && player["cooks_assistant_flour", 0] == 1 && player["cooks_assistant_egg", 0] == 1) {
                list.add("")
                list.add("<str>According to the cook, I can find the ingredients in the")
                list.add("<str>vicinity of Lumbridge. he has noted certain possible")
                list.add("<str>locations of the ingredients on my world map.")
                list.add("")
                list.add("<navy>I just need to talk to the <maroon>cook <navy>now to claim my reward!")
            } else {
                list.add("")
                list.add("<navy>According to the <maroon>cook, <navy>I can find the ingredients in the")
                list.add("<navy>vicinity of <maroon>Lumbridge. <navy>he has noted certain possible")
                list.add("<navy>locations of the ingredients on my world map.")
            }
            list
        }
        else -> listOf(
            "<navy>I can start this quest by speaking to the <maroon>Cook <navy>in the",
            "<maroon>kitchen <navy>of <maroon>Lumbridge Castle."
        )
    }
    player.questJournal("Cook's Aassistant", lines)
}