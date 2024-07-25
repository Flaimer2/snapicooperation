package ru.snapix.snapicooperation.commands

import ru.snapix.library.bukkit.BukkitCommands
import ru.snapix.library.bukkit.utils.addReplacements
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings

object Commands : BukkitCommands(plugin, PartyCommand(), FriendCommand()) {
    override fun registerCommandCompletions() {

    }

    override fun registerCommandReplacements() {
        val config = Settings.config.alias()

        commandReplacements.addReplacements(
            "party_command_",
            "main" to config.mainPartyCommand(),
            "help" to config.helpPartyCommand(),
            "disband" to config.disbandPartyCommand(),
            "invite" to config.invitePartyCommand(),
            "accept" to config.acceptPartyCommand(),
            "deny" to config.denyPartyCommand(),
            "remove" to config.removePartyCommand(),
            "chat" to config.chatCommand(),
            "leave" to config.leaveCommand(),
            "lead" to config.leadCommand()
        )
        commandReplacements.addReplacements(
            "friend_command_",
            "main" to config.mainFriendCommand(),
            "help" to config.helpFriendCommand(),
            "add" to config.addFriendCommand(),
            "accept" to config.acceptFriendCommand(),
            "decline" to config.declineFriendCommand(),
            "remove" to config.removeFriendCommand()
        )
    }
}