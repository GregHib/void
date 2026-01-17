package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class CurePlant(val definitions: SpellDefinitions) : Script {

    init {
        onObjectOperate("lunar_spellbook:cure_plant", "*") { (target) ->
            if (!target.id.startsWith("farming_")) {
                return@onObjectOperate
            }
            val value = get(target.id, "weeds_life3")
            if (!value.contains("_diseased")) {
                if (value.contains("_dead")) {
                    message("It says 'Cure', not 'Resurrect'. Although death may arise from disease, it is not in itself a disease and hence cannot be cured by this spell. So there.")
                } else if (value.startsWith("weeds_")) {
                    message("The weeds are healthy enough already.")
                } else {
                    message("It's growing just fine.")
                }
                return@onObjectOperate
            }
            if (!removeSpellItems("cure_plant")) {
                return@onObjectOperate
            }
            val definition = definitions.get("cure_plant")
            start("movement_delay", 3)
            anim("lunar_cure_plant")
            gfx("cure_me")
            areaSound("lunar_fertilize", target.tile, radius = 10)
            delay(3)
            set(target.id, value.replace("_diseased", ""))
            experience.add(Skill.Magic, definition.experience)
        }
    }
}
