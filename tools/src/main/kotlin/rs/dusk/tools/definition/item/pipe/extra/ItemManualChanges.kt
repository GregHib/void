package rs.dusk.tools.definition.item.pipe.extra

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class ItemManualChanges : Pipeline.Modifier<Extras> {

    val examines: Map<String, Int> = mapOf(
        "pet_kitten" to 1,
        "pet_kitten_2" to 3,
        "pet_kitten_3" to 1,
        "pet_kitten_4" to 3,
        "pet_kitten_5" to 0,
        "pet_kitten_6" to 3,
        "pet_cat" to 2,
        "pet_cat_2" to 5,
        "pet_cat_3" to 2,
        "pet_cat_4" to 5,
        "pet_cat_5" to 2,
        "pet_cat_6" to 5,
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
        "baby_dragon" to 2
    )

    val npcs: Map<String, Int> = mapOf(
        "pet_cat" to 1,
        "pet_cat_2" to 2,
        "pet_cat_3" to 3,
        "pet_cat_4" to 4,
        "pet_cat_5" to 5,
        "pet_cat_6" to 6
    )
    val numberRegex = "([0-9]+)".toRegex()
    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, _, _, _, _, _, _, _, uid) = builder
        if (extras.containsKey("examine2")) {
            extras["examine2"] = (extras.getValue("examine2") as String).replace("intrument", "instrument")
        }
        replaceName(extras)
        examines.forEach { (id, index) ->
            content.selectExamine(id, index)
        }
        npcs.forEach { (id, index) ->
            content.selectNPC(id, index)
        }
        if (uid.startsWith("clue_scroll_")) {
            val examines = extras.count { it.key.startsWith("examine") }
            val suffix = getSuffixNumber(uid)
            val index = suffix.rem(examines)
            content.selectExamine(uid, index)
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
        if (uid.contains("_puppy")) {
            content.selectExamine(uid, 1)
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
            content.selectExamine(uid,
                when {
                    uid.endsWith("_health") -> 2
                    uid.endsWith("_decayed") -> 3
                    else -> 1
                }
            )
        }
        if (id == 20823) {//Primal platelegs
            extras["examine"] = "${extras["examine"]}${extras.remove("examine2")}"
        }
        if (uid.startsWith("reward_book")) {
            content.selectExamine(uid, 1)
        }
        if (uid.startsWith("elegant_shirt")) {
            content.selectExamine(uid, 3)
            val suffix = getSuffixNumber(uid)
            extras["examine"] = (extras["examine"] as String).replace("[colour]", when (suffix) {
                1 -> "red"
                2 -> "blue"
                3 -> "green"
                else -> "black"
            })
        }
        if (uid.contains("shelves")) {
            content.selectExamine(uid, if (uid.endsWith("_3")) 2 else 1)
        }
        if (extras.containsKey("examine2")) {
            content.selectExamine(uid, 1)
        }
        // Manual changes go here
        return content
    }

    private fun getSuffixNumber(text: String): Int {
        return numberRegex.find(text)?.groupValues?.last()?.toIntOrNull() ?: 0
    }

    private fun Extras.selectExamine(uid: String, index: Int) = select(uid, "examine", index)

    private fun Extras.selectNPC(uid: String, index: Int) = select(uid, "npc", index)

    private fun Extras.selectDestroy(uid: String, index: Int) = select(uid, "destroy", index)

    private fun Extras.select(uid: String, name: String, index: Int) {
        if (first.uid.startsWith(uid)) {
            val key = "$name${if (index > 1) index.toString() else ""}"
            val choice = second[key]
            if (choice != null) {
                forAll {
                    second.remove("$name$it")
                }
                second[name] = choice
            }
        }
    }

    private fun replaceName(extras: MutableMap<String, Any>) {
        forAll {
            if (extras.containsKey("examine$it")) {
                extras["examine$it"] = (extras.getValue("examine$it") as String).replace("RuneScape", "Dusk")
            }
            if (extras.containsKey("destroy$it")) {
                extras["destroy$it"] = (extras.getValue("destroy$it") as String).replace("RuneScape", "Dusk")
            }
        }
    }

    private fun forAll(function: (String) -> Unit) {
        for (i in 1..10) {
            function(if (i == 1) "" else i.toString())
        }
    }
}