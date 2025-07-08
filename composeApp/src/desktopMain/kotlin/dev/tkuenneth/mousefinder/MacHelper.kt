package dev.tkuenneth.mousefinder

import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

object MacHelp {
    private val TAG: String = MacHelp::class.java.getName()
    private val LOGGER: Logger = Logger.getLogger(TAG)

    fun activateApp(name: String) {
        val script = "tell application \"$name\"\nactivate\nend tell"
        run(script)
    }

//    val frontmostApp: String
//        get() {
//            val script =
//                "tell application \"System Events\"\nitem 1 of (get name of processes whose frontmost is true)\nend tell"
//            return run(script)
//        }

    private fun run(script: String): String {
        val sbIS = StringBuilder()
        val sbES = StringBuilder()
        val pb = ProcessBuilder("/usr/bin/osascript", "-e", script)
        val result = start(pb, sbIS, sbES)
        if (result == 0) {
            return sbIS.toString().trim { it <= ' ' }
        } else {
            LOGGER.log(Level.SEVERE, sbES.toString())
            return ""
        }
    }

    private fun start(pb: ProcessBuilder, sbIS: StringBuilder, sbES: StringBuilder): Int {
        var exit = 1
        try {
            val p = pb.start()
            val `is` = p.inputStream
            var isData: Int
            val es = p.errorStream
            var esData: Int
            while (true) {
                isData = `is`.read()
                esData = es.read()
                if (isData != -1) {
                    sbIS.append(isData.toChar())
                }
                if (esData != -1) {
                    sbES.append(esData.toChar())
                }
                if ((isData == -1) && (esData == -1)) {
                    try {
                        exit = p.exitValue()
                        break
                    } catch (_: IllegalThreadStateException) {
                        // no logging needed... just waiting
                    }
                }
            }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "exception while reading", e)
        }
        return exit
    }
}
