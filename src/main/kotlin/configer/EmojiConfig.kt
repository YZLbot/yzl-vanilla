package top.tbpdt.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object EmojiConfig : AutoSavePluginConfig("EmojiConfig") {

    @ValueDescription(
        """
        启用表情嗅探
        """
    )
    val enable: Boolean by value(true)
}