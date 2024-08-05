package top.tbpdt.vanilla.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
/**
 * @author Takeoff0518
 */
object BlacklistConfig : AutoSavePluginConfig("BlacklistConfig") {
    @ValueDescription("用户黑名单")
    val userBlacklist: MutableSet<Long> by value()

    @ValueDescription("群黑名单")
    val groupBlacklist: MutableSet<Long> by value()
}