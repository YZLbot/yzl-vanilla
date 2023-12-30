package top.tbpdt.Processor

import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * @author Takeoff0518
 */
object Handler {
    fun register() {
        onGroupTalkativeChangeEvent()
        onMemberJoinEvent()
        onMemberLeaveEvent()
        onGroupMuteAllEvent()
        onMemberMuteEvent()
        onBotMuteEvent()
        onMemberPermissionChangeEvent()
        onNudgeEvent()
        onBotJoinGroupEvent()
        onGroupMessageEvent()
        onFriendMessageEvent()
    }

    /**
     * 龙王改变
     */
    private fun onGroupTalkativeChangeEvent() {
        GlobalEventChannel.subscribeAlways<GroupTalkativeChangeEvent> {

        }
    }

    /**
     * 入群
     */
    private fun onMemberJoinEvent() {
        GlobalEventChannel.subscribeAlways<MemberJoinEvent> {

        }
    }

    /**
     * 退群
     */
    private fun onMemberLeaveEvent() {
        // 被踢退群
        GlobalEventChannel.subscribeAlways<MemberLeaveEvent.Kick> {

        }
        // 主动退群
        GlobalEventChannel.subscribeAlways<MemberLeaveEvent.Quit> {

        }

    }

    /**
     *  全员禁言
     */
    private fun onGroupMuteAllEvent() {
        GlobalEventChannel.subscribeAlways<GroupMuteAllEvent> {

        }
    }

    /**
     * 成员禁言
     */
    private fun onMemberMuteEvent() {
        // 被禁
        GlobalEventChannel.subscribeAlways<MemberMuteEvent> {

        }
        // 解禁
        GlobalEventChannel.subscribeAlways<MemberUnmuteEvent> {

        }
    }

    /**
     * 机器人禁言
     */
    private fun onBotMuteEvent() {
        // 被禁
        GlobalEventChannel.subscribeAlways<BotMuteEvent> {

        }
        // 解禁
        GlobalEventChannel.subscribeAlways<BotUnmuteEvent> {

        }
    }

    /**
     * 机器人入群
     */
    @OptIn(MiraiExperimentalApi::class)
    private fun onBotJoinGroupEvent() {
        GlobalEventChannel.subscribeAlways<BotJoinGroupEvent.Invite> {

        }
    }

    /**
     * 成员权限改变
     */
    private fun onMemberPermissionChangeEvent() {
        GlobalEventChannel.subscribeAlways<MemberPermissionChangeEvent> {

        }
    }

    /**
     * 戳一戳
     */
    private fun onNudgeEvent() {
        GlobalEventChannel.subscribeAlways<NudgeEvent> {

        }
    }

    /**
     * 群消息
     */
    private fun onGroupMessageEvent() {
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {

        }
    }

    /**
     * 好友消息
     */
    private fun onFriendMessageEvent() {
        GlobalEventChannel.subscribeAlways<FriendMessageEvent> {

        }
    }
}