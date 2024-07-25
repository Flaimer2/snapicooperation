package ru.snapix.snapicooperation

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import ru.snapix.library.SnapiLibrary
import ru.snapix.library.bukkit.panel.Item
import ru.snapix.library.bukkit.panel.dsl.Material
import ru.snapix.library.bukkit.panel.dsl.generatorPanel
import ru.snapix.library.bukkit.panel.dsl.panel
import ru.snapix.library.bukkit.panel.nextPage
import ru.snapix.library.bukkit.panel.prevPage
import ru.snapix.library.network.player.NetworkPlayer
import ru.snapix.library.network.player.OfflineNetworkPlayer
import ru.snapix.library.network.player.OnlineNetworkPlayer
import ru.snapix.library.utils.message
import ru.snapix.library.utils.toDate
import ru.snapix.snapicooperation.api.CooperationApi
import ru.snapix.snapicooperation.api.Party
import ru.snapix.snapicooperation.api.User
import kotlin.time.Duration.Companion.seconds

object PanelStorage {
    fun partyMenu(player: Player) {
        if (Party[player.name] == null) {
            nullPartyMenu(player)
        } else {
            defaultPartyMenu(player)
        }
    }

    fun nullPartyMenu(player: Player) {
        panel(player) {
            title = "Создать группу"
            layout {
                -"         "
                -"         "
                -"    G    "
                -"         "
                -"         "
                -"  O   I  "
            }
            items {
                'G' {
                    name = "&aСоздать группу"
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBiNTVmNzQ2ODFjNjgyODNhMWMxY2U1MWYxYzgzYjUyZTI5NzFjOTFlZTM0ZWZjYjU5OGRmMzk5MGE3ZTcifX19"
                    lore {
                        -"&fСоздайте новую группу для"
                        -"&fсовместной игры и общения!"
                        -""
                        -"&fЧтобы вступать в другие группы,"
                        -"&fвам нужно будет удалить эту"
                        -""
                        -"&aНажмите, чтобы создать группу"
                    }
                    actions {
                        Party.create(player)
                        defaultPartyMenu(player)
                    }
                }
                'O' {
                    name = "&aОнлайн"
                    material = Material.PAPER
                    lore {
                        -"&fВы можете пригласить любого"
                        -"&fигрока, для игры вместе!"
                        -""
                        -"&fОнлайн на сервере: &a${SnapiLibrary.getOnlinePlayers().size}"
                        -"&fОнлайн друзей: &a${User[player.name].friends.size}"
                    }
                }
                'I' {
                    name = "&aО группах"
                    material = Material.REDSTONE
                    lore {
                        -"&fВы можете создать группу"
                        -"&fс &aчетырьмя &fигроками для"
                        -"&fсовместной игры на сервере!"
                    }
                }
            }
        }
    }

    fun defaultPartyMenu(player: Player) {
        generatorPanel<NetworkPlayer?>(player) {
            title = "Группа"
            update = 1.seconds

            layout {
                -"FFFFFFFFF"
                -"FFFFIFFFF"
                -"FFFFFFFFF"
                -"F F F F F"
                -"FFFFFFFFF"
                -"FFPFFFDFF"
            }

            generatorSource {
                val party = Party[player.name]
                if (party == null) {
                    nullPartyMenu(player)
                    emptyList<NetworkPlayer>()
                } else {
                    val list: MutableList<NetworkPlayer?> = mutableListOf(
                        party.leader,
                        *party.players.toTypedArray(),
                        *party.invitations.toTypedArray()
                    )
                    val need = party.maxSize - list.size
                    if (need != 0) {
                        repeat(need) { list.add(null) }
                    }
                    list
                }
            }
            generatorOutput = {
                val party = Party[player.name]
                if (party == null) {
                    Item()
                } else if (it != null) {
                    if (party.isLeader(it)) {
                        Item(
                            name = "&a${it.getName()} &7(лидер)",
                            head = it.getName(),
                            lore = listOf(
                                "",
                                "&fСейчас находится: &a${it.getCurrentServer()?.name ?: "Где-то в переходе..."}",
                                "",
                                "&aНажмите, чтобы открыть профиль"
                            ),
                            clickAction = {
                                Bukkit.dispatchCommand(player, "profile ${it.getName()}")
                            }
                        )
                    } else if (party.inParty(it)) {
                        Item(
                            name = "&a${it.getName()}${if (User[player.name].friends.contains(it.getName())) " &7(друг)" else ""}",
                            head = it.getName(),
                            lore = listOf(
                                "",
                                "&fСейчас находится: &a${it.getCurrentServer()?.name ?: "Где-то в переходе..."}",
                                "",
                                "&a${if (party.isLeader(player.name)) "&aНажмите ЛКМ, чтобы открыть профиль\n&aНажмите СКМ, чтобы назначить лидером группы\n&aНажмите ПКМ, чтобы исключить участника" else "&aНажмите, чтобы открыть профиль"}"
                            ),
                            clickAction = {
                                if (party.isLeader(player.name)) {
                                    if (type == ClickType.LEFT || type == ClickType.SHIFT_LEFT) {
                                        Bukkit.dispatchCommand(player, "profile ${it.getName()}")
                                    }
                                    if (type == ClickType.MIDDLE) {
                                        party.changeLeader(it)
                                    }
                                    if (type == ClickType.RIGHT || type == ClickType.SHIFT_RIGHT) {
                                        party.removePlayer(it)
                                    }
                                } else {
                                    Bukkit.dispatchCommand(player, "profile ${it.getName()}")
                                }
                            }
                        )
                    } else {
                        Item(
                            name = "&e${it.getName()} &7(приглашён)",
                            lore = listOf(
                                "&fИгрок приглашён. После принятия",
                                "&fон появится в вашей группе",
                                "",
                                "&cНажмите, чтобы отменить приглашение",
                            ),
                            material = Material.YELLOW_STAINED_GLASS_PANE,
                            clickAction = {
                                if (party.isLeader(player.name)) {
                                    party.removeInvitation(it)
                                }
                            }
                        )
                    }
                } else {
                    Item(
                        name = "&aПустой слот",
                        material = Material.LIME_STAINED_GLASS_PANE,
                        lore = if (party.isLeader(player.name)) listOf(
                            "&fВы можете пригласить",
                            "&fигрока в свою группу",
                            "",
                            "&aНажмите, чтобы открыть список",
                        ) else listOf(
                            "&fЛидер группы может",
                            "&fпригласить игрока",
                        ),
                        clickAction = {
                            if (party.isLeader(player.name)) {
                                if (CooperationApi.playerWithoutParty().isEmpty()) {
                                    player.message("&fУ вас &cнет &fигроков, которых вы можете добавить в группу")
                                } else {
                                    playerListPartyMenu(player)
                                }
                            }
                        }
                    )
                }
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'I' {
                    material = Material.BOOK
                    name = "&aИнформация о группе"
                    lore {
                        - ""
                        - "&fЛидер: &a${Party[player.name]?.leader?.getName() ?: "Что-то не так..."}"
                        - "&fУчастников: &a${Party[player.name]?.size ?: "0"}/${Party[player.name]?.maxSize ?: "4"}"
                        - "&fВремя создания: &a${Party[player.name]?.date?.toDate("dd/MM/yyyy HH:mm") ?: "&cНет игроков"}"
                    }
                }
                'D' {
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQxYTNjOTY1NjIzNDg1MjdkNTc5OGYyOTE2MDkyODFmNzJlMTZkNjExZjFhNzZjMGZhN2FiZTA0MzY2NSJ9fX0="
                    name = if (Party[player.name]?.isLeader(player.name) == true) "&cРаспустить группу" else "&cВыйти из группы"
                    lore {
                        - if (Party[player.name]?.isLeader(player.name) == true)
                            "&fРаспустите группу, чтобы\n&fзавершить совместную игру\n\n&aНажмите, чтобы распустить"
                        else
                            "&fВыйти из группы и продолжить\n&fиграть в одиночку\n\n&aНажмите, чтобы покинуть"
                    }
                    actions {
                        val networkPlayer = OfflineNetworkPlayer(player.name)
                        val party = Party[networkPlayer] ?: return@actions
                        
                        player.closeInventory()
                        
                        if (party.isLeader(networkPlayer)) {
                            party.remove()
                        } else {
                            party.leave(networkPlayer)
                        }
                    }
                }
                'P' {
                    name = "&aОнлайн игроков"
                    material = Material.PAPER
                    lore {
                        -""
                        -"&fОнлайн на сервере: &a${SnapiLibrary.getOnlinePlayers().size}"
                        -"&fОнлайн друзей: &a${User[player.name].friends.size}"
                    }
                }
            }

            replacements {
                -("leader" to { Party[player.name]?.leader ?: ""  })
                -("party_size" to { Party[player.name]?.size ?: 0 })
                -("party_max_size" to { Party[player.name]?.maxSize ?: 0 })
            }
        }
    }

    fun playerListPartyMenu(player: Player) {
        generatorPanel<NetworkPlayer>(player) {
            title = "Список игроков"
            update = 1.seconds


            generatorSource { CooperationApi.playerWithoutParty().toList() }
            generatorOutput = {
                Item(
                    name = "&a${it.getName()}",
                    head = it.getName(),
                    lore = listOf(
                        "&fВы можете пригласить",
                        "&fэтого игрока в группу",
                        "",
                        "&aНажмите, чтобы пригласить",
                    ),
                    clickAction = {
                        val party = Party[player.name] ?: return@Item
                        party.createInvitation(it)
                        defaultPartyMenu(player)
                    }
                )
            }
            comparator = compareBy<NetworkPlayer> {
                val user = User[player]
                user.friends.contains(it.getName())
            }.thenBy { it.getName() }

            layout {
                -"FFFFFFFFF"
                -"F       F"
                -"F       F"
                -"F       F"
                -"FFFFFFFFF"
                -"PFFFRFFFN"
            }

            items {
                'F' {
                    material = Material.AIR
                }
                'R' {
                    material = Material.ARROW
                    name = "&aВернуться"
                    actions {
                        defaultPartyMenu(player)
                    }
                }
                'P' {
                    material = Material.ARROW
                    name = "&aПредыдущая страница"
                    condition { prevPage() }
                    actions {
                        prevPage()
                    }
                }
                'N' {
                    material = Material.ARROW
                    name = "&aСледующая страница"
                    condition { nextPage() }
                    actions {
                        nextPage()
                    }
                }
            }
        }
    }

    fun friendMenu(player: Player) {
        generatorPanel<User>(player) {
            title = "Друзья"

            layout {
                -"#########"
                -"#       #"
                -"#       #"
                -"#########"
                -"P# ### #N"
            }

            generatorSource { User[player].friends.map { User[it] } }
            generatorOutput = {
                Item(
                    name = "&a${it.name}",
                    head = it.name,
                )
            }
            comparator = compareBy<User> { OfflineNetworkPlayer(it.name).isOnline() }.thenBy { it.name }

            items {
                '#' {
                    material = Material.AIR
                }
                'P' {
                    name = "&aПредыдущая страница"
                    material = Material.ARROW
                    condition { prevPage() }
                    actions {
                        prevPage()
                    }
                }
                'N' {
                    name = "&aСледующая страница"
                    material = Material.ARROW
                    condition { nextPage() }
                    actions {
                        nextPage()
                    }
                }
            }
        }
    }
}