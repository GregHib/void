package rs.dusk.tools.definition.obj.pipe

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras

class ObjectManualChanges : Pipeline.Modifier<MutableMap<Int, Extras>> {

    val uids = mapOf(
        "oak_2" to "oak_dark",
        "oak_3" to "oak_farming",
        "oak_4" to "oak_farming_2",
        "oak_5" to "oak_farming_3",
        "oak_6" to "oak_farming_4",
        "oak_7" to "oak_farming_5",
        "oak_8" to "oak_farming_6",
        "oak_9" to "oak_construction",
        "oak_10" to "oak_pale",
        "oak_11" to "oak_2",
        "oak_12" to "oak_3",
        "oak_13" to "oak_trunk",
        "oak_14" to "oak_trunk_2",
        "oak_15" to "oak_canopy",
        "oak_16" to "oak_canopy_2",
        "oak_17" to "oak_stump_2",
        "oak_18" to "oak_stump_3",
        "oak_19" to "oak_canopy_3",
        "tree_stump_10" to "oak_dark_stump",
        "tree_stump_11" to "oak_stump",
        "tree_stump_38" to "oak_pale_stump",
        "willow_tree" to "willow_tree_farming",
        "willow_tree_2" to "willow_tree_farming_2",
        "willow_tree_3" to "willow_tree_farming_3",
        "willow_tree_4" to "willow_tree_farming_4",
        "willow_tree_5" to "willow_tree_farming_5",
        "willow_tree_6" to "willow_tree_farming_6",
        "willow_tree_7" to "willow_tree_farming_7",
        "willow_tree_8" to "willow_tree_farming_8",
        "willow_tree_9" to "willow_tree_construction",
        "willow_tree_10" to "willow_tree_construction_2",
        "willow_7" to "willow_trunk",
        "willow_8" to "willow_canopy",
        "willow_9" to "willow_canopy_2",
        "willow_10" to "willow_stump",
        "willow_11" to "willow_canopy_3"
    )

    // TODO post-process replace id's with uids

    val stumps = mapOf(
        "oak" to 1356,
        "oak_2" to 1356,
        "oak_dark" to 1355,
        "oak_pale" to 12007,
        "oak_trunk" to 38741,
        "oak_trunk_2" to 38754,
        "willow" to 5554,
        "willow_2" to 5554,
        "willow_3" to 5554,
        "willow_4" to 5554,
        "willow_5" to 1350,
        "willow_6" to 38725,
        "willow_7" to 38725,
        "willow_12" to 38725
    )

    val canopies = mapOf(
        "oak_trunk" to 38736,
        "oak_trunk_2" to 38739,
        "willow_6" to 38717,
        "willow_7" to 38718,
        "willow_12" to 38717
    )

    override fun modify(content: MutableMap<Int, Extras>): MutableMap<Int, Extras> {
        content.forEach { (_, content) ->
            val (builder, extras) = content
            uids.forEach { (match, replacement) ->
                if (builder.uid == match) {
                    builder.uid = replacement
                }
            }
            val uid = builder.uid
            stumps.forEach { (tree, stumpId) ->
                if(uid == tree) {
                    extras["stump"] = stumpId
                }
            }
            canopies.forEach { (tree, stumpId) ->
                if(uid == tree) {
                    extras["canopy"] = stumpId
                }
            }
        }
        // Manual changes go here
        return content
    }
}