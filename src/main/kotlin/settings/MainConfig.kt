package ru.snapix.snapicooperation.settings

import ru.snapix.library.libs.dazzleconf.annote.ConfDefault.*
import ru.snapix.library.libs.dazzleconf.annote.ConfKey
import ru.snapix.library.libs.dazzleconf.annote.SubSection

interface MainConfig {
    @SubSection
    fun alias(): Alias
    interface Alias {
        @ConfKey("main-party-command")
        @DefaultString("party")
        fun mainPartyCommand(): String

        @ConfKey("main-friend-command")
        @DefaultString("friend|friends")
        fun mainFriendCommand(): String

        @ConfKey("help-party-command")
        @DefaultString("help")
        fun helpPartyCommand(): String

        @ConfKey("disband-party-command")
        @DefaultString("disband")
        fun disbandPartyCommand(): String

        @ConfKey("invite-party-command")
        @DefaultString("invite")
        fun invitePartyCommand(): String

        @ConfKey("accept-party-command")
        @DefaultString("accept")
        fun acceptPartyCommand(): String

        @ConfKey("deny-party-command")
        @DefaultString("deny")
        fun denyPartyCommand(): String

        @ConfKey("remove-party-command")
        @DefaultString("remove")
        fun removePartyCommand(): String

        @ConfKey("chat-party-command")
        @DefaultString("chat")
        fun chatCommand(): String

        @ConfKey("leave-party-command")
        @DefaultString("leave")
        fun leaveCommand(): String

        @ConfKey("lead-party-command")
        @DefaultString("lead")
        fun leadCommand(): String

        @ConfKey("help-friend-command")
        @DefaultString("help")
        fun helpFriendCommand(): String

        @ConfKey("add-friend-command")
        @DefaultString("add")
        fun addFriendCommand(): String

        @ConfKey("accept-friend-command")
        @DefaultString("accept")
        fun acceptFriendCommand(): String

        @ConfKey("decline-friend-command")
        @DefaultString("decline")
        fun declineFriendCommand(): String

        @ConfKey("remove-friend-command")
        @DefaultString("remove")
        fun removeFriendCommand(): String
    }

    @ConfKey("chat-prefix")
    @SubSection
    fun chatPrefix(): ChatPrefix
    interface ChatPrefix {
        @DefaultBoolean(true)
        fun enable(): Boolean

        @DefaultStrings("@", "$")
        fun prefix(): List<String>
    }

    @DefaultStrings("Auth")
    @ConfKey("disable-servers")
    fun disableServers(): List<String>

    @DefaultInteger(10)
    @ConfKey("default-max-size")
    fun defaultMaxSize(): Int

    @DefaultInteger(60)
    @ConfKey("invitation-delay")
    fun invitationDelay(): Int

    @ConfKey("chat-format")
    @DefaultString("%vault_prefix%%player_name%%vault_suffix% > %message%")
    fun chatFormat(): String
}