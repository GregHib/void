package org.redrune.util

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
class OutLogger(out: OutputStream) : PrintStream(out) {

    override fun print(message: Boolean) {
        prettyLog(elementConversion(getProperElement(Thread.currentThread().stackTrace)), "" + message)
    }

    override fun print(message: Int) {
        prettyLog(elementConversion(getProperElement(Thread.currentThread().stackTrace)), "" + message)
    }

    override fun print(message: Long) {
        prettyLog(elementConversion(getProperElement(Thread.currentThread().stackTrace)), "" + message)
    }

    override fun print(message: Double) {
        prettyLog(elementConversion(getProperElement(Thread.currentThread().stackTrace)), "" + message)
    }

    override fun print(message: String) {
        prettyLog(elementConversion(getProperElement(Thread.currentThread().stackTrace)), "" + message)
    }

    override fun print(message: Any) {
        prettyLog(elementConversion(getProperElement(Thread.currentThread().stackTrace)), "" + message)
    }

    /**
     * Converts the [StackTraceElement] instance to a detailed description of the source
     */
    private fun elementConversion(element: StackTraceElement): String {
        val fileName = element.fileName ?: ""
        val endIndex = fileName.indexOf(".")
        return "${fileName.substring(
            0,
            (if (endIndex == -1) fileName.length else endIndex)
        )}:${element.lineNumber}#${element.methodName}"
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
        super.print("[$description][$formattedDate]  $text")
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