package world.gregs.voidps.engine.event

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec

data class UseData(
    val builder: FunSpec.Builder,
    val method: ClassName,
    val ids: MutableSet<String> = mutableSetOf(),
    var on: List<String> = emptyList(),
    var option: String = "*",
    var component: String = "*",
    var approach: Boolean = false,
    var arrive: Boolean = true,
)