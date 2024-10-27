package top.tbpdt.vanilla.utils

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object StatusRecorder: AutoSavePluginData("StatusRecorder") {

    val receiveCount: MutableMap<String, Int> by value(mutableMapOf())

    val sendCount: MutableMap<String, Int> by value(mutableMapOf())
}
