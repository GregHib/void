package content.skill.construction

import content.skill.construction.House.Companion.notImplemented
import world.gregs.voidps.engine.Script

/**
 * Workshop furniture: workbenches for making flatpacks, clockmaker's benches, the helmet pluming stand and heraldry easels
 */
class HouseWorkshop : Script {
    init {
        objectOperate("Work-at", WORKBENCHES) {
            notImplemented()
        }

        objectOperate("Upgrade", UPGRADABLE) {
            notImplemented()
        }

        objectOperate("Craft", "crafting_table_*") {
            notImplemented()
        }

        objectOperate("Make-helmet", "pluming_stand") {
            notImplemented()
        }

        objectOperate("Use", "shield_easel,banner_easel") {
            notImplemented()
        }
    }

    companion object {
        private const val WORKBENCHES = "wooden_workbench,oak_workbench,steel_framed_bench,bench_with_vice,bench_with_lathe"
        private const val UPGRADABLE = "steel_framed_bench,bench_with_vice,crafting_table_1,crafting_table_2,crafting_table_3,tool_store_1,tool_store_2,tool_store_3,tool_store_4"
    }
}
