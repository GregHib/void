package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.entity.character.player.Player

internal fun Player.agilityStage(stage: Int) {
    if (stage == this.agilityStage + 1) {
        this.agilityStage = stage
    }
}

internal fun Player.agilityCourse(name: String) {
    this["agility_course"] = name
}

internal var Player.agilityStage: Int
    get() = this["${this["agility_course", "unknown"]}_course_stage", 0]
    set(value) {
        this["${this["agility_course", "unknown"]}_course_stage"] = value
    }