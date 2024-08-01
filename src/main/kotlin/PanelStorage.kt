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
    fun partyMenu(player: Player, backProfile: Boolean = false) {
        if (Party[player.name] == null) {
            nullPartyMenu(player, backProfile)
        } else {
            defaultPartyMenu(player, backProfile)
        }
    }

    fun nullPartyMenu(player: Player, backProfile: Boolean = false) {
        panel(player) {
            title = "Создать группу"
            layout {
                -"         "
                -"         "
                -"    G    "
                -"         "
                -"         "
                -"  O B I  "
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
                        -"&fОнлайн друзей: &a${User[player.name].friends.size}"
                        -"&fОнлайн на сервере: &a${SnapiLibrary.getOnlinePlayers().size}"
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
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { backProfile }
                    actions {
                        ru.snapix.profile.PanelStorage.profile(player)
                    }
                }
            }
        }
    }

    fun defaultPartyMenu(player: Player, backProfile: Boolean = false) {
        generatorPanel<NetworkPlayer?>(player) {
            title = "Группа"
            update = 1.seconds

            layout {
                -"FFFFFFFFF"
                -"FFFFIFFFF"
                -"FFFFFFFFF"
                -"F F F F F"
                -"FFFFFFFFF"
                -"FFPFBFDFF"
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
                                ru.snapix.profile.PanelStorage.otherProfile(player, it, backParty = true)
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
                                        ru.snapix.profile.PanelStorage.otherProfile(player, it, backParty = true)
                                    }
                                    if (type == ClickType.MIDDLE) {
                                        party.changeLeader(it)
                                    }
                                    if (type == ClickType.RIGHT || type == ClickType.SHIFT_RIGHT) {
                                        party.removePlayer(it)
                                    }
                                } else {
                                    ru.snapix.profile.PanelStorage.otherProfile(player, it, backParty = true)
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
                        -"&fОнлайн друзей: &a${User[player.name].friends.size}"
                        -"&fОнлайн на сервере: &a${SnapiLibrary.getOnlinePlayers().size}"
                    }
                }
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { backProfile }
                    actions {
                        ru.snapix.profile.PanelStorage.profile(player)
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
                        Bukkit.dispatchCommand(player, "party invite ${it.getName()}")
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

    fun friendMenu(player: Player, backProfile: Boolean = false) {
        generatorPanel<User?>(player) {
            title = "Друзья"
            update = 1.seconds

            layout {
                -"#########"
                -"#       #"
                -"#       #"
                -"#       #"
                -"#########"
                -"P#O#B#A#N"
            }

            generatorSource {
                val friends: MutableList<String> = User[player].friends.toMutableList()
                if (friends.isEmpty()) {
                    val list = arrayOfNulls<String>(10).map { it as User? }.toMutableList()
                    list.add(User[player])
                    list
                } else {
                    friends.map { User[it] }.sortedWith(compareBy<User> { OfflineNetworkPlayer(it.name).isOnline() }.thenBy { it.name })
                }
            }
            generatorOutput = {
                if (it == null) {
                    Item()
                } else if (it.name.equals(player.name, ignoreCase = true)) {
                    Item(
                        name = "&cНет друзей",
                        material = Material.RED_STAINED_GLASS_PANE,
                        lore = listOf(
                            "&fВы можете добавить до",
                            "&a${User[player].maxSize} &fдрузей! Так, вы не",
                            "&fбудете терять с ними связь",
                            "",
                            "&fЧтобы добавить игрока в",
                            "&fдрузья, нажмите на &a&l+&r &fвнизу",
                        )
                    )
                } else {
                    val networkPlayer = SnapiLibrary.getPlayer(it.name)
                    if (networkPlayer.isOnline()) {
                        Item(
                            name = "&a${it.name}",
                            head = it.name,
                            lore = listOf(
                                "",
                                "&fСтатус: &aОнлайн",
                                "&fСейчас находится: &a${networkPlayer.getCurrentServer()?.name ?: "Где-то в переходе..."}",
                                "",
                                "&aНажмите ЛКМ, чтобы добавить в группу",
                                "&aНажмите СКМ, чтобы удалить из друзей",
                                "&aНажмите ПКМ, чтобы открыть профиль",
                            ),
                            clickAction = {
                                if (type == ClickType.LEFT) {
                                    Bukkit.dispatchCommand(player, "party invite ${it.name}")
                                }
                                if (type == ClickType.MIDDLE) {
                                    Bukkit.dispatchCommand(player, "friend remove ${it.name}")
                                }
                                if (type == ClickType.RIGHT) {
                                    ru.snapix.profile.PanelStorage.otherProfile(
                                        player,
                                        SnapiLibrary.getPlayer(it.name),
                                        backFriend = true
                                    )
                                }
                            }
                        )
                    } else {
                        Item(
                            name = "&7${it.name}",
                            head = it.name,
                            lore = listOf(
                                "",
                                "&fСтатус: &cОфлайн",
                                "",
                                "&cНажмите СКМ, чтобы удалить из друзей",
                                "&cНажмите ПКМ, чтобы открыть профиль",
                            ),
                            clickAction = {
                                if (type == ClickType.MIDDLE) {
                                    Bukkit.dispatchCommand(player, "friend remove ${it.name}")
                                }
                                if (type == ClickType.RIGHT) {
                                    ru.snapix.profile.PanelStorage.otherProfile(
                                        player,
                                        SnapiLibrary.getPlayer(it.name),
                                        backFriend = true
                                    )
                                }
                            }
                        )
                    }
                }
            }

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
                'O' {
                    name = "&aОнлайн игроков"
                    material = Material.PAPER
                    lore {
                        -""
                        -"&fОнлайн друзей: &a${if (User[player].friends.isEmpty()) "&cНет друзей" else "${User[player].friends.map { OfflineNetworkPlayer(it) }.filter { it.isOnline() }.size}/${User[player].friends.size}"}"
                        -"&fОнлайн на сервере: &a${SnapiLibrary.getOnlinePlayers().size}"
                    }
                }
                'A' {
                    name = "&aДобавить друга"
                    head = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBiNTVmNzQ2ODFjNjgyODNhMWMxY2U1MWYxYzgzYjUyZTI5NzFjOTFlZTM0ZWZjYjU5OGRmMzk5MGE3ZTcifX19"
                    lore {
                        -"&fВы можете добавить до"
                        -"&a${User[player].maxSize} &fигроков в друзья"
                        -""
                        -"&aНажмите, чтобы добавить"
                    }
                    actions {
                        if (CooperationApi.playerWithoutFriend(player).isEmpty()) {
                            player.message("&fУ вас &cнет &fигроков, которых вы можете добавить в друзья")
                        } else {
                            playerListFriendMenu(player)
                        }
                    }
                }
                'B' {
                    name = "&aВернуться назад"
                    material = Material.ARROW
                    condition { backProfile }
                    actions {
                        ru.snapix.profile.PanelStorage.profile(player)
                    }
                }
            }
        }
    }

    fun playerListFriendMenu(player: Player) {
        generatorPanel<NetworkPlayer>(player) {
            title = "Список игроков"
            update = 1.seconds

            generatorSource {
                CooperationApi.playerWithoutFriend(player).toList()
            }
            generatorOutput = {
                Item(
                    name = "&a${it.getName()}",
                    head = it.getName(),
                    lore = listOf(
                        "&fВы можете отправить запрос",
                        "&fна дружбу этому игроку",
                        "",
                        "&fУровень: &a${getStatisticInt(it, "alonsolevels_lastlevel")}",
                        "&fЛюбимый режим: {favourite_game}",
                        "",
                        "&aНажмите, чтобы отправить",
                    ),
                    clickAction = {
                        Bukkit.dispatchCommand(player, "friend add ${it.getName()}")
                        friendMenu(player)
                    }
                )
            }
            comparator = compareBy { it.getName() }

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
                        friendMenu(player)
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

            replacements {
                - ("favourite_game" to {
                    val networkPlayer = OnlineNetworkPlayer(player.name)
                    val map = mapOf(
                        "skywars" to getStatisticInt(networkPlayer, "skywars_played"),
                        "bedwars" to getStatisticInt(
                            networkPlayer,
                            "bedwars_gamesplayed"
                        ),
                        "murdermystery" to getStatisticInt(networkPlayer, "murdermystery_games_played"),
                        "thebridge" to getStatisticInt(
                            networkPlayer,
                            "thebridge_gamesplayed"
                        )
                    ).filter { it.value > 0 }
                    val max = map.maxByOrNull { it.value }
                    when (max?.key) {
                        "skywars" -> "&bSkyWars"
                        "bedwars" -> "&cBedWars"
                        "murdermystery" -> "&eMurderMystery"
                        "thebridge" -> "&9TheBridge"
                        else -> "&cНет"
                    }
                })
            }
        }
    }

    fun getStatisticInt(player: NetworkPlayer, placeholder: String): Int {
        return try {
            player.getStatistics()[placeholder]!!.invoke().toString().toInt()
        } catch (_: Exception) {
            0
        }
    }
}