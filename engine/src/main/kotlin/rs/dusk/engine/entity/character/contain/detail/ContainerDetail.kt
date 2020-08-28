package rs.dusk.engine.entity.character.contain.detail

import rs.dusk.engine.entity.EntityDetail
import rs.dusk.engine.entity.character.contain.StackMode

data class ContainerDetail(
    val id: Int,
    val width: Int = 0,
    val height: Int = 0,
    val stack: StackMode = StackMode.Normal
) : EntityDetail