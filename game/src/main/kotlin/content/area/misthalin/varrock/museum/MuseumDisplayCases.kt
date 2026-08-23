package content.area.misthalin.varrock.museum

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
        objectOperate("Study", "vm_timeline_terracotta_statue_multi,vm_timeline_no_display") { (target) ->
            if (target.id != "vm_timeline_terracotta_statue") {
                noInterest()
                return@objectOperate
            }
            studyDisplayCase()
        }

        objectOperate("Study", "vm_timeline_no_display") {
            displayCase("vm_timeline_barbarian_village_model", 45, 25522, "Item removed for cleaning.")
        }

        objectOperate("Study", "vm_timeline_soil_layers") {
            displayCase("vm_timeline_soil_layers_model", 27, 25614, "The Layers of Archaeology<br>Soil Layers<br><br>O horizon - The top layer of soil is made up mostly of leaves and decomposed organic matter.<br><br>A horizon (topsoil) - Plants grow in this dark-coloured layer, which is made up of decomposed organic matter mixed with mineral particles.<br><br>E horizon - This eluviation (leaching) layer has a light colour and is made up of sand and silt. We often find significant archaeological artefacts in this layer.<br><br>B horizon (subsoil) - Contains clay and mineral deposits that it receives from layers above, when water drips through.<br><br>C horizon - Regolith consists of slightly broken up bedrock. Plant roots do not penetrate into this layer.<br><br>R horizon - The bedrock layer that is beneath all the other layers.")
        }

        objectOperate("Study", "vm_digsite_finds_pottery_before_digsite_multi") {
            displayCase("vm_timeline_bridge_over_water_model", 22, 25527, "Two beautiful examples of the many items of pottery found around the Dig Site. There appear to be Saradominist markings upon these artefacts, indicating that this was at one time a Saradominist city. This pottery appears to be dated around the beginning of the 4th Age.")
        }

        objectOperate("Study", "vm_digsite_finds_jewellery") {
            displayCase("vm_timeline_black_knight_armour_model", 40, 25525, "Fine silver and gold jewellery has been found concealed in one of the many urns scattered around the Dig Site. Not much is known about it, although most of the items do have Saradominist markings.")
        }

        objectOperate("Study", "vm_digsite_finds_vase") {
            displayCase("vm_timeline_mage_training_guardian_model", 38, 25531, "One of the few vases found in good condition. Its markings show some kind of celebration to Saradomin.")
        }

        objectOperate("Study", "vm_digsite_finds_arrow_heads") {
            displayCase("vm_timeline_barrows_suit_of_arms_model", 41, 25524, "Arrowheads of crude bronze have been found along with the finds deeper in the Dig Site, which leads us to believe that the forces occupying the city before the Saradominists used bows, as well as other methods of war.")
        }

        objectOperate("Study", "vm_digsite_finds_coin_saranthium") {
            displayCase("vm_timeline_barbarian_village_model", 45, 25522, "A coin in very good condition with Saradominist markings. It bears the word 'Saranthium', which we have found to be the name of the city being excavated east of Varrock. The numbers on the coin would indicate that it is from the year 3804, presumably from the 3rd Age as the Godwars were coming to an end.")
        }

        interfaceClosed("vm_timeline") {
            clearAnim()
        }
    }

    private fun Player.displayCase(component: String, display: Int, model: Int, text: String) {
        if (!open("vm_timeline")) {
            return
        }
        val comp = "vm_timeline_terracotta_statue_model"
        val componentId = InterfaceDefinitions.getComponent("vm_timeline", comp)?.id ?: return
        anim("vm_display_case_ponder")
        sendScript("museum_rotate_display", 0, 5, 0, InterfaceDefinition.pack(534, componentId))
        interfaces.sendModel("vm_timeline", comp, model)
        interfaces.sendText("vm_timeline", "display_num", display.toString())
        interfaces.sendText("vm_timeline", "vm_timeline_text", text)
    }

    private fun Player.studyDisplayCase() {
        if (!open("vm_timeline")) {
            return
        }
        anim("vm_display_case_ponder")
        val stolen = get("golem_retrieved_statuette", false)
        sendScript("museum_rotate_display", 0, 5, 0, InterfaceDefinition.pack(534, 50))
        interfaces.sendModel("vm_timeline", "vm_timeline_terracotta_statue_model", if (stolen) 25568 else 25576)
        interfaces.sendText("vm_timeline", "display_num", "30")
        interfaces.sendText(
            "vm_timeline",
            "vm_timeline_text",
            "3rd Age - yr 3000-4000<br><br>This " +
                "statuette was found in an underground temple in the ruined city of Uzer, which was destroyed late " +
                "in the 3rd Age, suddenly, due to causes unknown. It probably represents one of the clay golems " +
                "that the craftsmen of the city built as warriors and servants. The statuette was originally part " +
                "of a mechanism whose purpose is unknown." +
                (if (stolen) "<br><br>Recently this display was stolen and its whereabouts are unknown." else ""),
        )
    }
}
