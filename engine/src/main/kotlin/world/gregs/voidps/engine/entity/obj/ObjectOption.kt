package world.gregs.voidps.engine.entity.obj

import com.fasterxml.jackson.annotation.JsonTypeInfo
import world.gregs.voidps.engine.event.Event

@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
data class ObjectOption(val obj: GameObject, val option: String?, val partial: Boolean) : Event