package world.gregs.voidps.engine.entity.character.player.skill

@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class SkillId(
    val skill: Skill,
    val id: String = ""
)