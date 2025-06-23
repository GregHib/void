package world.gregs.voidps.engine.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants.*
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

class LogLevelColourConverter : ForegroundCompositeConverterBase<ILoggingEvent>() {
    override fun getForegroundColorCode(event: ILoggingEvent) = when (event.level.toInt()) {
        Level.ERROR_INT -> RED_FG
        Level.WARN_INT -> YELLOW_FG
        Level.INFO_INT -> BLUE_FG
        Level.DEBUG_INT -> CYAN_FG
        Level.TRACE_INT -> MAGENTA_FG
        else -> DEFAULT_FG
    }
}
