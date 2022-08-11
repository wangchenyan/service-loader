package me.wcy.serviceloader.compiler

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

object Log {
    private var messager: Messager? = null
    private var tag: String = "Log"

    fun setLogger(messager: Messager) {
        this.messager = messager
    }

    fun setTag(tag: String) {
        this.tag = tag
    }

    fun i(msg: String) {
        messager?.printMessage(Diagnostic.Kind.NOTE, "[$tag] $msg\n")
    }

    fun w(msg: String) {
        messager?.printMessage(Diagnostic.Kind.WARNING, "[$tag] $msg\n")
    }

    fun e(msg: String) {
        messager?.printMessage(Diagnostic.Kind.ERROR, "[$tag] $msg\n")
    }

    fun exception(msg: String) {
        e(msg)
        throw IllegalStateException("[$tag] $msg")
    }

    fun e(msg: String, tr: Throwable) {
        messager?.printMessage(
            Diagnostic.Kind.ERROR,
            "[$tag] $msg\n${getStackTraceString(tr)}\n"
        )
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
