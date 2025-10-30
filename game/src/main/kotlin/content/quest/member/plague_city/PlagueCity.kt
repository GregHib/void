package content.quest.member.plague_city

import content.entity.combat.hit.directHit
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.player.inv.inventoryItem
import content.entity.player.inv.item.ItemUsedOnItem
import content.entity.player.modal.tab.questJournalOpen
import content.entity.sound.sound
import content.quest.messageScroll
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class PlagueCity : Script {

    val areas: AreaDefinitions by inject()

    val stages = setOf("grill_open", "spoken_to_jethick", "returned_book", "spoken_to_ted", "spoken_to_milli", "need_clearance", "talk_to_bravek", "has_cure_paper", "gave_cure", "freed_elena", "completed", "completed_with_spell")

    init {
        playerSpawn {
            if (get("plaguecity_can_see_edmond_up_top", false)) {
                sendVariable("plaguecity_can_see_edmond_up_top")
            }
            if (get("plaguecity_dug_mud_pile", false)) {
                sendVariable("plaguecity_dug_mud_pile")
            }
            if (get("plaguecity_checked_grill", false)) {
                sendVariable("plaguecity_checked_grill")
            }
            if (get("plaguecity_key_asked", false)) {
                sendVariable("plaguecity_key_asked")
            }
            sendVariable("plaguecity_pipe")
            sendVariable("plaguecity_elena_at_home")
            sendVariable("plague_city")
        }

        questJournalOpen("plague_city") {
            val lines = when (player.quest("plague_city")) {
                "started" -> {
                    val list = mutableListOf(
                        "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                        "<str>help find his daughter Elena, who's gone missing while ",
                        "<str>trying to help plague victims in West Ardougne.",
                        "<maroon>Edmond <navy>told me that his wife, <maroon>Alrena, <navy>can make me a <maroon>gas",
                        "<maroon>Mask <navy>to protect myself from the <maroon>plague.",
                    )
                    if (player.holdsItem("dwellberries")) {
                        list.add("<navy>I need to get some <maroon>Dwellberries<navy> for <maroon>Alrena<navy> so she can make")
                        list.add("<navy>me a <maroon>Gas Mask<navy> to protect myself from the <maroon>Plague<navy>. According")
                        list.add("<navy>to <maroon>Edmond<navy>, I can find some in <maroon>McGrubor's Wood<navy>, west of")
                        list.add("<navy><maroon>Seers' Village<navy>. <maroon>Edmond<navy> warned me that I'd need to find a back")
                        list.add("<navy>way in, as the area is guarded.")
                    } else {
                        list.add("<navy>I have the <maroon>Dwellberries <navy>she needs to make it.")
                        list.add("<navy>I should give them to her.")
                        list.add("")
                        list.add("")
                    }
                    list
                }
                "has_mask" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<navy>I need to talk to <maroon>Edmond <navy>about getting into <maroon>West Ardougne.",
                    "",
                    "",
                )
                "about_digging" -> listOf(
                    "<navy>I need to dig a tunnel down into the <maroon>Ardougne Sewers<navy> from",
                    "<navy><maroon>Edmond's<navy> garden. However, I first need to use some <maroon>Buckets",
                    "<maroon>of Water<navy> to soften the ground. <maroon>Edmond<navy> reckons four <maroon>Buckets",
                    "<maroon>of Water<navy> should be enough.",
                    "",
                    "",
                )
                "one_bucket_of_water", "two_bucket_of_water", "three_bucket_of_water" -> {
                    val list = mutableListOf(
                        "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                        "<str>help find his daughter Elena, who's gone missing while ",
                        "<str>trying to help plague victims in West Ardougne.",
                        "<str>Alrena has given me a Gas Mask to protect me from the",
                        "<str>Plague while in West Ardougne. She's made me a spare one",
                        "<str>and left it in he wardrobe for if I lose this one.",
                        "<str>I've spoken to Edmond about getting into West Ardougne.",
                        "<str>He thinks I can get into the city through the sewers beneath",
                        "<str>his home.",
                        "<navy>I need to dig a tunnel down into the <maroon>Ardougne Sewers <navy>from",
                        "<maroon>Edmond's <navy>garden. However, I first need to use some",
                        "<maroon>Bucket of Water <navy>to soften the ground.<maroon>Edmond <navy>reckons",
                        "<navy>four <maroon>Bucket of Water <navy>should be enough.",
                    )
                    if (player.quest("plague_city") == "one_bucket_of_water") {
                        list.add("<navy>I've used one <maroon>Bucket of Water <navy>so far.")
                    }
                    if (player.quest("plague_city") == "two_bucket_of_water") {
                        list.add("<navy>I've used two <maroon>Bucket of Water <navy>so far.")
                    }
                    if (player.quest("plague_city") == "three_bucket_of_water") {
                        list.add("<navy>I've used three <maroon>Bucket of Water <navy>so far.")
                    }
                    list.add("")
                    list.add("")
                    list
                }
                "four_bucket_of_water" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<navy>I now need to dig a tunnel into the <maroon>Ardougne Sewers <navy>from",
                    "<maroon>Edmond's <navy>garden.",
                    "",
                    "",
                )
                "sewer" -> {
                    val list = mutableListOf(
                        "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                        "<str>help find his daughter Elena, who's gone missing while ",
                        "<str>trying to help plague victims in West Ardougne.",
                        "<str>Alrena has given me a Gas Mask to protect me from the",
                        "<str>Plague while in West Ardougne. She's made me a spare one",
                        "<str>and left it in he wardrobe for if I lose this one.",
                        "<str>I've spoken to Edmond about getting into West Ardougne.",
                        "<str>He thinks I can get into the city through the sewers beneath",
                        "<str>his home.",
                        "<str>I've softened the ground in Edmond's garden enough to dig",
                        "<str>down into the Sewers.",
                        "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    )
                    if (player["plaguecity_checked_grill", false]) {
                        list.add("<navy>I've found a pipe that leads out of the <maroon>Ardougne Sewers")
                        list.add("<navy>and into <maroon>West Ardougne. <navy>However, there's a <maroon>Grill <navy>blocking")
                        list.add("<navy>my way. I might be able to use some <maroon>Rope <navy>to pull it off.")
                    } else {
                        list.add("<navy>I need to find a way out of the <maroon>Ardougne Sewers <navy>and into")
                        list.add("<maroon>West Ardougne.")
                    }
                    list.add("")
                    list.add("")
                    list
                }
                "grill_rope" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<navy>I've found a pipe that leads out of the <maroon>Ardougne Sewers",
                    "<navy>and into <maroon>West Ardougne. <navy>However, there's a <maroon>Grill <navy>blocking",
                    "<navy>my way. I've tied a rope to the <maroon>Grill <navy>but I need help to pull it",
                    "<navy>off, I should ask <maroon>Edmond <navy>for some help.",
                    "",
                    "",
                )
                "grill_open" -> {
                    val list = mutableListOf(
                        "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                        "<str>help find his daughter Elena, who's gone missing while ",
                        "<str>trying to help plague victims in West Ardougne.",
                        "<str>Alrena has given me a Gas Mask to protect me from the",
                        "<str>Plague while in West Ardougne. She's made me a spare one",
                        "<str>and left it in he wardrobe for if I lose this one.",
                        "<str>I've spoken to Edmond about getting into West Ardougne.",
                        "<str>He thinks I can get into the city through the sewers beneath",
                        "<str>his home.",
                        "<str>I've softened the ground in Edmond's garden enough to dig",
                        "<str>down into the Sewers.",
                        "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                        "<str>I've found a pipe that leads out of the Ardougne Sewers",
                        "<str>and into West Ardougne.",
                    )
                    if (player["plaguecity_picture_asked", false]) {
                        list.add("<navy>I entered <maroon>West Ardougne <navy>and found <maroon>Jethick<navy>, an old friend")
                        list.add("<navy>of <maroon>Edmond. <navy>He seemed willing to help me find <maroon>Elena <navy>but")
                        list.add("<navy>didn't know what she looked like.")
                        if (player.holdsItem("picture_plague_city")) {
                            list.add("<navy>I have a picture of her which might help. I should show it to <maroon>Jethick.")
                        }
                    } else {
                        list.add("<navy>I can now enter <maroon>West Ardougne. Edmond <navy>told me to look out")
                        list.add("<navy>for a old family friend named <maroon>Jethick<navy>, who might be able")
                        list.add("<navy>to help me find <maroon>Elena. <navy>I should make sure I wear my <maroon>gas")
                        list.add("<maroon>Mask <navy>while in <maroon>West Ardougne.")
                    }
                    list.add("")
                    list.add("")
                    list
                }
                "spoken_to_jethick" -> {
                    val list = mutableListOf(
                        "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                        "<str>help find his daughter Elena, who's gone missing while ",
                        "<str>trying to help plague victims in West Ardougne.",
                        "<str>Alrena has given me a Gas Mask to protect me from the",
                        "<str>Plague while in West Ardougne. She's made me a spare one",
                        "<str>and left it in he wardrobe for if I lose this one.",
                        "<str>I've spoken to Edmond about getting into West Ardougne.",
                        "<str>He thinks I can get into the city through the sewers beneath",
                        "<str>his home.",
                        "<str>I've softened the ground in Edmond's garden enough to dig",
                        "<str>down into the Sewers.",
                        "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                        "<str>I've found a pipe that leads out of the Ardougne Sewers",
                        "<str>and into West Ardougne.",
                    )
                    if (player["plaguecity_picture_asked", false]) {
                        list.add("<navy>of <maroon>Edmond. <navy>He thinks that <maroon>Elena <navy>was staying with the")
                        list.add("<maroon>Rehnison Family. <navy>According to him, they live in a timber")
                        list.add("<navy>house in the north of the city. He asked me to return a")
                        list.add("<navy>book to them while I was there.")
                        if (!player.holdsItem("book_turnip_growing_for_beginners")) {
                            list.add("<navy>but I don't have it with me.")
                        }
                    } else {
                        list.add("<navy>I entered <maroon>West Ardougne<navy> and found <maroon>Jethick<navy>, an old friend of")
                        list.add("<navy><maroon>Edmond<navy>. He seemed willing to help me find <maroon>Elena<navy> but didn't")
                        if (player.holdsItem("picture_plague_city")) {
                            list.add("<navy>know what she looked like. I have a picture of her which might")
                            list.add("<navy>help. I should show it to <maroon>Jethick<navy>.")
                        } else {
                            list.add("<navy>know what she looked like. He suggested I get a picture of her.")
                            list.add("<navy>Perhaps <maroon>Edmond<navy> and <maroon>Alrena<navy> have one.")
                        }
                    }
                    list.add("")
                    list.add("")
                    list
                }
                "returned_book" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<navy>I entered <maroon>West Ardougne <navy>and found <maroon>Jethick<navy>, an old friend",
                    "<navy>of <maroon>Edmond. <navy>He thinks that <maroon>Elena <navy>was staying with the",
                    "<maroon>Rehnison Family. <navy>According to him, they live in a timber",
                    "<navy>house in the north of the city.",
                    "",
                    "",
                )
                "spoken_to_ted" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<str>I entered West Ardougne and found Jethick, an old friend",
                    "<str>of Edmond. He thinks that Elena was staying with the",
                    "<str>Rehnison Family.",
                    "<navy>I visited the home of the <maroon>Rehnison Family. <navy>in the north of",
                    "<navy>the city to see if I could find <maroon>Elena. <navy>According to her",
                    "<navy>parents, <maroon>Milli Rehnison <navy>may have seen <maroon>Elena.",
                    "",
                    "",
                )
                "spoken_to_milli" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<str>I entered West Ardougne and found Jethick, an old friend",
                    "<str>of Edmond. He thinks that Elena was staying with the",
                    "<str>Rehnison Family.",
                    "<str>I visited the home of the Rehnison Family. in the north of",
                    "<str>the city to see if I could find Elena.",
                    "<navy>According to <maroon>Milli Rehnison, Elena <navy> was captured and taken",
                    "<navy>into a <maroon>Plague House <navy>in the south east corner of the city I",
                    "<navy>should go there and see if I can get into the house.",
                    "",
                    "",
                )
                "need_clearance", "talk_to_bravek" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<str>I entered West Ardougne and found Jethick, an old friend",
                    "<str>of Edmond. He thinks that Elena was staying with the",
                    "<str>Rehnison Family.",
                    "<str>I visited the home of the Rehnison Family. in the north of",
                    "<str>the city to see if I could find Elena.",
                    "<str>According to Milli Rehnison, Elena was captured and taken",
                    "<str>into a Plague House in the south east corner of the city",
                    "<navy>I tried to get into the <maroon>Plague House <navy>but was denied entry",
                    "<navy>Apparently, I need clearance from either the <maroon>Head",
                    "<maroon>Mourner <navy>or <maroon>Bravek<navy>, the <maroon>City Warder.",
                    "",
                    "",
                )
                "has_cure_paper" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<str>I entered West Ardougne and found Jethick, an old friend",
                    "<str>of Edmond. He thinks that Elena was staying with the",
                    "<str>Rehnison Family.",
                    "<str>I visited the home of the Rehnison Family. in the north of",
                    "<str>the city to see if I could find Elena.",
                    "<str>According to Milli Rehnison, Elena was captured and taken",
                    "<str>into a Plague House in the south east corner of the city",
                    "<str>I tried to get into the Plague House but was denied entry",
                    "<str>Apparently, I need clearance from either the Head",
                    "<str>Mourner or Bravek, the City Warder.",
                    "<str>Bravek, the City Warder, gave me a Warrant to enter the",
                    "<str>Plague House. However, I need to bring him a",
                    "<str><maroon>Hangover Cure<navy> first. He gave me a recipe for this cure.",
                )
                "gave_cure" -> {
                    val list = mutableListOf(
                        "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                        "<str>help find his daughter Elena, who's gone missing while ",
                        "<str>trying to help plague victims in West Ardougne.",
                        "<str>Alrena has given me a Gas Mask to protect me from the",
                        "<str>Plague while in West Ardougne. She's made me a spare one",
                        "<str>and left it in he wardrobe for if I lose this one.",
                        "<str>I've spoken to Edmond about getting into West Ardougne.",
                        "<str>He thinks I can get into the city through the sewers beneath",
                        "<str>his home.",
                        "<str>I've softened the ground in Edmond's garden enough to dig",
                        "<str>down into the Sewers.",
                        "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                        "<str>I've found a pipe that leads out of the Ardougne Sewers",
                        "<str>and into West Ardougne.",
                        "<str>I entered West Ardougne and found Jethick, an old friend",
                        "<str>of Edmond. He thinks that Elena was staying with the",
                        "<str>Rehnison Family.",
                        "<str>I visited the home of the Rehnison Family. in the north of",
                        "<str>the city to see if I could find Elena.",
                        "<str>According to Milli Rehnison, Elena was captured and taken",
                        "<str>into a Plague House in the south east corner of the city",
                        "<str>I tried to get into the Plague House but was denied entry",
                        "<str>Apparently, I need clearance from either the Head",
                        "<str>Mourner or Bravek, the City Warder.",
                        "<str>Bravek, the City Warder, gave me a Warrant to enter the",
                        "<str>Plague House in return for a Hangover Cure",
                    )

                    if (!player["plaguecity_key_asked", false]) {
                        list.add("<navy>I entered the <maroon>Plague House <navy>and found <maroon>Elena. <navy>Now I just")
                        list.add("<navy>need to free her. She thinks the key to her cell is hidden")
                        list.add("<navy>somewhere in the house.")
                    } else {
                        if (player.tile in areas["plague_house"] || player.tile in areas["plague_house_basement"]) {
                            list.add("<navy>I've managed to enter the <maroon>Plague House. <navy>Now I need to find")
                            list.add("<maroon>Elena.")
                        } else {
                            list.add("<navy>I need to get into the <maroon>Plague House <navy> in the south east")
                            list.add("<navy> corner of the city and find <maroon>Elena<navy>.")
                        }
                    }
                    list.add("")
                    list.add("")
                    list
                }
                "freed_elena" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<str>I entered West Ardougne and found Jethick, an old friend",
                    "<str>of Edmond. He thinks that Elena was staying with the",
                    "<str>Rehnison Family.",
                    "<str>I visited the home of the Rehnison Family. in the north of",
                    "<str>the city to see if I could find Elena.",
                    "<str>According to Milli Rehnison, Elena was captured and taken",
                    "<str>into a Plague House in the south east corner of the city",
                    "<str>I tried to get into the Plague House but was denied entry",
                    "<str>Apparently, I need clearance from either the Head",
                    "<str>Mourner or Bravek, the City Warder.",
                    "<str>Bravek, the City Warder, gave me a Warrant to enter the",
                    "<str>Plague House in return for a Hangover Cure",
                    "<str>I managed to get into the Plague House",
                    "<navy>I successfully freed <maroon>Elena <navy>from the <maroon>Plague House. <navy>I should",
                    "<navy>return to <maroon>Edmond <navy>and let him know.",
                    "",
                    "",
                )
                "completed", "completed_with_spell" -> listOf(
                    "<str>I've spoken to Edmond in East Ardougne. He's asked to",
                    "<str>help find his daughter Elena, who's gone missing while ",
                    "<str>trying to help plague victims in West Ardougne.",
                    "<str>Alrena has given me a Gas Mask to protect me from the",
                    "<str>Plague while in West Ardougne. She's made me a spare one",
                    "<str>and left it in he wardrobe for if I lose this one.",
                    "<str>I've spoken to Edmond about getting into West Ardougne.",
                    "<str>He thinks I can get into the city through the sewers beneath",
                    "<str>his home.",
                    "<str>I've softened the ground in Edmond's garden enough to dig",
                    "<str>down into the Sewers.",
                    "<str>I've dug a tunnel into the Ardougne sewers from Edmond's garden",
                    "<str>I've found a pipe that leads out of the Ardougne Sewers",
                    "<str>and into West Ardougne.",
                    "<str>I entered West Ardougne and found Jethick, an old friend",
                    "<str>of Edmond. He thinks that Elena was staying with the",
                    "<str>Rehnison Family.",
                    "<str>I visited the home of the Rehnison Family. in the north of",
                    "<str>the city to see if I could find Elena.",
                    "<str>According to Milli Rehnison, Elena was captured and taken",
                    "<str>into a Plague House in the south east corner of the city",
                    "<str>I tried to get into the Plague House but was denied entry",
                    "<str>Apparently, I need clearance from either the Head",
                    "<str>Mourner or Bravek, the City Warder.",
                    "<str>Bravek, the City Warder, gave me a Warrant to enter the",
                    "<str>Plague House in return for a Hangover Cure",
                    "<str>I managed to get into the Plague House",
                    "<str>I successfully freed Elena from the Plague House. I",
                    "<str>returned to Edmond who thanked me for rescuing her. He",
                    "<str>game me a new spell to learn as a reward.",
                    "",
                    "<red>QUEST COMPLETE!",
                    "",
                    "",
                )
                else -> listOf(
                    "<navy>I can start this quest by talking to <maroon>Edmond <navy>at his home in northern",
                    "<maroon>East Ardougne.",
                    "",
                    "",
                )
            }
            player.questJournal("Plague City", lines)
        }

        itemOnObjectOperate("rope", "plague_sewer_pipe_open") {
            player<Talk>("Maybe I should try opening it first.")
        }

        itemOnObjectOperate("rope", "plague_grill") {
            ropeOnGrill()
        }

        objectOperate("Climb-up", "plague_sewer_pipe_open") {
            if (player["plaguecity_pipe", "grill"] == "grill_open" &&
                stages.contains(player.quest("plague_city"))
            ) {
                if (player.equipped(EquipSlot.Hat).id == "gas_mask") {
                    player.anim("4855", delay = 10)
                    statement("You climb up through the sewer pipe.", clickToContinue = false)
                    player.open("fade_out")
                    delay(3)
                    player.tele(2529, 3304)
                    player.open("fade_in")
                    statement("You climb up through the sewer pipe.", clickToContinue = true)
                } else {
                    npc<Neutral>("edmond", "I can't let you enter the city without your gas mask on.")
                }
            } else {
                statement("There is a grill blocking your way.")
            }
        }

        objectOperate("Open", "plague_grill_vis") {
            player.animDelay("pull_on_pipe")
            player.sound("irondoor_locked")
            if (!player["plaguecity_checked_grill", false]) {
                player["plaguecity_checked_grill"] = true
            }
            statement("The grill is too secure. <br> You can't pull it off alone.")
        }

        inventoryItem("Read", "a_magic_scroll") {
            if (player.quest("plague_city") == "completed_with_spell") {
                player.directHit(0)
//                player.gfx("explosion")
            } else {
                player["plague_city"] = "completed_with_spell"
                player.sound("wom_bless")
                player.inventory.remove("a_magic_scroll")
                item("a_magic_scroll", 600, "You memorise what is written on the scroll. You can now use the Ardougne Teleport Spell.")
            }
        }

        inventoryItem("Read", "a_scruffy_note") {
            player.messageScroll(
                listOf(
                    "",
                    "",
                    "Got a bncket of nnilk",
                    "Tlen qrind sorne lhoculate",
                    "vnith a pestal and rnortar",
                    "ald the grourd dlocolate to tho milt",
                    "fnales add 5cme snape gras5 ",
                ),
                handwriting = true,
            )
        }

        onEvent<Player, ItemUsedOnItem>("item_used_on_item", "*") { player ->
            if (def.add.any { it.id == "chocolatey_milk" }) {
                player.queue("milk") {
                    item("chocolatey_milk", 400, "You mix the chocolate into the bucket.")
                }
            }
        }

        onEvent<Player, ItemUsedOnItem>("item_used_on_item", "*") { player ->
            if (def.add.any { it.id == "hangover_cure" }) {
                player.queue("cure") {
                    item("hangover_cure", 400, "You mix the snape grass into the bucket.")
                }
            }
        }
    }

    suspend fun SuspendableContext<Player>.ropeOnGrill() {
        if (player["plaguecity_pipe", "grill"] != "grill") {
            player.noInterest()
            return
        }
        player.sound("plague_attach")
        player.animDelay("rope_tie")
        player["plaguecity_pipe"] = "grill_rope"
        player["plague_city"] = "grill_rope"
        player.inventory.remove("rope", 1)
        item("rope", 600, "You tie the end of the rope to the sewer pipe's grill.")
    }
}
