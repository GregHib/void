package content.area.misthalin.varrock.museum

import content.quest.questCompleted
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest

class MuseumDisplayCases : Script {
    init {
        // vm_timeline_no_display is the empty-case transform for every museum display,
        // so scope both options to the terracotta statue's own base object.
        objectOperate("Study", "vm_timeline_terracotta_statue_multi") { (target) ->
            if (target.id != "vm_timeline_terracotta_statue") {
                noInterest()
                return@objectOperate
            }
        }

        objectOperate("Study", "vm_timeline_no_display") { (target) ->
            display(target.id)
        }

        objectOperate("Study", "vm_timeline_*,vm_digsite_finds*,surok_timeline_*") { (target) ->
            display(target.def(this).stringId)
        }

        interfaceClosed("vm_timeline") {
            clearAnim()
        }
    }

    private fun Player.display(id: String) {
        when (id) {
            "vm_timeline_meeting_history" -> displayNoInfo(13)
            "vm_timeline_meeting_history_multi" -> TODO("13, Meeting history")
            "vm_timeline_soil_layers" -> displayCaseBowAndSwordRunes(
                27,
                25614,
                "The Layers of Archaeology<br>Soil Layers<br><br>O horizon - The top layer of soil is made up mostly of leaves and decomposed organic matter.<br><br>A horizon (topsoil) - Plants grow in this dark-coloured layer, which is made up of decomposed organic matter mixed with mineral particles.<br><br>E horizon - This eluviation (leaching) layer has a light colour and is made up of sand and silt. We often find significant archaeological artefacts in this layer.<br><br>B horizon (subsoil) - Contains clay and mineral deposits that it receives from layers above, when water drips through.<br><br>C horizon - Regolith consists of slightly broken up bedrock. Plant roots do not penetrate into this layer.<br><br>R horizon - The bedrock layer that is beneath all the other layers.",
            )
            "vm_digsite_finds_coin_saranthium" -> displayNoInfo(45)
            "vm_digsite_finds_coin_saranthium_multi" -> displayCaseCoinSaranthium(45, 25522, "A coin in very good condition with Saradominist markings. It bears the word 'Saranthium', which we have found to be the name of the city being excavated east of Varrock. The numbers on the coin would indicate that it is from the year 3804, presumably from the 3rd Age as the Godwars were coming to an end.")
            "vm_digsite_finds_pottery_before_digsite_multi" -> displayCasePottery(22, 25527, "Two beautiful examples of the many items of pottery found around the Dig Site. There appear to be Saradominist markings upon these artefacts, indicating that this was at one time a Saradominist city. This pottery appears to be dated around the beginning of the 4th Age.")
            "vm_digsite_finds_saradomin_symbol_old" -> displayNoInfo(36)
            "vm_digsite_finds_saradomin_symbol_old_multi" -> displayCaseDruidStoneCircle(36, 25528, "Thought to be an altar decoration, possibly celebrating the beginning of the 4th Age when Saranthium was at its most vibrant. Lovingly crafted in silver and sapphire, it is still in good condition.")
            "vm_digsite_finds_saradomin_symbol_ancient" -> displayNoInfo(37)
            "vm_digsite_finds_saradomin_symbol_ancient_multi" -> displayCaseBrokenArrow(37, 25521, "This small symbol was used as a decoration on the Saradominist altars throughout the city. Two or three examples have been found, but this is the best. Worked in blue-enamelled bronze, it has not survived the ages well. We believe this to be from the early years when the city was still being built.")
            "vm_digsite_finds_vase" -> displayCaseMap(38, 25531, "One of the few vases found in good condition. Its markings show some kind of celebration to Saradomin.")
            "vm_digsite_finds_jewellery" -> displayCaseBowAndSwordRunes(40, 25525, "Fine silver and gold jewellery has been found concealed in one of the many urns scattered around the Dig Site. Not much is known about it, although most of the items do have Saradominist markings.")
            "vm_digsite_finds_arrow_heads" -> displayCaseBowAndSwordRunes(41, 25524, "Arrowheads of crude bronze have been found along with the finds deeper in the Dig Site, which leads us to believe that the forces occupying the city before the Saradominists used bows, as well as other methods of war.")
            "vm_digsite_finds_talisman" -> displayNoInfo(42)
            "vm_digsite_finds_talisman_multi" -> displayCaseMap(42, 25530, "Found at the Dig Site east of Varrock by a recently qualified archaeologist. It appears to be a symbol of the god, Zaros.")
            "vm_digsite_finds_tablet" -> displayNoInfo(43)
            "vm_digsite_finds_tablet_multi" -> displayCaseMap(
                43,
                25529,
                "Excavated recently from a cavernous temple below the Dig Site by a recently qualified archaeologist. It is of great significance, as it proves the existence of a settlement that predates the Saradominist city currently being excavated. The temple appears to be dedicated to the pagan god, Zaros, and survived the destruction of the Zarosian city which was rebuilt by Saradominists.",
            )
            "vm_digsite_finds_coin_senntisten" -> displayNoInfo(44)
            "vm_digsite_finds_coin_senntisten_multi" -> displayCaseCoinSaranthium(44, 25523, "A battered and bent coin with Zamorakian markings. It bears the word 'Senntisten', which we believe to be the original name of the city, Saranthium, before it was rebuilt by those loyal to Saradomin. Partial numbers on the coin would indicate that it is from the year 3740, presumably from the 3rd Age.")
            "vm_digsite_finds_pottery_before_digsite" -> if (get("vm_digsite_zaros_`pottery", false)) {
                displayCasePottery(22, 25526, "Two distinct types of pottery have been recovered from the Dig Site, adding evidence to the two settlements on this site. The red clay pottery appears to be much older and occasionally has purple Zarosian symbols, while the Saradominist artefacts are usually of a lighter shade with blue decorations.")
            } else {
                displayCasePottery(22, 25527, "Two beautiful examples of the many items of pottery found around the Dig Site. There appear to be Saradominist markings upon these artefacts, indicating that this was at one time a Saradominist city. This pottery appears to be dated around the beginning of the 4th Age.")
            }
            "vm_timeline_runestone_rock_model_multi" -> displayCase(
                "vm_timeline_runestone_rock_model_model",
                21,
                25610,
                "5th Age - Currently 169 years old<br><br>In the first years of the 5th Age, human mages discovered the rune essence rocks, but kept their locations a closely guarded secret between a select few of them, so as not to let the information fall into enemy hands. They used the power of the essence and the other altars they knew about to create various runes. Due to the power of the runes humans started to become more dominant within the world. The human kingdoms of Misthalin and Asgarnia quickly grew to become the RuneScape kingdoms and cities we know today. The mages who discovered the rune essence rock (modelled here) set up a great tower of wizardry in southern Misthalin.",
            )
            "vm_timeline_runestones_multi" -> displayCaseSevenKightsHelms(21, 25609, "5th Age - currently 169 years old. In the first years of the 5th Age, human mages discovered the secrets of making runes from rune essence. Due to the power of these runes, humans started to become dominant within the world, with the human kingdoms of Misthalin and Asgarnia quickly growing to become the ones we know today.")
            "vm_timeline_king_lathas_painting" -> displayKingLathasPainting()
            "vm_timeline_map_of_runescape" -> displayMap1st2ndAge()
            "vm_timeline_staff_of_armadyll" -> displayNoInfo(28)
            "vm_timeline_staff_of_armadyll_multi" -> displayCase(
                "vm_timeline_staff_of_armadyll_model",
                28,
                25615,
                "End of 2nd Age<br><br>This is a replica of the Staff of Armadyl, made from the descriptions given to us by a brave adventurer.${
                    if (get("vm_staff_of_armadyl", 0) == 1) " " else " It was this staff, wielded by Zamorak, which killed the god Zaros and cursed all those who aided this treacherous deed. It would seem that it was around this time that Zamorak ascended to godhood. Not much is known of what happened to the staff afterwards, until it's recent discovery. "
                }Armadyl is said to be a god whom was worshipped during the 2nd and 3rd Ages, though he does not seem to have much of a following in modern times.",
            )
            "vm_timeline_three_god_symbols" -> displayCaseTerracottaStatue(
                31,
                25617,
                "3rd Age - 4,000 years long<br><br>The scorched earth of the Wilderness is a lasting reminder of the destruction wrought during the God Wars. The little evidence found intact from these times would indicate that most of the mortal races only just survived this onslaught. Stories handed down over generations tell of great, powerful entities and agents of the gods fighting cataclysmic wars. However, at the end of this 4,000 year long 3rd Age, it seems that Saradomin, Guthix, and Zamorak settled for a less direct influence on our world.",
            )
            "vm_timeline_terracotta_statue" -> displayNoInfo(30)
            "vm_timeline_terracotta_statue_multi" -> displayCaseTerracottaStatue(
                30,
                25576,
                "3rd Age - yr 3000-4000<br><br>This statuette was found in an underground temple in the ruined city of Uzer, which was destroyed late in the 3rd Age, suddenly, due to causes unknown. It probably represents one of the clay golems that the craftsmen of the city built as warriors and servants. The statuette was originally part of a mechanism whose purpose is unknown.",
            )
            "vm_timeline_random_stuff" -> displayCaseRobertTheStrong(
                19,
                25602,
                "4th Age - 2,000 yrs long<br><br>Finds indicate that at the beginning of the 4th Age, the humans who had survived the god wars formed nomadic tribes that battled for survival against not only each other, but also the dwarves, goblins, ogres, gnomes and many more races that were competing for land and resources. Over time, they started to make more permanent settlements throughout the world, but they continued to battle with their neighbours.",
            )
            "vm_timeline_druid_stone_circle" -> displayCaseDruidStoneCircle(
                8,
                25574,
                "4th Age - Years 1-200<br><br>Taverley is the site of the only known example of a surviving druidic stone circle, but the druids of Guthix speak of many others scattered around RuneScape in the past. Our best dating techniques place them in the 4th Age. The druids built them to worship Guthix all across the world. The Druids claim to be keeping watch for Guthix, keeping balance in the world.",
            )
            "vm_timeline_world_map" -> displayMap4thAgeEarly()
            "vm_timeline_robert_the_strong" -> displayNoInfo(20)
            "vm_timeline_robert_the_strong_multi" -> displayCaseRobertTheStrong(
                20,
                25603,
                "4th Age - yr 1-100<br><br>Around the beginning of the 4th Age, a new terror was seen in the world: the Dragonkin. Without active gods, the people had to deal with this problem largely by themselves. Heroes arose to step up to this challenge, one of them being Robert the Strong, who helped drive the dragonkin back to their stronghold where, as far as anyone knows, they sit and brood to this very day.",
            )
            "vm_timeline_star_chart" -> displayCase("vm_timeline_star_chart_model", 29, 25616, "4th Age - yr 31-60<br><br>A fine example of an incredibly old star chart. Not much is known about it, but the positions of the stars indicate that it was made around the years of 31-60 of the 4th Age.", rotate = false)
            "vm_timeline_seven_kights_helms" -> displayNoInfo(23)
            "vm_timeline_seven_kights_helms_multi" -> displayCaseSevenKightsHelms(
                23,
                25577,
                "4th Age - Years 1100-1200<br><br>A tide of evil creatures from the east threatened Avarrocka and border skirmishes were seen between them and the human settlers. Seven priestly warriors-Iriandul Caistlyn, Sarl Dunegun, Friar Twiblick, Derygull, Ivandis Seergaze, Erysail the Pious, and Essiander Gar-attempted to drive the evil creatures back into what we now know as Morytania. Saradomin blessed the River Salve, making it impassable to the foul things lurking in the swamps, and the brave priests were buried in the temple above the river.",
            )
            "vm_timeline_shield_of_arrav", "vm_timeline_shield_of_arrav_multi", "vm_timeline_no_shield_display" -> {
                val status = get("vm_shield_of_arrav", 0)
                val missingDisplay = status == 0 || status == 2
                val text = buildString {
                    append("4th Age - yr 700-800<br><br>")
                    append("Arrav is probably the best known hero from the 4th Age. In his youth, he was found by a tribe that took him to be a good omen and set up a camp which they called Avarrocka. That camp, in later years, became known as our glorious city of Varrock. Many legends are told of Arrav's heroics later in his life.")
                    append("<br><br>")
                    when (status) {
                        0 -> append("The shield of Arrav was lost in year 143 of the 5th age, when a gang of thieves called the Phoenix Gang broke into Varrock Museum and stole it. Kinq Roald has offered a 1,200gp bounty for the reward of the shield and we keep this display case empty in the hopes it will one day be returned.")
                        1 -> append("The shield you see here actually belonged to Arrav. For a time, this priceless exhibit was lost to us, when a bunch of thieves called the Phoenix Gang broke into the Varrock Museum and stole it. We thank the daring adventurer who recently returned the shield to us.")
                        2 -> append("The shield has been removed for security reasons. So, if you are a hopeful marauding zombie, the museum exit is on the ground floor.")
                        3 -> append("The shield has magical properties and was recently used to destroy a horde of zombies that invaded Varrock.")
                    }
                }
                displayCaseSevenKightsHelms(24, if (missingDisplay) 25350 else 25611, text)
            }
            "vm_timeline_morytania_settler_map" -> displayMorytaniaSettlerMap()
            "vm_timeline_bridge_over_water_model" -> displayCaseBrokenArrow(6, 25572, "4th Age - yr 1937<br><br>Settlers established a town on the River Lum. Across the river they built a bridge and hence the town became called Lumbridge. This heralded more human settlements springing up early in the next age, where humans began to move into the desert, establishing Al Kharid, and towns on both Karamja and Entrana.")
            "vm_timeline_observatory_model" -> displayNoInfo(17)
            "vm_timeline_observatory_model_multi" -> displayCaseCoinSenntisten(
                17,
                25599,
                "5th Age - yr 12<br><br>Scorpius, the early 4th Age astrologer, used this machine (in the Observatory, which he designed) to track the stars and predict the future, gaining dark knowledge. The plans for his machine were lost in the early 4th Age. In the early 5th Age they were rediscovered and from these plans the Observatory was restored. Since then, many have learned the ways of the astrologer.",
            )
            "vm_timeline_werewolf_skin" -> displayCase(
                "vm_timeline_werewolf_skin_model",
                32,
                25618,
                "5th Age - yr 23<br><br>An evil vampire lord started to take control of northern Morytania and his minions visited the various human groups in the area demanding blood tithes, causing widespread panic. Werewolves founded a settlement near to the temple. From the few survivors, we learned that most of the human inhabitants eventually succumbed and paid the blood tithe imposed by the vampyres. Only the inhabitants of Castle Fenkenstrain stood up to this evil influence.",
            )
            "vm_timeline_barbarian_village" -> displayCaseCoinSaranthium(
                1,
                25581,
                "5th Age - yr 62<br><br>The barbarian invaders were running out of steam. They were pretty much forced to make peace or be wiped out, so they agreed to settle down peacefully and make a village on the Misthalin/Asgarnia border, thinking that they could lie low, well-positioned to build up strength and resume their campaign later. This is what is now called the Barbarian Village.",
                rotate = false,
            )
            "vm_timeline_map_of_edgeville" -> displayEdgevilleFounding()
            "vm_timeline_black_knight_armour" -> displayCase(
                "vm_digsite_jewllery_model",
                4,
                25567,
                "5th Age - yr 8<br><br>The kingdom of Asgarnia grew rapidly. King Raddallin, who was one of the tribal leaders of the area, had united many of the smaller tribes and settlements. However, one group within his domain is known as the Kinshra, or Black Knights. They had originally proved cooperative in helping the expansion of the kingdom of Asgarnia, and as a result he'd supported them in building a great fortress on his border to the Wilderness in the north-east. Another group known as the White Knights had also proven to be particularly competent in battle and were now helping him by being the main military force defending his capital city of Falador. However, it turned out that the White Knights and Black Knights had always been bitter rivals. It has been a constant political battle ever since for the kings of Asgarnia to prevent their kingdoms sliding into an out-and-out, very bloody civil war.",
            )
            "vm_timeline_infinity_armour" -> displayCaseVase(
                11,
                25578,
                "5th Age - yr 9<br><br>As the manufacture of runes intensified and Magic became available to people of a great variety of ages and backgrounds. It soon became evident just how dangerous this was, with a great many tragic accidents occurring due to inexperienced wizards. Wizards and victims alike called for something to be done, but it was only due to a tragic accident involving one of the leaders of that time that the Mage Training Arena was constructed, established with all skill levels in mind. They even created magic guardians to run the building.",
            )
            "vm_timeline_silverlight" -> displayCase(
                "vm_timeline_silverlight_model",
                25,
                25612,
                "5th Age - yr 20<br><br>A striking example of early 5th Age weapon-smithing, this steel sword has been magically treated to make it especially powerful against demons. ${
                    if (get("vm_demon_slayer", false)
                    ) {
                        "It originally belonged to a warrior who defended Varrock from the demon Delrith, and was recently used to drive off the very same demon once again. This is a replica; the original Silverlight is in the hands of a private collector."
                    } else {
                        "It is thought to have belonged to a warrior who lived in Varrock. This is a replica; the original Silverlight is in the hands of Sir Prysin of Varrock."
                    }
                }",
            )
            "vm_timeline_barbarian_weapons" -> displayCase(
                "vm_digsite_arrow_heads_model",
                2,
                25570,
                "5th Age - yr 42-62<br><br>The Fremennik mountain tribe has always been opposed to the manufacture of runes, as they feel this should be the work of the gods. A group of the more warmongering of the mountain tribe broke away, led by a warrior called Gunnar, and went on a rampage across northern Kandarin and Asgarnia to put a stop to this 'runecrafting'.<br><br>A group of master smith dwarves, known as the Imcando, were some of the most unfortunate during this period. They had been given many fire and nature runes by the White Knights of Falador to help with their smithing by use of the Superheat Item spell. The invading Fremennik were not happy about all the spellcasting and launched many attacks, reducing the Imcando to near extinction. The invading Fremennik met a fair amount of resistance and their numbers were reduced very significantly over the years.",
            )
            "vm_timeline_bow_and_sword_surrounding_runes" -> displayCaseCoinSenntisten(5, 25571, "5th Age - yr 47<br><br>The invading Fremennik had a number of sympathisers within the kingdom of Misthalin. There were many warriors skilled in the art of the sword and bow that take offence to this art of spellcasting, and taking matters into their own hands, they destroyed the Mage Training Arena.")
            "vm_timeline_zamorakian_symbol" -> displayCase(
                "vm_timeline_zamorakian_symbol_model",
                5,
                25621,
                "5th Age - yr 70<br><br>The mages let Zamorakian followers, skilled in the way of Magic, join their numbers in the 70th year of the 5th Age. This proved to have been a disaster when the dark mages betrayed them and burnt down the tower of magic south of what we now know as Draynor Village. The location of the rune essence and the runecrafting altars was consequently lost. As a result the further campaigns of the barbarians never happened.",
            )
            "vm_timeline_excalibur" -> displayNoInfo(9)
            "vm_timeline_excalibur_multi" -> displayCaseDruidStoneCircle(
                9,
                25575,
                "5th Age - yr 132<br><br>King Arthur and the Knights of the Round Table arrive on RuneScape in year 132 of the 5th Age. Legend says they will return to Britain in its time of greatest need, so they're passing their time on Gielinor until then. King Ulthas saw them as good and rightful men who would do much good in the world, and granted them a large area of land to set up a new Camelot in north-east Kandarin. This is a replica of King Arthur's sword, Excalibur.",
            )
            "vm_timeline_broken_arrow" -> {
                val differentView = get("vm_hazeel_cult_joined", false) && get("vm_hazeel_cult", false)
                displayCaseDruidStoneCircle(
                    7,
                    25573,
                    "5th Age - yr 7<br><br>Using the power of runes, Saradominist humans in Kandarin, led by the Carnillean family, became confident and decided to take on the evil influences resident in the area around what is now known as Ardougne.${if (differentView) "Of particular note was an evil lord by the name of Hazeel, so they stormed his home and defeated the Zamorakins inside. The following morning, the Carnillean forefathers moved into the empty household and claimed it as their own. From this base the Carnilleans began to build the city of Ardougne. From there, they expanded their territory until Kandarin was the largest of the human kingdoms." else "From there, they expanded their territory until Kandarin was the largest of the human kingdoms."}",
                )
            }
            "vm_timeline_phoenix_crossbow_and_black_arm_bands" -> displayCase(
                "vm_timeline_random_stuff_model",
                18,
                25601,
                "5th Age - yr 143<br><br>The Phoenix Gang had a big fight amongst themselves and the Shield of Arrav was broken in half as a result. Shortly afterwards, some gang members decided they didn't want to be part of the Phoenix gang anymore, so split off to form the Black Arm Gang. On their way out they looted what treasures they could, which included one of the halves of the shield. The phoenix and Black Arm gangs became bitter rivals, a rivalry that lasts to this day.",
            )
            "vm_timeline_skeleton" -> displayCase("vm_timeline_skeleton_model", 26, 25613, "5th Age - yr 154<br><br>A strange undead necromancer leads an army of undead out of the Wilderness in an attack upon Varrock. Misthalin is strong enough and still has enough runes that the attack is defeated fairly quickly.")
            "vm_timeline_white_knights_armour" -> displayCase(
                "vm_timeline_white_knights_armour_model",
                33,
                25620,
                "5th Age - yr 162-163<br><br>The old king of Asgarnia, King Vallance, falls very ill. The White Knights took advantage of this and began to take control of Falador and Asgarnia for themselves.<br><br>Year 163, 5th Age - The White Knights declared that the Black knights were no longer to have any political power within Asgarnia. Lord Milton of the Kinshra immediately responds by declaring his Black Knights as being at a state of war with Falador. The two sides engaged in a big battle to the north of Falador-however, both sides were fairly evenly matched, and retreated to their fortresses to build up their forces and devise plans to crush the other, once and for all.",
            )
            "vm_timeline_mage_training_guardian" -> displayCaseVase(12, 25569, "5th Age - yr 169<br><br>A collection of wizards took it upon themselves to resurrect the Mage Training Arena along with all the guardians that were destroyed with it. These guardians were created out of the very same essence as runes, embodying the Magic power and authority needed to oversee the Arena.")
            "vm_timeline_gnome_glider" -> displayCase(
                "vm_timeline_gnome_glider_model",
                10,
                25788,
                "5th Age - yr 20<br><br>What little we know of gnomish history is mainly learned from the glider pilots scattered around RuneScape. They did mention that an old gnome engineer called Oaknock used to make model gliders for his children. Oaknock's son, Yewknock, ended up creating this method of transport with inspiration from his father's models. Captain Daerkin and Captain Ninto were said to be the test pilots of the initial prototypes, though enquiries have proved them to be reticent to give much information. ${
                    if (get(
                            "vm_grand_tree",
                            false,
                        )
                    ) {
                        "Previously, we thought that the strange, 'bird-like', wood and leather gliders that the gnomes find so useful would only support a single gnome in flight. However, due to a brave adventurer heroically aiding the gnomes to save their Grand Tree and earning their gratitude, we find that the gliders can indeed transport at least one human and one gnome."
                    } else {
                        "It appears that these gliders will safely support one gnome in flight."
                    }
                } The staff at the Museum have carefully constructed this replica model.",
                rotate = false,
            )
            "vm_timeline_map_of_ardougne" -> displayArdougneSplit()
            "surok_timeline_tomb" -> displayNoInfo(3)
            "surok_timeline_tomb_multi" -> displayCase(
                "vm_timeline_surok_tomb",
                3,
                23984,
                "5th Age - yr 70<br><br>Donated by one of our citizens, this historic tome details the rise and fall of an order of Zamorakian mages, called the dagon'hai, residing in Varrock. Skilled in the art of chaos Magic, the Dagon'hai fought continually with the priests of Saradomin until the day they were forced out of the city. They were not seen again until they were discovered recently in a tunnel near Varrock.",
            )
            "vm_timeline_glomem_prophecy" -> displayNoInfo(48)
            "vm_timeline_glomem_prophecy_multi" -> displayCase(
                "vm_timeline_glomem_prophecy",
                48,
                32800,
                "5th Age - yr 150<br><br>The Fremennik barbarians discover the whereabouts of several stone tablets, inscribed with an ancient prophecy. In this display, the first four tablets are replicas, but the last is an original, generously donated to the museum. Unfortunately, we still lack the first four, but the main feature of the fifth is the inscribed name of [your Fremennik name]. Who [your Fremennik name] might be is unknown.",
            )
        }
    }

    private fun Player.displayNoInfo(display: Int, text: String = "No information at this time.") = displayCaseTerracottaStatue(display, 25568, text)

    private fun Player.displayCaseTerracottaStatue(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_terracotta_statue_model", display, model, text, rotate)

    private fun Player.displayCaseCoinSaranthium(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_digsite_coin_saranthium_model", display, model, text, rotate)

    private fun Player.displayCaseCoinSenntisten(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_digsite_coin_senntisten_model", display, model, text, rotate)

    private fun Player.displayCaseMap(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_map_model", display, model, text, rotate)

    private fun Player.displayCaseBrokenArrow(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_broken_arrow_model", display, model, text, rotate)

    private fun Player.displayCaseDruidStoneCircle(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_druid_stone_circle_model", display, model, text, rotate)

    private fun Player.displayCaseBowAndSwordRunes(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_bow_and_sword_surrounding_runes_model", display, model, text, rotate)

    private fun Player.displayCasePottery(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_digsite_pottery_model", display, model, text, rotate)

    private fun Player.displayCaseVase(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_digsite_vase_model", display, model, text, rotate)

    private fun Player.displayCaseSevenKightsHelms(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_seven_kights_helms_model", display, model, text, rotate)

    private fun Player.displayCaseRobertTheStrong(display: Int, model: Int, text: String, rotate: Boolean = true) = displayCase("vm_timeline_robert_the_strong_model", display, model, text, rotate)

    private fun Player.displayCase(component: String, display: Int, model: Int, text: String, rotate: Boolean = true) {
        if (!open("vm_timeline")) {
            return
        }
        if (rotate) {
            val componentId = InterfaceDefinitions.getComponent("vm_timeline", component)?.id ?: return
            sendScript("museum_rotate_display", 0, 5, 0, InterfaceDefinition.pack(534, componentId))
        }
        anim("vm_display_case_ponder")
        interfaces.sendModel("vm_timeline", component, model)
        interfaces.sendText("vm_timeline", "display_num", display.toString())
        interfaces.sendText("vm_timeline", "vm_timeline_text", text)
    }

    /**
     * Portrait of King Lathas, plus 15 supporting panels. Text changes once "Making History" is completed.
     */
    private fun Player.displayKingLathasPainting() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        val componentId = InterfaceDefinitions.getComponent("vm_timeline", "vm_timeline_king_lathas_painting_model_1")?.id
        if (componentId != null) {
            sendScript("museum_rotate_display", 0, 5, 0, InterfaceDefinition.pack(534, componentId))
        }
        interfaces.sendModel("vm_timeline", "vm_timeline_king_lathas_painting_model_1", 25520)
        for (i in 0 until 15) {
            interfaces.sendModel("vm_timeline", "vm_timeline_king_lathas_painting_model_${i + 2}", 25532 + i)
        }
        interfaces.sendText(
            "vm_timeline",
            "vm_timeline_text",
            if (questCompleted("making_history")) {
                "5th Age - yr 98<br><br>One of the survivors of the great battle became king in year 98 of the 5th Age. His goal was to listen to people's views and ensure a fair and equal life for everyone. He was the first of a line of kings in Ardougne that continues to this day. The other survivor of the battle founded the marketplace, allowing people to trade their skills and wares under equal rights and opportunities. More information is available at a small museum north of Ardougne."
            } else {
                "5th Age - yr 98<br><br>A portrait of King Lathas of Ardougne."
            },
        )
        interfaces.sendText("vm_timeline", "display_num", "46")
    }

    /**
     * 1st-2nd Age map, 5 panels. Text changes if a brave adventurer has provided new evidence about Zamorak.
     */
    private fun Player.displayMap1st2ndAge() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        val newEvidence = get("vm_staff_of_armadyl", 0) == 2
        val components = arrayOf(
            "vm_timeline_map_of_runescape_model",
            "vm_timeline_map_of_runescape_02_model",
            "vm_timeline_map_of_runescape_03_model",
            "vm_timeline_map_of_runescape_04_model",
            "vm_timeline_map_of_runescape_05_model",
        )
        for (i in 0 until 5) {
            interfaces.sendModel("vm_timeline", components[i], 25594 + i)
        }
        interfaces.sendText(
            "vm_timeline",
            "vm_timeline_text",
            if (newEvidence) {
                "1st - 2nd Ages<br><br>The 1st Age is thought to have been 4,000 years long. The world of RuneScape is said to have been created by the gods Saradomin, Zamorak and Guthix at the start of the 1st Age. However, recent evidence from a brave adventurer suggests that Zamorak was not a god at this point in time and so would not have been able to create worlds. It's thought that for much of this time, the gods were still in the process of forming the world and making the various lands, seas, plants and animals. This map is our approximation of the lands at that time, based upon our existing knowledge of the world"
            } else {
                "1st-2nd Age<br><br>The 1st Age is thought to have been 4,000 years long. The world of RuneScape was created by the gods Saradomin, Zamorak and Guthix at the beginning of the 1st Age. It's thought that, for much of this time, the gods were still in the process of forming the world and making the various lands, seas, plants and animals. <br><br>2nd Age - Research suggests this age was roughly 2,000 years long. This map is our approximation of the lands at that time, based upon our existing knowledge of the world."
            },
        )
        interfaces.sendText("vm_timeline", "display_num", "16")
    }

    /**
     * Early 4th Age map of the human civilisations, 5 panels.
     */
    private fun Player.displayMap4thAgeEarly() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        interfaces.sendModel("vm_timeline", "vm_timeline_world_map_model", 25604)
        interfaces.sendModel("vm_timeline", "vm_timeline_world_map_model_1", 25605)
        interfaces.sendModel("vm_timeline", "vm_timeline_world_map_model_2", 25606)
        interfaces.sendModel("vm_timeline", "vm_timeline_world_map_model_3", 25607)
        interfaces.sendModel("vm_timeline", "vm_timeline_world_map_model_4", 25608)
        interfaces.sendText("vm_timeline", "vm_timeline_text", "4th Age - yr 500-900<br><br>An early 4th Age map showing the human civilisations starting to settle into more permanent villages. After the god wars, all the races began to rebuild their settlements.")
        interfaces.sendText("vm_timeline", "display_num", "34")
        interfaces.sendVisibility("vm_timeline", "vm_timeline_map_text", true)
    }

    /**
     * Torn map showing the settlers' expansion routes into Morytania, 6 panels.
     */
    private fun Player.displayMorytaniaSettlerMap() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        val modelIds = intArrayOf(25589, 25588, 25590, 25591, 25592, 25593)
        val components = arrayOf(
            "vm_timeline_map_of_morytania_model",
            "vm_timeline_map_of_morytania_03_model",
            "vm_timeline_map_of_morytania_04_model",
            "vm_timeline_map_of_morytania_05_model",
            "vm_timeline_map_of_morytania_06_model",
            "vm_timeline_map_of_morytania_07_model",
        )
        for (i in 0 until 6) {
            interfaces.sendModel("vm_timeline", components[i], modelIds[i])
        }
        interfaces.sendText(
            "vm_timeline",
            "vm_timeline_text",
            "4th Age - yr 1777<br><br>Temple records show that human settlers came to the temple on the River Salve looking for new lands in which to settle. While there were legends that the temple on the Salve was blocking great evil, they refused to heed the warnings of the then custodians of the temple, thinking them possibly behind the times or reciting old legends. The lands of Misthalin and Asgarnia weren't the safest places in the world - the human tribes of these times were having constant troubles with goblins, hobgoblins, giants, etc., so how much worse could Morytania have been? So, human settlers passed over the Salve into Morytania. The Museum recently came into possession of this torn map showing the expansion routes of the settlers.",
        )
        interfaces.sendText("vm_timeline", "display_num", "15")
    }

    /**
     * King Roald's renovation of Edgeville (formerly the destroyed town of Paddewwa/"Ghost Town"), 2 panels.
     */
    private fun Player.displayEdgevilleFounding() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        interfaces.sendModel("vm_timeline", "vm_timeline_map_of_edgeville_model", 25587)
        interfaces.sendModel("vm_timeline", "vm_timeline_map_of_edgeville_02_model", 25586)
        interfaces.sendText("vm_timeline", "vm_timeline_text", "5th Age - yr 169<br><br>King Roald of Misthalin renovated a ruined town in north-west Misthalin. It was a town once known as Paddewwa, but was destroyed during the god wars and known to the humans as Ghost Town. King Roald renamed the town Edgeville.")
        interfaces.sendText("vm_timeline", "display_num", "14")
    }

    /**
     * King Ulthas death and the split of Ardougne between his sons Tyras and Lathas, 6 panels.
     */
    private fun Player.displayArdougneSplit() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        val modelIds = intArrayOf(25580, 25579, 25582, 25583, 25584, 25585)
        val components = arrayOf(
            "vm_timeline_map_of_ardougne_model",
            "vm_timeline_map_of_ardougne_02_model",
            "vm_timeline_map_of_ardougne_03_model",
            "vm_timeline_map_of_ardougne_04_model",
            "vm_timeline_map_of_ardougne_05_model",
            "vm_timeline_map_of_ardougne_06_model",
        )
        for (i in 0 until 6) {
            interfaces.sendModel("vm_timeline", components[i], modelIds[i])
        }
        interfaces.sendText("vm_timeline", "vm_timeline_text", "5th Age - yr 136<br><br>King Ulthas of Ardougne dies from an accidental arrow shot while out on a hunting expedition. Ardougne is left to his 2 sons, Tyras and Lathas who decide to split the city between them.")
        interfaces.sendText("vm_timeline", "display_num", "47")
    }
}
