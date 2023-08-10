package me.wcy.serviceloader.compiler

import com.google.devtools.ksp.processing.KSPLogger
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

object Log {
    private var logger: KSPLogger? = null

    fun setLogger(logger: KSPLogger) {
        this.logger = logger
    }

    fun i(tag: String, msg: String) {
        logger?.info("[$tag] $msg")
    }

    fun w(tag: String, msg: String) {
        logger?.warn("[$tag] $msg")
    }

    fun e(tag: String, msg: String) {
        logger?.error("[$tag] $msg")
    }

    fun e(tag: String, msg: String, tr: Throwable) {
        logger?.error("[$tag] $msg\n${getStackTraceString(tr)}\n")
    }

    fun exception(tag: String, msg: String) {
        e(tag, msg)
        throw IllegalStateException("[$tag] $msg")
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    private fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}
