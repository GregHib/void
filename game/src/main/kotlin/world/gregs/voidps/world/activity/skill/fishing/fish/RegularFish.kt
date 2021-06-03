package world.gregs.voidps.world.activity.skill.fishing.fish

import world.gregs.voidps.utility.func.toTitleCase
import world.gregs.voidps.utility.func.toUnderscoreCase

enum class RegularFish(
    override val level: Int,
    override val xp: Double,
    override val chance: IntRange,
) : Catch {
    Shrimp(1, 10.0, 48..256),
    Crayfish(1, 10.0, 1..1),
    Sardine(5, 20.0, 32..192),
    Karambwanji(5, 5.0, 100..250),
    Herring(10, 30.0, 24..128),
    Anchovies(15, 40.0, 24..128),
    Mackerel(16, 20.0, 5..65),
    Trout(20, 50.0, 32..192),
    Cod(23, 45.0, 4..55),
    Pike(25, 60.0, 16..96),
    SlimyEel(28, 65.0, 10..80),
    CaveEel(38, 80.0, 10..80),
    Salmon(30, 70.0, 16..96),
    SwampWeed(33, 1.0, 10..10),
    FrogSpawn(33, 75.0, 16..96),
    Tuna(35, 80.0, 8..64),
    RainbowFish(38, 80.0, 8..64),
    Lobster(40, 90.0, 6..95),
    Bass(46, 100.0, 3..40),
    BigBass(46, 100.0, 2..3),
    Swordfish(50, 100.0, 4..48),
    LavaEel(53, 60.0, 16..96),
    Monkfish(62, 120.0, 48..90),
    Karambwan(65, 50.0, 5..160),
    Shark(76, 110.0, 3..40),
    SeaTurtle(79, 38.0, 3..30),
    MantaRay(81, 46.0, 2..30),
    Cavefish(85, 300.0, 2..24),
    RockCrab(90, 380.0, 1..16);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}