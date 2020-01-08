package org.redrune.utility.functions

import java.io.OutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class modifies previous [System.out] logging and prints them with information. We need to know which class
 * printed data and at what time at all times.
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 9, 2015
 */
class OutLogger(out: OutputStream?) : PrintStream(out) {
    override fun print(message: Boolean) {
        val element =
            getProperElement(Thread.currentThread().stackTrace)
        prettyLog("""${element.fileName}:${element.lineNumber}#${element.methodName}""", "" + message)
    }

    override fun print(message: Int) {
        val element =
            getProperElement(Thread.currentThread().stackTrace)
        prettyLog("""${element.fileName}:${element.lineNumber}#${element.methodName}""", "" + message)
    }

    override fun print(message: Long) {
        val element =
            getProperElement(Thread.currentThread().stackTrace)
        prettyLog("""${element.fileName}:${element.lineNumber}#${element.methodName}""", "" + message)
    }

    override fun print(message: Double) {
        val element =
            getProperElement(Thread.currentThread().stackTrace)
        prettyLog("""${element.fileName}:${element.lineNumber}#${element.methodName}""", "" + message)
    }

    override fun print(message: String) {
        val element =
            getProperElement(Thread.currentThread().stackTrace)
        prettyLog("""${element.fileName}:${element.lineNumber}#${element.methodName}""", "" + message)
    }

    override fun print(message: Any) {
        val element =
            getProperElement(Thread.currentThread().stackTrace)
        prettyLog("""${element.fileName}:${element.lineNumber}#${element.methodName}""", "" + message)
    }

    /**
     * Outputs a pretty log
     *
     * @param description
     * The description of where its coming from
     * @param text
     * The text that is being outputted
     */
    private fun prettyLog(description: String, text: String) {
        val pretext = "[$description][$formattedDate]  $text"
        super.print(pretext)
    }

    /**
     * Gets the date in a formatted string.
     *
     * @return The date
     */
    private val formattedDate: String
        get() = DATE_FORMAT.format(Date())

    companion object {
        /**
         * The format of a date
         */
        private val DATE_FORMAT = SimpleDateFormat("MM.dd.yyyy hh:mm:ss.SSS")

        /**
         * Gets the proper stacktrace element
         *
         * @param elements
         * The elements
         */
        fun getProperElement(elements: Array<StackTraceElement>): StackTraceElement {
            for (i in elements.indices) {
                val element = elements[i]
                if (element.toString().contains("java.io.PrintStream")) {
                    val newIndex = i + 1
                    if (newIndex >= elements.size) {
                        continue
                    }
                    return elements[newIndex]
                }
            }
            return elements[elements.size - 2]
        }
    }
}