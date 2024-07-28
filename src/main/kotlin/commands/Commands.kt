package ru.snapix.snapicooperation.commands

import ru.snapix.library.SnapiLibrary
import ru.snapix.library.bukkit.BukkitCommands
import ru.snapix.library.bukkit.utils.addReplacements
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.snapicooperation.api.CooperationApi
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.caches.Friends
import ru.snapix.snapicooperation.caches.Parties
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings

object Commands : BukkitCommands(plugin, PartyCommand(), FriendCommand()) {
    override fun registerCommandCompletions() {
        val commandCompletions = manager.commandCompletions
        commandCompletions.registerAsyncCompletion("player_without_party") { context ->
            CooperationApi.playerWithoutParty().map { it.getName() }
        }
        commandCompletions.registerAsyncCompletion("player_invite") { context ->
            val player = context.player
            val networkPlayer = OnlineNetworkPlayer(player.name)
            Parties.values().filter { it.invitations.contains(networkPlayer) }.map { it.leader.getName() }
        }
        commandCompletions.registerAsyncCompletion("player_in_you_party") { context ->
            val player = context.player
            val party = Party[player.name] ?: return@registerAsyncCompletion emptyList<String>()
            party.players.map { it.getName() }
        }
        commandCompletions.registerAsyncCompletion("player_in_friend") { context ->
            CooperationApi.playerInFriend(context.player).map { it.getName() }
        }
        commandCompletions.registerAsyncCompletion("player_without_friend") { context ->
            CooperationApi.playerWithoutFriend(context.player).map { it.getName() }
        }
        commandCompletions.registerAsyncCompletion("friend_invite") { context ->
            val player = context.player
            Friends.values().filter { it.invitations.contains(player.name) }.map { it.name }
        }
        commandCompletions.registerAsyncCompletion("nothing") { _ ->
            emptyList<String>()
        }
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