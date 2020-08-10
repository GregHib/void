package rs.dusk.engine.entity.character.contain.detail

import rs.dusk.engine.entity.EntityDetail
import rs.dusk.engine.entity.character.contain.StackMode

data class ContainerDetail(val id: Int, val stack: StackMode = StackMode.Normal) : EntityDetail