package content.skill.magic.book.lunar

import content.entity.gfx.areaGfx
import content.skill.farming.patchName
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp

class FertileSoil : Script {

    init {
        onObjectOperate("lunar_spellbook:fertile_soil", "*") { (target) ->
            if (!target.id.startsWith("farming_")) {
                message("Um... I don't want to fertilise that!")
                return@onObjectOperate
            }
            val id = target.def(this).stringId
            if (id.endsWith("_fullygrown")) {
                message("Composting it isn't going to make it get any bigger.")
                return@onObjectOperate
            }
            if (containsVarbit("patch_super_compost", target.id)) {
                message("This ${target.patchName()} has already been treated with supercompost.")
                return@onObjectOperate
            }
            if (containsVarbit("patch_compost", target.id)) {
                message("This ${target.patchName()} has already been treated with compost.")
                return@onObjectOperate
            }
            if (!removeSpellItems("fertile_soil")) {
                return@onObjectOperate
            }
            start("movement_delay", 3)
            anim("lunar_cast_charge")
            areaGfx("fertile_soil", target.tile)
            areaSound("lunar_fertilize", target.tile, radius = 10)
            delay(3)
            addVarbit("patch_super_compost", target.id)
            message("You treat the ${target.patchName()} with supercompost.")
            exp(Skill.Magic, Tables.int("spells.fertile_soil.xp") / 10.0)
        }
    }
}
