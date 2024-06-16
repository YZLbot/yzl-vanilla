package top.tbpdt.vanilla.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object CensorConfig : AutoSavePluginConfig("CensorConfig") {
    @ValueDescription(
        """
        启用敏感词检测的群聊
        若权限不够则不起作用
        """
    )
    val enableCensor: MutableSet<Long> by value(mutableSetOf(12345L))

    @ValueDescription("命中敏感词惩罚时长")
    val muteTime: MutableMap<Long, Int> by value(mutableMapOf(12345L to 0))
}
