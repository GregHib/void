package world.gregs.voidps.tools.definition.item.pipe.extra

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.wiki.model.Infobox.indexSuffix

class ItemManualChanges : Pipeline.Modifier<MutableMap<Int, Extras>> {

    val examines: Map<String, Int> = mapOf(
        "overgrown_cat" to 3,
        "overgrown_hellcat" to 3,
        "hellcat" to 2,
        "hell-kitten" to 1,
        "lazy_hellcat" to 5,
        "wily_hellcat" to 4,
        "terrier" to 2,
        "greyhound" to 2,
        "labrador" to 2,
        "dalmatian" to 2,
        "sheepdog" to 2,
        "bulldog" to 2,
        "baby_monkey" to 1,
        "raven" to 2,
        "baby_raccoon" to 1,
        "raccoon" to 2,
        "baby_gecko" to 1,
        "gecko" to 2,
        "baby_giant_crab" to 1,
        "giant_crab" to 2,
        "baby_squirrel" to 2,
        "squirrel" to 1,
        "baby_penguin" to 1,
        "penguin" to 2,
        "vulture_chick" to 1,
        "vulture" to 2,
        "worn-out_adamant_gauntlets" to 2,
        "worn-out_rune_gauntlets" to 2,
        "milk_tooth_creature_2" to 1,
        "milk_tooth_creature" to 2,
        "rune_guardian" to 1,
        "zamorak_chick" to 1,
        "zamorak_bird" to 2,
        "zamorak_hawk" to 3,
        "saradomin_chick" to 1,
        "saradomin_bird" to 2,
        "saradomin_owl" to 3,
        "guthix_chick" to 1,
        "guthix_bird" to 2,
        "guthix_raptor" to 3,
        "dragon_hatchling" to 1,
        "hatchling_dragon" to 1,
        "baby_dragon" to 2,
    )

    val npcs: Map<String, Int> = mapOf(
        "pet_cat_2" to 2,
        "pet_cat_3" to 3,
        "pet_cat_4" to 4,
        "pet_cat_5" to 5,
        "pet_cat_6" to 6,
        "pet_cat" to 1,
    )

    val pets: Map<String, List<Int>> = mapOf(
        "terrier_puppy" to listOf(6894, 6858),
        "terrier_puppy_2" to listOf(6894, 6858),
        "terrier_puppy_3" to listOf(7237),
        "terrier_puppy_4" to listOf(7239),
        "terrier" to listOf(6959, 9190),
        "terrier_2" to listOf(7238, 9191),
        "terrier_3" to listOf(7240, 9192),
        "chameleon" to listOf(6923, 9262),
        "chameleon_2" to listOf(6923, 9262),
        "baby_chameleon" to listOf(6922),
        "baby_dragon_red" to listOf(6901, 9208),
        "baby_dragon_blue" to listOf(6903, 9209),
        "baby_dragon_green" to listOf(6905, 9210),
        "baby_dragon_black" to listOf(6907, 9211),
        "hatchling_dragon_red" to listOf(6900),
        "hatchling_dragon_blue" to listOf(6902),
        "hatchling_dragon_green" to listOf(6904),
        "hatchling_dragon_black" to listOf(6906),
        "penguin_grey" to listOf(6909, 9233, 6910),
        "penguin_brown" to listOf(7314, 9234, 7315),
        "penguin_blue" to listOf(7317, 9235, 7318),
        "baby_penguin_grey" to listOf(6908),
        "baby_penguin_brown" to listOf(7313),
        "baby_penguin_blue" to listOf(7316),
        "raven_blue" to listOf(6912, 9239),
        "raven_blue_crest" to listOf(7262, 9240),
        "raven_black" to listOf(7264, 9241),
        "raven_black_crest" to listOf(7266, 9242),
        "raven_red" to listOf(7268, 9243),
        "raven_red_crest" to listOf(7270, 9244),
        "raven_chick_blue" to listOf(6911),
        "raven_chick_blue_crest" to listOf(7261),
        "raven_chick_black" to listOf(7263),
        "raven_chick_black_crest" to listOf(7265),
        "raven_chick_red" to listOf(7267),
        "raven_chick_red_crest" to listOf(72694),
        "raccoon_grey" to listOf(6914, 9236),
        "raccoon_brown" to listOf(7272, 9237),
        "raccoon_red" to listOf(7274, 9238),
        "baby_raccoon_grey" to listOf(6913, 6997),
        "baby_raccoon_brown" to listOf(7271, 7275),
        "baby_raccoon_red" to listOf(7273, 7276),
        "giant_crab_red" to listOf(6948, 9218),
        "giant_crab_beige" to listOf(7294, 9219),
        "giant_crab_grey" to listOf(7296, 9220),
        "giant_crab_green" to listOf(7298, 9221),
        "giant_crab_yellow" to listOf(7300, 9222),
        "baby_giant_crab_red" to listOf(6947),
        "baby_giant_crab_beige" to listOf(7293),
        "baby_giant_crab_grey" to listOf(7295),
        "baby_giant_crab_green" to listOf(7297),
        "baby_giant_crab_yellow" to listOf(7299),
        "greyhound_brown" to listOf(6961, 9193),
        "greyhound_grey" to listOf(7242, 9194),
        "greyhound_dark" to listOf(7244, 9195),
        "greyhound_puppy_brown" to listOf(6960),
        "greyhound_puppy_grey" to listOf(7241),
        "greyhound_puppy_dark" to listOf(7243),
        "labrador_yellow" to listOf(6963, 9196),
        "labrador_black" to listOf(7246, 9197),
        "labrador_grey" to listOf(7248, 9198),
        "labrador_puppy_yellow" to listOf(6962, 6895),
        "labrador_puppy_black" to listOf(7245),
        "labrador_puppy_grey" to listOf(7247),
        "dalmatian_black" to listOf(6965, 9199),
        "dalmatian_blue" to listOf(7250, 9200),
        "dalmatian_red" to listOf(7252, 9201),
        "dalmatian_puppy_black" to listOf(6964, 6896),
        "dalmatian_puppy_blue" to listOf(7249),
        "dalmatian_puppy_red" to listOf(7251),
        "sheepdog_black" to listOf(6967, 9202),
        "sheepdog_grey" to listOf(7254, 9203),
        "sheepdog_yellow" to listOf(7256, 9204),
        "sheepdog_puppy_black" to listOf(6966, 2311),
        "sheepdog_puppy_grey" to listOf(7253),
        "sheepdog_puppy_yellow" to listOf(7255),
        "bulldog_white" to listOf(6968, 9205),
        "bulldog_navy" to listOf(7257, 9206),
        "bulldog_grey" to listOf(7258, 9207),
        "bulldog_puppy_white" to listOf(6969),
        "bulldog_puppy_navy" to listOf(7259),
        "bulldog_puppy_grey" to listOf(7260),
        "platypus_brown" to listOf(7015, 7021, 9304),
        "platypus_tan" to listOf(7016, 7022, 9305),
        "platypus_grey" to listOf(7017, 7023, 9306),
        "baby_platypus_brown" to listOf(7018, 7024),
        "baby_platypus_tan" to listOf(7019, 7025),
        "baby_platypus_grey" to listOf(7020, 7026),
        "vulture_grey" to listOf(6946, 9256),
        "vulture_grey_striped" to listOf(7320, 9257),
        "vulture_brown" to listOf(7322, 9258),
        "vulture_brown_striped" to listOf(7324, 9259),
        "vulture_pink" to listOf(7326, 9260),
        "vulture_pink_striped" to listOf(7328, 9261),
        "vulture_chick_grey" to listOf(6945),
        "vulture_chick_grey_striped" to listOf(7319),
        "vulture_chick_brown" to listOf(7321),
        "vulture_chick_brown_striped" to listOf(7323),
        "vulture_chick_pink" to listOf(7325),
        "vulture_chick_pink_striped" to listOf(7327),
        "squirrel_light_grey" to listOf(6920, 9250),
        "squirrel_light_brown" to listOf(7302, 9251),
        "squirrel_white" to listOf(7304, 9252),
        "squirrel_dark_grey" to listOf(7306, 9253),
        "squirrel_dark_brown" to listOf(7308, 9254),
        "monkey_grey_and_beige" to listOf(6943, 9223),
        "monkey_brown_and_beige" to listOf(7211, 9224),
        "monkey_black_and_brown" to listOf(7213, 9225),
        "monkey_beige" to listOf(7215, 9226),
        "monkey_tan_and_beige" to listOf(7217, 9227),
        "monkey_grey_and_white" to listOf(7219, 9228),
        "monkey_blue_and_grey" to listOf(7221, 9229),
        "monkey_black_and_white" to listOf(7223, 9230),
        "monkey_orange" to listOf(7225, 9231),
        "monkey_blue_and_white" to listOf(7227, 9232),
        "baby_monkey_grey_and_beige" to listOf(6942, 6944),
        "baby_monkey_brown_and_beige" to listOf(7210, 7228),
        "baby_monkey_black_and_brown" to listOf(7212, 7229),
        "baby_monkey_beige" to listOf(7214, 7230),
        "baby_monkey_tan_and_beige" to listOf(7216, 7231),
        "baby_monkey_grey_and_white" to listOf(7218, 7232),
        "baby_monkey_blue_and_grey" to listOf(7220, 7233),
        "baby_monkey_black_and_white" to listOf(7222, 7234),
        "baby_monkey_orange" to listOf(7224, 7235),
        "baby_monkey_blue_and_white" to listOf(7226, 7236),
        "fishbowl_blue" to listOf(8737),
        "fishbowl_green" to listOf(8738),
        "fishbowl_red" to listOf(8739),
        "gecko_orange_2" to listOf(6916, 6918),
        "gecko_orange" to listOf(7281, 9214, 7289),
        "gecko_green" to listOf(7282, 9215, 7290),
        "gecko_red" to listOf(7283, 9216),
        "gecko_blue" to listOf(7284, 9217, 7292),
        "baby_gecko_orange_2" to listOf(6915, 6917),
        "baby_gecko_orange" to listOf(7277, 7285),
        "baby_gecko_green" to listOf(7278, 7286),
        "baby_gecko_red" to listOf(7279, 7287),
        "baby_gecko_blue" to listOf(7280, 7288),
        "zamorak_hawk" to listOf(6954, 9186),
        "saradomin_owl" to listOf(6951, 7816),
        "greyhound_puppy_brown_2" to emptyList(),
        "labrador_puppy_yellow_2" to emptyList(),
        "dalmatian_puppy_black_2" to emptyList(),
        "sheepdog_puppy_black_2" to emptyList(),
        "bulldog_puppy_white_2" to emptyList(),
        "platypus_brown_2" to emptyList(),
        "vulture_grey_2" to emptyList(),
        "monkey_grey_and_beige_2" to emptyList(),
        "gecko_red_2" to emptyList(),
    )

    val uids = mapOf(
        "wizard_boots_t" to "wizard_boots",
        "baby_dragon" to "baby_dragon_red",
        "baby_dragon_2" to "baby_dragon_blue",
        "baby_dragon_3" to "baby_dragon_green",
        "baby_dragon_4" to "baby_dragon_black",
        "hatchling_dragon" to "hatchling_dragon_red",
        "hatchling_dragon_2" to "hatchling_dragon_blue",
        "hatchling_dragon_3" to "hatchling_dragon_green",
        "hatchling_dragon_4" to "hatchling_dragon_black",
        "penguin" to "penguin_grey",
        "penguin_2" to "penguin_brown",
        "penguin_3" to "penguin_blue",
        "penguin_4" to "penguin_grey_2",
        "baby_penguin" to "penguin_grey",
        "baby_penguin_2" to "penguin_brown",
        "baby_penguin_3" to "penguin_blue",
        "raven" to "raven_blue",
        "raven_2" to "raven_blue_crest",
        "raven_3" to "raven_black",
        "raven_4" to "raven_black_crest",
        "raven_5" to "raven_red",
        "raven_6" to "raven_red_crest",
        "raven_chick" to "raven_chick_blue",
        "raven_chick_2" to "raven_chick_blue_crest",
        "raven_chick_3" to "raven_chick_black",
        "raven_chick_4" to "raven_chick_black_crest",
        "raven_chick_5" to "raven_chick_red",
        "raven_chick_6" to "raven_chick_red_crest",
        "raccoon" to "raccoon_grey",
        "raccoon_2" to "raccoon_brown",
        "raccoon_3" to "raccoon_red",
        "baby_raccoon" to "baby_raccoon_grey_2",
        "baby_raccoon_2" to "baby_raccoon_grey",
        "baby_raccoon_3" to "baby_raccoon_brown",
        "baby_raccoon_4" to "baby_raccoon_red",
        "giant_crab" to "giant_crab_red",
        "giant_crab_2" to "giant_crab_beige",
        "giant_crab_3" to "giant_crab_grey",
        "giant_crab_4" to "giant_crab_green",
        "giant_crab_5" to "giant_crab_yellow",
        "baby_giant_crab" to "baby_giant_crab_red",
        "baby_giant_crab_2" to "baby_giant_crab_beige",
        "baby_giant_crab_3" to "baby_giant_crab_grey",
        "baby_giant_crab_4" to "baby_giant_crab_green",
        "baby_giant_crab_5" to "baby_giant_crab_yellow",
        "greyhound" to "greyhound_brown",
        "greyhound_2" to "greyhound_grey",
        "greyhound_3" to "greyhound_dark",
        "greyhound_puppy" to "greyhound_puppy_brown_2",
        "greyhound_puppy_2" to "greyhound_puppy_brown",
        "greyhound_puppy_3" to "greyhound_puppy_grey",
        "greyhound_puppy_4" to "greyhound_puppy_dark",
        "labrador" to "labrador_yellow",
        "labrador_2" to "labrador_black",
        "labrador_3" to "labrador_grey",
        "labrador_puppy" to "labrador_puppy_yellow_2",
        "labrador_puppy_2" to "labrador_puppy_yellow",
        "labrador_puppy_3" to "labrador_puppy_black",
        "labrador_puppy_4" to "labrador_puppy_grey",
        "dalmatian" to "dalmatian_black",
        "dalmatian_2" to "dalmatian_blue",
        "dalmatian_3" to "dalmatian_red",
        "dalmatian_puppy" to "dalmatian_puppy_black_2",
        "dalmatian_puppy_2" to "dalmatian_puppy_black",
        "dalmatian_puppy_3" to "dalmatian_puppy_blue",
        "dalmatian_puppy_4" to "dalmatian_puppy_red",
        "sheepdog" to "sheepdog_black",
        "sheepdog_2" to "sheepdog_grey",
        "sheepdog_3" to "sheepdog_yellow",
        "sheepdog_puppy" to "sheepdog_puppy_black_2",
        "sheepdog_puppy_2" to "sheepdog_puppy_black",
        "sheepdog_puppy_3" to "sheepdog_puppy_grey",
        "sheepdog_puppy_4" to "sheepdog_puppy_yellow",
        "bulldog_3" to "bulldog_white",
        "bulldog" to "bulldog_navy",
        "bulldog_2" to "bulldog_grey",
        "bulldog_puppy" to "bulldog_puppy_white_2",
        "bulldog_puppy_2" to "bulldog_puppy_white",
        "bulldog_puppy_3" to "bulldog_puppy_grey",
        "bulldog_puppy_4" to "bulldog_puppy_navy",
        "platypus_4" to "platypus_brown_2",
        "platypus" to "platypus_brown",
        "platypus_2" to "platypus_tan",
        "platypus_3" to "platypus_grey",
        "baby_platypus" to "baby_platypus_brown",
        "baby_platypus_2" to "baby_platypus_tan",
        "baby_platypus_3" to "baby_platypus_grey",
        "vulture" to "vulture_grey_2",
        "vulture_2" to "vulture_grey",
        "vulture_3" to "vulture_grey_striped",
        "vulture_4" to "vulture_brown",
        "vulture_5" to "vulture_brown_striped",
        "vulture_6" to "vulture_pink",
        "vulture_7" to "vulture_pink_striped",
        "vulture_chick" to "vulture_grey",
        "vulture_chick_2" to "vulture_grey_striped",
        "vulture_chick_3" to "vulture_brown",
        "vulture_chick_4" to "vulture_brown_striped",
        "vulture_chick_5" to "vulture_pink",
        "vulture_chick_6" to "vulture_pink_striped",
        "squirrel" to "squirrel_light_grey",
        "squirrel_2" to "squirrel_light_brown",
        "squirrel_3" to "squirrel_white",
        "squirrel_4" to "squirrel_dark_grey",
        "squirrel_5" to "squirrel_dark_brown",
        "monkey_12" to "monkey_grey_and_beige_2",
        "monkey" to "monkey_grey_and_beige",
        "monkey_2" to "monkey_brown_and_beige",
        "monkey_3" to "monkey_black_and_brown",
        "monkey_4" to "monkey_beige",
        "monkey_5" to "monkey_tan_and_beige",
        "monkey_6" to "monkey_grey_and_white",
        "monkey_7" to "monkey_blue_and_grey",
        "monkey_8" to "monkey_black_and_white",
        "monkey_9" to "monkey_orange",
        "monkey_10" to "monkey_blue_and_white",
        "baby_monkey" to "baby_monkey_grey_and_beige",
        "baby_monkey_2" to "baby_monkey_brown_and_beige",
        "baby_monkey_3" to "baby_monkey_black_and_brown",
        "baby_monkey_4" to "baby_monkey_beige",
        "baby_monkey_5" to "baby_monkey_tan_and_beige",
        "baby_monkey_6" to "baby_monkey_grey_and_white",
        "baby_monkey_7" to "baby_monkey_blue_and_grey",
        "baby_monkey_8" to "baby_monkey_black_and_white",
        "baby_monkey_9" to "baby_monkey_orange",
        "baby_monkey_10" to "baby_monkey_blue_and_white",
        "fishbowl_2" to "fishbowl_blue",
        "fishbowl_3" to "fishbowl_green",
        "fishbowl_4" to "fishbowl_red",
        "gecko_6" to "gecko_red_2",
        "gecko" to "gecko_orange_2",
        "gecko_2" to "gecko_orange",
        "gecko_3" to "gecko_green",
        "gecko_4" to "gecko_red",
        "gecko_5" to "gecko_blue",
        "baby_gecko" to "baby_gecko_orange_2",
        "baby_gecko_2" to "baby_gecko_orange",
        "baby_gecko_3" to "baby_gecko_green",
        "baby_gecko_4" to "baby_gecko_red",
        "baby_gecko_5" to "baby_gecko_blue",
    )

    val numberRegex = "([0-9]+)".toRegex()

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (id, content) ->
            val (builder, extras) = content
            uids.forEach { (match, replacement) ->
                if (builder.uid == match) {
                    builder.uid = replacement
                }
            }
            val uid = builder.uid
            if (extras.containsKey("examine2")) {
                extras["examine2"] = (extras.getValue("examine2") as String).replace("intrument", "instrument")
            }
            if (uid.contains("_puppy")) {
                content.selectExamine(uid, 1)
            }
            examines.forEach { (uid, index) ->
                content.selectExamine(uid, index)
            }
            npcs.forEach { (uid, index) ->
                content.selectNPC(uid, index)
            }
            if (uid.startsWith("clue_scroll_")) {
                val examines = extras.count { it.key.startsWith("examine") }
                val suffix = getSuffixNumber(uid)
                if (examines > 0) {
                    val index = suffix.rem(examines)
                    content.selectExamine(uid, index)
                }
            }
            if (uid.startsWith("enchanted_lyre") && !uid.endsWith("0")) {
                content.selectExamine(uid, 1)
            }
            if (uid.startsWith("lazy_cat")) {
                content.selectExamine(uid, 3)
            }
            if (uid.startsWith("wily_cat")) {
                content.selectExamine(uid, 1)
            }
            if (uid.startsWith("black_mask")) {
                content.selectExamine(uid, if (uid.endsWith("_0")) 2 else 1)
            }
            if (uid.startsWith("combat_bracelet_") && !uid.endsWith("0")) {
                content.selectExamine(uid, 2)
            }
            if ((uid.contains("spikeshield") || uid.contains("berserker_shield")) && !uid.endsWith("_0")) {
                content.selectExamine(uid, 2)
            }
            if (uid.contains("dragonhide_coif") && !uid.endsWith("_0")) {
                content.selectExamine(uid, 1)
            }
            if (uid.startsWith("seaweed_net_") && !uid.endsWith("_0")) {
                content.selectExamine(uid, 2)
            }
            if (uid.startsWith("ferocious_ring")) {
                val suffix = 6 - getSuffixNumber(uid)
                content.selectExamine(uid, suffix)
            }
            if (uid.startsWith("tooth_creature")) {
                content.selectExamine(
                    uid,
                    when {
                        uid.endsWith("_health") -> 2
                        uid.endsWith("_decayed") -> 3
                        else -> 1
                    },
                )
            }
            if (id == 20823) { // Primal plate legs
                extras["examine"] = "${extras["examine"]}${extras.remove("examine2")}"
            }
            if (uid.startsWith("reward_book")) {
                content.selectExamine(uid, 1)
            }
            if (uid.startsWith("elegant_shirt")) {
                content.selectExamine(uid, 3)
                val suffix = getSuffixNumber(uid)
                extras["examine"] = (extras["examine"] as String).replace(
                    "[colour]",
                    when (suffix) {
                        1 -> "red"
                        2 -> "blue"
                        3 -> "green"
                        else -> "black"
                    },
                )
            }
            if (uid.contains("shelves")) {
                content.selectExamine(uid, if (uid.endsWith("_3")) 2 else 1)
            }

            if (uid.startsWith("baby_dragon") && extras.containsKey("examine")) {
                extras["examine"] = (extras["examine"] as String).replace("[colour]", uid.removePrefix("baby_dragon_"))
            } else if (uid.startsWith("hatchling_dragon") && extras.containsKey("examine")) {
                extras["examine"] = (extras["examine"] as String).replace("[colour]", uid.removePrefix("hatchling_dragon_"))
            }

            pets.forEach { (name, ids) ->
                if (uid == name) {
                    forAll {
                        extras.remove("npc$it")
                    }
                    ids.forEachIndexed { index, id ->
                        extras["npc${if (index == 0) "" else (index + 1).toString()}"] = id
                    }
                }
            }

            if (extras.containsKey("examine")) {
                extras["examine"] = (extras["examine"] as String).replace("Puppy - ", "")
            }
            if (extras.containsKey("examine2")) {
                content.selectExamine(uid, 1)
            }
        }
        // Manual changes go here
        return content
    }

    private fun getSuffixNumber(text: String): Int = numberRegex.find(text)?.groupValues?.last()?.toIntOrNull() ?: 0

    private fun Extras.selectExamine(uid: String, vararg index: Int) = select(uid, "examine", *index)

    private fun Extras.selectNPC(uid: String, vararg index: Int) = select(uid, "npc", *index)

    private fun Extras.selectDestroy(uid: String, vararg index: Int) = select(uid, "destroy", *index)

    private fun Extras.select(uid: String, name: String, vararg indices: Int) {
        if (first.uid.startsWith(uid)) {
            val selections = indices.map { indexSuffix(name, it - 1) }.mapNotNull { second[it] }
            if (selections.isNotEmpty()) {
                forAll {
                    second.remove("$name$it")
                }
                selections.forEachIndexed { index, any ->
                    second[indexSuffix(name, index)] = any
                }
            }
        }
    }

    private fun forAll(function: (String) -> Unit) {
        for (i in 1..20) {
            function(if (i == 1) "" else i.toString())
        }
    }
}
