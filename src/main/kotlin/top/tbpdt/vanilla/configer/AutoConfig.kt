package top.tbpdt.configer

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * @author Takeoff0518
 */
object AutoConfig : AutoSavePluginConfig("AutoConfig") {
//    @ValueDescription(
//        """
//        新人入群欢迎提示语
//        为空则不欢迎
//        """
//    )
//    val newMemberJoinMessage: Set<String> by value(setOf("是新人诶……唔……可以吃嘛？", "欢迎~"))

    @ValueDescription(
        """
        缺省新人入群欢迎提示语
        为空则不欢迎
        """
    )
    val defaultNewMemberJoinMessage: Set<String> by value(setOf("是新人诶……唔……可以吃嘛？", "欢迎~"))

    @ValueDescription(
        """
        对于每个群单独的新人入群欢迎提示语
        """
    )
    val newMemberJoinMessage: Map<Long, String> by value(mapOf(12345L to "是新人诶……唔……可以吃嘛？"))

    @ValueDescription(
        """
        Bot 被戳时的回复
        可任意按照格式添加
        当戳一戳未触发反击时便随机选取列表中的消息发送
        为空时不开启
        """
    )
    val nudgedReply: Set<String> by value(
        setOf(
            "ヾ(≧へ≦)〃",
            "请不要戳我~>_<~",
            "别戳啦",
            "再戳我你就是笨批<( ￣^￣)",
            "awa~",
            "nya~",
            "坏...掉了...",
            "嗯哼~",
            "诶嘿嘿",
            "要戳坏掉了>_<",
            "不可以戳戳>_<",
            "\"(º Д º*)",
            "¿",
            "?",
            "咬你哟！嗷呜！",
            "再戳……再戳我就咬死你!",
            "φ(>ω<*)"
        )
    )

    @ValueDescription("戳一戳触发反击的概率百分比(%)")
    val counterNudge: Int by value(4)

    @ValueDescription("戳一戳触发反击的回复消息")
    val counterNudgeMessage: Set<String> by value(setOf("戳回去(￣ ‘i ￣;)"))

    @ValueDescription("戳一戳反击结束语")
    val counterNudgeCompleteMessage: Set<String> by value(setOf("哼", "切"))

    @ValueDescription("触发戳一戳超级加倍的概率 (仅触发反击时)")
    val superNudge: Int by value(1)
    val superNudgeMessage: String by value("超级加倍！")

    @ValueDescription("超级加倍戳一戳次数")
    val superNudgeTimes: Int by value(3)

    @ValueDescription(
        """
        Bot 被禁言后对禁言操作者私聊的消息
        消息会按顺序放出
    """
    )
    val botMutedMessage: List<String> by value(
        listOf(
            "就是你禁言的我吧",
            "咕姆姆，我记住你了"
        )
    )

    @ValueDescription(
        """
        Bot 被管理员解禁时的回复
        变量: %主动% (解禁操作人)
        """
    )
    val botUnmutedMessage: String by value("我自由啦！感谢%主动% 大人 🥵🥵🥵🥵🥵🥵🥵🥵")

    @ValueDescription(
        """
        群员被禁言时的回复
        变量: %主动% (解禁操作人), %被动% (被解禁的成员)
        特殊: botOperatedMuteMessage 为 Bot 主动发起禁言时的回复
    """
    )
    val memberMutedMessage: String by value("%被动% 被%主动% 禁言了，好可惜")

    @ValueDescription(
        """
        群员被解禁时的回复
        变量: %主动% (解禁操作人), %被动% (被解禁的成员)
        特殊: botOperatedUnmuteMessage 为 Bot 主动解除禁言时的回复
    """
    )
    val memberUnmuteMessage: String by value("%被动% 你自由啦！还不快感谢%主动% 大人")

    @ValueDescription(
        """
        全体解禁时的回复
        变量: %主动% (解禁操作人)
    """
    )
    val groupMuteAllRelease: String by value("嗯？好像能说话了耶")

    @ValueDescription(
        """
        有人被踢出群时的回复
        变量: %主动% (踢人操作人), %被动% (被踢出的前群员)
    """
    )
    val kickMessage: String by value("有个人被%主动% 踢了！好可怕")

    @ValueDescription(
        """
        有人主动退群时的回复
        变量: %主动% (退出的前群员)
    """
    )
    val quitMessage: String by value("%主动% 悄悄退群了...")


}
