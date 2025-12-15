package content.skill.farming

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.entity.obj.GameObject

internal fun GameObject.patchName(): String {
    val patchType = id.removePrefix("farming_").substringBeforeLast("_patch").toLowerSpaceCase()
    return if (patchType == "veg") "allotment" else "$patchType patch"
}

object FarmingPatches {
    // Multiplier (for farming.growth.mins) to list of varbits
    val patches = mutableMapOf(
        // flowers, saplings
        1 to listOf(
            "farming_evil_turnip_patch_draynor",
            "farming_flower_patch_falador",
            "patch_wilderness_flower",
            "farming_flower_patch_catherby",
            "farming_flower_patch_ardougne",
            "patch_herblore_habitat_vine_flower",
            "farming_flower_patch_morytania",
        ),
        2 to listOf(
            // allotments
            "farming_veg_patch_falador_nw",
            "farming_veg_patch_falador_se",
            "farming_veg_patch_catherby_north",
            "farming_veg_patch_catherby_south",
            "farming_veg_patch_ardougne_north",
            "farming_veg_patch_ardougne_south",
            "farming_veg_patch_morytania_nw",
            "farming_veg_patch_morytania_se",
            // hops
            "farming_hops_patch_lumbridge",
            "patch_harmony_allotment",
            "farming_hops_patch_entrana",
            "farming_hops_patch_seers_village",
            "farming_hops_patch_yannile",
        ),
        4 to listOf(
            // herbs
            "farming_herb_patch_falador",
            "farming_herb_patch_catherby",
            "farming_herb_patch_ardougne",
            "farming_herb_patch_morytania",
            "farming_herb_patch_my_arm",
            "patch_herblore_habitat_island_vine_herb",
            "patch_herblore_habitat_vine_herb",
            "patch_herblore_habitat_vine_bush",
            // bushes
            "farming_bush_patch_varrock",
            "farming_bush_patch_rimmington",
            "farming_bush_patch_etceteria",
            "farming_bush_patch_ardougne",
        ),
        // trees, mushrooms
        8 to listOf(
            "farming_tree_patch_lumbridge",
            "farming_tree_patch_varrock",
            "farming_tree_patch_falador",
            "patch_canifis_mushroom",
            "farming_tree_patch_taverley",
            "farming_tree_patch_gnome_stronghold",
        ),
        // belladonna, cactus
        16 to listOf(
            "farming_belladonna_patch_draynor",
            "farming_cactus_patch_al_kharid",
        ),
        // fruit_trees, calquat
        32 to listOf(
            "farming_fruit_tree_patch_catherby",
            "patch_ardougne_jade_vine",
            "farming_calquat_tree_patch_tai_bwo_wannai",
            "patch_herblore_habitat_fruit_tree",
            "farming_fruit_tree_patch_gnome_village",
            "farming_fruit_tree_patch_gnome_stronghold",
            "farming_fruit_tree_patch_brimhaven",
            "farming_fruit_tree_patch_lletya",
        ),
        // spirit tree
        64 to listOf(
            "farming_spirit_tree_patch_port_sarim",
            "farming_spirit_tree_patch_etceteria",
            "farming_spirit_tree_patch_brimhaven",
        ),
    )
}
