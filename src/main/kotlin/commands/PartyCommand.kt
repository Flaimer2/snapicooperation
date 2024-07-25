package ru.snapix.snapicooperation.commands

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.bukkit.utils.sendMessage
import ru.snapix.library.libs.commands.BaseCommand
import ru.snapix.library.libs.commands.annotation.CatchUnknown
import ru.snapix.library.libs.commands.annotation.CommandAlias
import ru.snapix.library.libs.commands.annotation.Default
import ru.snapix.library.libs.commands.annotation.Subcommand
import ru.snapix.library.utils.message
import ru.snapix.library.utils.stripColor
import ru.snapix.library.utils.translateAlternateColorCodes
import ru.snapix.snapicooperation.PanelStorage
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.settings.Settings

@CommandAlias("%party_command_main")
class PartyCommand : BaseCommand() {
    private val config get() = Settings.message.party().commands()

    @Default
    fun default(player: Player) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer]
        if (party == null) {
            PanelStorage.nullPartyMenu(player)
            return
        }
        PanelStorage.defaultPartyMenu(player)
    }


    @CatchUnknown
    @Subcommand("%party_command_help")
    fun help(sender: CommandSender) {
        sender.sendMessage(config.help().map { translateAlternateColorCodes(it) }.toTypedArray())
    }

    @Subcommand("%party_command_disband")
    fun disband(player: Player) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer]
        if (party == null) {
            player.message(config.disband().notInParty())
            return
        }
        if (!party.isLeader(networkPlayer)) {
            player.message(config.disband().onlyLeave(), "party_leader" to party.leader.getName())
            return
        }
        party.remove()
    }

    @Subcommand("%party_command_invite")
    fun invite(player: Player, args: Array<String>) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer] ?: Party.create(player)
        if (!party.isLeader(networkPlayer)) {
            player.message(config.invite().notLeader(), "party_leader" to party.leader.getName())
            return
        }

        if (args.isEmpty()) {
            player.message(config.invite().use())
            return
        }
        val invited = args[0]

        if (invited.equals(player.name, ignoreCase = true)) {
            player.message(config.invite().errorYourself(), "party_leader" to party.leader.getName())
            return
        }

        val ntInvited = SnapiLibrary.getPlayer(invited)
        if (!ntInvited.hasPlayedBefore()) {
            player.message(config.invite().notFound(), "name" to ntInvited.getName(), "party_leader" to party.leader.getName())
            return
        }

        if (!ntInvited.isOnline()) {
            player.message(config.invite().offline(), "name" to ntInvited.getName(), "party_leader" to party.leader.getName())
            return
        }

        if (party.inParty(ntInvited)) {
            player.message(config.invite().alreadyInYouParty(), "name" to ntInvited.getName(), "party_leader" to party.leader.getName())
            return
        }

        val partyInvited = Party[ntInvited]

        if (partyInvited != null) {
            if (partyInvited.isLeader(ntInvited)) {
                player.message(
                    config.invite().alreadyInPartyLeader(),
                    "name" to ntInvited.getName(),
                    "party_leader" to partyInvited.leader
                )
            } else {
                player.message(
                    config.invite().alreadyInParty(),
                    "name" to ntInvited.getName(),
                    "party_leader" to partyInvited.leader
                )
            }
            return
        }

        if (party.invitations.contains(ntInvited)) {
            player.message(
                config.invite().alreadyInvite(),
                "name" to ntInvited.getName(),
                "party_leader" to party.leader
            )
            return
        }

        if (party.size >= party.maxSize) {
            player.message(
                config.invite().fullParty(),
                "name" to ntInvited.getName(),
                "party_leader" to party.leader,
                "size" to party.size,
                "max_size" to party.maxSize
            )
            return
        }

        party.createInvitation(ntInvited)
    }

    @Subcommand("%party_command_accept")
    fun accept(player: Player, args: Array<String>) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        if (Party[networkPlayer] != null) {
            player.message(config.accept().alreadyInParty(), "party_leader" to Party[networkPlayer]!!.leader.getName())
            return
        }

        if (args.isEmpty()) {
            player.message(config.accept().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.name, ignoreCase = true)) {
            player.message(config.accept().errorYourself())
            return
        }

        val ntInviter = SnapiLibrary.getPlayer(inviter)
        if (!ntInviter.hasPlayedBefore()) {
            player.message(config.accept().notFound(), "name" to ntInviter.getName())
            return
        }

        if (!ntInviter.isOnline()) {
            player.message(config.accept().offline(), "name" to ntInviter.getName())
            return
        }

        val party = Party[ntInviter]
        if (party == null) {
            player.message(config.accept().notFoundParty(), "name" to ntInviter.getName())
            return
        }

        if (!party.isLeader(ntInviter)) {
            player.message(
                config.accept().notLeader(),
                "name" to ntInviter.getName(),
                "party_leader" to party.leader.getName()
            )
            return
        }

        party.accept(networkPlayer)
    }

    @Subcommand("%party_command_deny")
    fun deny(player: Player, args: Array<String>) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        if (Party[networkPlayer] != null) {
            player.message(config.deny().alreadyInParty(), "party_leader" to Party[networkPlayer]!!.leader.getName())
            return
        }

        if (args.isEmpty()) {
            player.message(config.deny().use())
            return
        }
        val inviter = args[0]

        if (inviter.equals(player.name, ignoreCase = true)) {
            player.message(config.deny().errorYourself())
            return
        }

        val ntInviter = SnapiLibrary.getPlayer(inviter)
        if (!ntInviter.hasPlayedBefore()) {
            player.message(config.deny().notFound(), "name" to ntInviter.getName())
            return
        }

        if (!ntInviter.isOnline()) {
            player.message(config.deny().offline(), "name" to ntInviter.getName())
            return
        }

        val party = Party[ntInviter]
        if (party == null) {
            player.message(config.deny().notFoundParty(), "name" to ntInviter.getName())
            return
        }

        if (!party.isLeader(ntInviter)) {
            player.message(
                config.deny().notLeader(),
                "name" to ntInviter.getName(),
                "party_leader" to party.leader.getName()
            )
            return
        }

        party.decline(networkPlayer)
    }

    @Subcommand("%party_command_remove")
    fun remove(player: Player, args: Array<String>) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer]
        if (party == null) {
            player.message(config.remove().notParty())
            return
        }

        if (!party.isLeader(networkPlayer)) {
            player.message(config.remove().notLeader(), "party_leader" to party.leader.getName())
            return
        }

        if (args.isEmpty()) {
            player.message(config.remove().use(), "party_leader" to party.leader.getName())
            return
        }
        val removed = args[0]

        if (removed.equals(player.name, ignoreCase = true)) {
            player.message(config.remove().errorYourself(), "party_leader" to party.leader.getName())
            return
        }

        val ntRemoved = SnapiLibrary.getPlayer(player.name)
        if (!ntRemoved.hasPlayedBefore()) {
            player.message(config.remove().notFound(), "name" to removed, "party_leader" to party.leader.getName())
            return
        }

        if (!ntRemoved.isOnline()) {
            player.message(
                config.remove().offline(),
                "name" to ntRemoved.getName(),
                "party_leader" to party.leader.getName()
            )
            return
        }

        if (party.inParty(ntRemoved)) {
            player.message(
                config.remove().notInParty(),
                "name" to ntRemoved.getName(),
                "party_leader" to party.leader.getName()
            )
            return
        }

        party.removePlayer(ntRemoved)
    }

    @Subcommand("%party_command_chat")
    fun chat(player: Player, args: Array<String>) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer]

        if (party == null) {
            player.message(config.chat().notParty())
            return
        }

        if (args.isEmpty()) {
            player.message(config.chat().use())
            return
        }

        val message = stripColor(args.joinToString(" "))

        var format = Settings.config.chatFormat()

        format = PlaceholderAPI.setPlaceholders(player, format)
        format = format.replace("%message%", message)

        val receivers = listOf(party.leader, *party.players.toTypedArray())
        receivers.sendMessage(format)
    }

    @Subcommand("%party_command_leave")
    fun leave(player: Player) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer]
        if (party == null) {
            player.message(config.leave().notParty())
            return
        }

        if (party.isLeader(networkPlayer)) {
            player.message(config.leave().leader())
            return
        }

        party.leave(networkPlayer)
    }

    @Subcommand("%party_command_lead")
    fun lead(player: Player, args: Array<String>) {
        val networkPlayer = SnapiLibrary.getPlayer(player.name)
        val party = Party[networkPlayer]
        if (party == null) {
            player.message(config.lead().notParty())
            return
        }

        if (!party.isLeader(networkPlayer)) {
            player.message(config.lead().notLeader(), "party_leader" to party.leader.getName())
            return
        }

        if (args.isEmpty()) {
            player.message(config.lead().use(), "party_leader" to party.leader.getName())
            return
        }
        val leader = args[0]

        if (leader.equals(player.name, ignoreCase = true)) {
            player.message(config.lead().errorYourself(), "party_leader" to party.leader.getName())
            return
        }

        val ntLeader = SnapiLibrary.getPlayer(leader)
        if (!ntLeader.hasPlayedBefore()) {
            player.message(config.lead().notFound(), "name" to leader, "party_leader" to party.leader.getName())
            return
        }

        if (!ntLeader.isOnline()) {
            player.message(
                config.lead().offline(),
                "name" to ntLeader.getName(),
                "party_leader" to party.leader.getName()
            )
            return
        }

        if (party.inParty(ntLeader)) {
            player.message(
                config.lead().notInParty(),
                "name" to ntLeader.getName(),
                "party_leader" to party.leader.getName()
            )
            return
        }

        party.changeLeader(ntLeader)
    }
}