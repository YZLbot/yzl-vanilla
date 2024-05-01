package top.tbpdt.vanilla.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object CaveConfig : AutoSavePluginConfig("CaveConfig") {
    @ValueDescription("cave_id 黑名单")
    val caveBlackList: MutableSet<Int> by value()
}