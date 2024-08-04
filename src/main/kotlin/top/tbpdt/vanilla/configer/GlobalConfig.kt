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

    @ValueDescription(
        """
        大本营群号
        """
    )
    val group: Long by value(758502512L)

    @ValueDescription(
        """
        自动同意拉拉群
        """
    )
    val autoAcceptInvitedJoinGroupRequest: Boolean by value(true)

    @ValueDescription(
        """
        自动同意加好友
        """
    )
    val autoAcceptNewFriendRequest: Boolean by value(true)

    @Deprecated("已分离为加好友 (newFriendExperienceLimi) 与拉群 (joinGroupExperienceLimit)")
    @ValueDescription(
        """
        加好友、拉群经验限制
        已弃用，分离为加好友 (newFriendExperienceLimi) 与拉群 (joinGroupExperienceLimit) 两项
        """
    )
    val experienceLimit: Int by value(100)

    @ValueDescription(
        """
        加好友经验限制
        """
    )
    val newFriendExperienceLimit: Int by value(0)

    @ValueDescription(
        """
        拉群经验限制
        """
    )
    val joinGroupExperienceLimit: Int by value(100)

    @ValueDescription(
        """
        拉群消耗经济
        """
    )
    val moneyLimit: Int by value(100)

    @ValueDescription(
        """
        拉群下限人数
        """
    )
    val groupMemberLimit: Int by value(10)
}