package top.tbpdt.vanilla.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object AutoPicsConfig : AutoSavePluginConfig("AutoPicsConfig") {
    // k = regex, v = path
    @ValueDescription("文件夹绝对路径: 命中正则表达式")
    val regexMap: Map<String, String> by value(mapOf("^37fb4os8dfh4bf7$" to "D:\\examples"))
}