package ru.snapix.snapicooperation.commands

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import ru.snapix.library.NetworkPlayer
import ru.snapix.library.callEvent
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.CatchUnknown
import ru.snapix.library.libs.commands.annotation.CommandAlias
import ru.snapix.library.libs.commands.annotation.Default
import ru.snapix.library.libs.commands.annotation.Subcommand
import ru.snapix.library.message
import ru.snapix.library.toPlayer
import ru.snapix.library.utils.stripColor
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.api.events.party.DisbandReason
import ru.snapix.snapicooperation.api.events.party.PartyDisbandEvent
import ru.snapix.snapicooperation.chatMessage
import ru.snapix.snapicooperation.PanelStorage
import ru.snapix.snapicooperation.placeholders
import ru.snapix.snapicooperation.plugin
import ru.snapix.snapicooperation.settings.Settings
import ru.snapix.snapicooperation.toPlayer

@CommandAlias("%party_command_main")
class PartyCommand : BaseCommand() {
    private val config get() = Settings.message.party().commands()

    @Default
    fun default(player: Player) {
        val party = Party[player]
        if (party == null) {
            PanelStorage.nullPartyMenu(player)
            return
        }
        PanelStorage.defaultPartyMenu(player)
    }

    @CatchUnknown
    @Subcommand("%party_command_help")
    fun help(sender: CommandSource) {
        sender.message(config.help())
    }

    @Subcommand("%party_command_disband")
    fun disband(player: Player) {
        val party = Party[player]
        if (party == null) {
            player.message(config.disband().notInParty())
            return
        }
        if (!party.isLeader(player)) {
            player.message(config.disband().onlyLeave(), *party.placeholders())
            return
        }
        plugin.callEvent(PartyDisbandEvent(player, party, DisbandReason.USER_DISBAND))
        party.remove()
    }

    @Subcommand("%party_command_invite")
    fun invite(player: Player, args: Array<String>) {
        val party = Party[player] ?: Party.create(player)
        if (!party.isLeader(player)) {
            player.message(config.invite().notLeader())
            return
        }

        if (args.isEmpty()) {
            player.message(config.invite().use())
            return
        }
        val invited = args[0]

        if (invited.equals(player.username, ignoreCase = true)) {
            player.message(config.invite().errorYourself())
            return
        }

        val ntInvited = NetworkPlayer(invited)
        if (!ntInvited.isExist()) {
            player.message(config.invite().notFound(), "name" to invited)
            return
        }

        val playerInvited = ntInvited.toPlayer()
        if (playerInvited == null) {
            player.message(config.invite().offline(), "name" to ntInvited.name())
            return
        }

        if (party.inParty(playerInvited)) {
            player.message(config.invite().alreadyInYouParty(), "name" to ntInvited.name())
            return
        }

        val partyInvited = Party[playerInvited]

        if (partyInvited != null) {
            if (partyInvited.isLeader(playerInvited)) {
                player.message(
                    config.invite().alreadyInPartyLeader(),
                    "name" to ntInvited.name(),
                    *partyInvited.placeholders()
                )
            } else {
                player.message(
                    config.invite().alreadyInParty(),
                    "name" to ntInvited.name(),
                    *partyInvited.placeholders()
                )
            }
            return
        }

        if (party.invitations.contains(ntInvited.name())) {
            player.message(config.invite().alreadyInvite(), "name" to ntInvited.name(), *party.placeholders())
            return
        }

        if (party.size >= party.maxSize) {
            player.message(config.invite().fullParty(), "name" to ntInvited.name(), *party.placeholders())
            return
        }

        party.createInvitation(playerInvited)
    }

    @Subcommand("%party_command_accept")
    fun accept(player: Player, args: Array<String>) {
        if (Party[player] != null) {
            player.message(config.accept().alreadyInParty(), *Party[player].placeholders())
            return
        }

        if (args.isEmpty()) {
            player.message(config.accept().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.username, ignoreCase = true)) {
            player.message(config.accept().errorYourself())
            return
        }

        val ntInviter = NetworkPlayer(inviter)
        if (!ntInviter.isExist()) {
            player.message(config.accept().notFound(), "name" to inviter)
            return
        }

        val playerInviter = ntInviter.toPlayer()
        if (playerInviter == null) {
            player.message(config.accept().offline(), "name" to ntInviter.name())
            return
        }

        val party = Party[playerInviter]
        if (party == null) {
            player.message(config.accept().notFoundParty(), "name" to ntInviter.name())
            return
        }

        if (!party.isLeader(playerInviter)) {
            player.message(config.accept().notLeader(), "name" to ntInviter.name(), *party.placeholders())
            return
        }

        party.accept(player)
    }

    @Subcommand("%party_command_deny")
    fun deny(player: Player, args: Array<String>) {
        if (Party[player] != null) {
            player.message(config.deny().alreadyInParty(), *Party[player].placeholders())
            return
        }

        if (args.isEmpty()) {
            player.message(config.deny().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.username, ignoreCase = true)) {
            player.message(config.deny().errorYourself())
            return
        }

        val ntInviter = NetworkPlayer(inviter)
        if (!ntInviter.isExist()) {
            player.message(config.deny().notFound(), "name" to inviter)
            return
        }

        val playerInviter = ntInviter.toPlayer()
        if (playerInviter == null) {
            player.message(config.deny().offline(), "name" to ntInviter.name())
            return
        }

        val party = Party[playerInviter]
        if (party == null) {
            player.message(config.deny().notFoundParty(), "name" to ntInviter.name())
            return
        }

        if (!party.isLeader(playerInviter)) {
            player.message(config.deny().notLeader(), "name" to ntInviter.name(), *party.placeholders())
            return
        }

        party.decline(player)
    }

    @Subcommand("%party_command_remove")
    fun remove(player: Player, args: Array<String>) {
        val party = Party[player]
        if (party == null) {
            player.message(config.remove().notParty())
            return
        }

        if (!party.isLeader(player)) {
            player.message(config.remove().notLeader(), *party.placeholders())
            return
        }

        if (args.isEmpty()) {
            player.message(config.remove().use(), *party.placeholders())
            return
        }
        val removed = args[0]

        if (removed.equals(player.username, ignoreCase = true)) {
            player.message(config.remove().errorYourself(), *party.placeholders())
            return
        }

        val ntRemoved = NetworkPlayer(removed)
        if (!ntRemoved.isExist()) {
            player.message(config.remove().notFound(), "name" to removed, *party.placeholders())
            return
        }

        val playerRemoved = ntRemoved.toPlayer()
        if (playerRemoved == null) {
            player.message(config.remove().offline(), "name" to ntRemoved.name(), *party.placeholders())
            return
        }

        if (party.inParty(player)) {
            player.message(config.remove().notInParty(), "name" to ntRemoved.name(), *party.placeholders())
            return
        }

        party.removePlayer(playerRemoved)
    }

    @Subcommand("%party_command_chat")
    fun chat(player: Player, args: Array<String>) {
        val party = Party[player]
        if (party == null) {
            player.message(config.chat().notParty())
            return
        }

        if (args.isEmpty()) {
            player.message(config.chat().use())
            return
        }

        val ntPlayer = NetworkPlayer(player.username)

        val prefix = ntPlayer.prefix()
        val suffix = ntPlayer.suffix()
        val message = stripColor(args.joinToString(" "))

        var format = Settings.config.chatFormat()

        format = format.replace("%player%", player.username)
        format = format.replace("%prefix%", prefix ?: "")
        format = format.replace("%suffix%", if (suffix == null) "" else " $suffix")
        format = format.replace("%message%", message)

        party.leader.toPlayer().chatMessage(player, format)
        party.players.mapNotNull { it.toPlayer() }.forEach { it.chatMessage(player, format) }
    }

    @Subcommand("%party_command_leave")
    fun leave(player: Player) {
        val party = Party[player]
        if (party == null) {
            player.message(config.leave().notParty())
            return
        }

        if (party.isLeader(player)) {
            player.message(config.leave().leader(), *party.placeholders())
            return
        }

        party.removePlayer(player)
    }

    @Subcommand("%party_command_lead")
    fun lead(player: Player, args: Array<String>) {
        val party = Party[player]
        if (party == null) {
            player.message(config.lead().notParty())
            return
        }

        if (!party.isLeader(player)) {
            player.message(config.lead().notLeader(), *party.placeholders())
            return
        }

        if (args.isEmpty()) {
            player.message(config.lead().use(), *party.placeholders())
            return
        }
        val leader = args[0]

        if (leader.equals(player.username, ignoreCase = true)) {
            player.message(config.lead().errorYourself(), *party.placeholders())
            return
        }

        val ntLeader = NetworkPlayer(leader)
        if (!ntLeader.isExist()) {
            player.message(config.lead().notFound(), "name" to leader, *party.placeholders())
            return
        }

        val playerLeader = ntLeader.toPlayer()
        if (playerLeader == null) {
            player.message(config.lead().offline(), "name" to ntLeader.name(), *party.placeholders())
            return
        }

        if (party.inParty(player)) {
            player.message(config.lead().notInParty(), "name" to ntLeader.name(), *party.placeholders())
            return
        }

        party.changeLeader(playerLeader)
    }
}