package top.tbpdt.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object GlobalConfig : AutoSavePluginConfig("Config") {

    @ValueDescription(
        """
        命令前缀
        """
    )
    val commandPrefix: Char by value('.')

    @ValueDescription(
        """
        管理员QQ号
        """
    )
    val admin: Set<Long> by value(
        setOf(
            1234L,
        )
    )
}