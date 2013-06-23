package ee.lutsu.alpha.mc.mytown;

import java.util.HashMap;

public enum Term
{
	// General & events
	ChatFormat("§f[$color$$channel$§f]$prefix$$name$$postfix$$color$: $msg$"),
	EmoteFormat("§f[$color$$channel$§f]* $prefix$$name$$postfix$$color$ $msg$"),
	PrivMsgFormatIn("§7[$sprefix$$sname$$spostfix$ §7-> Me] $msg$"),
	PrivMsgFormatOut("§7[Me -> $prefix$$name$$postfix$§7] $msg$"),
	
	ChatErrNotInTown("You cannot use townchat if you are not in a town"),
	ChatErrNotInNation("You cannot use nationchat if your town is not part of a nation"),
	ChatAloneInChannel("§7§oSo lonely..."),
	ChatListStart("Switch chat channel"),
	ChatListEntry("   %s%s §f[%s%s§f]"),
	ChatSwitch("Changed active channel to [%s%s§f] %s%s"),
	ChatSwitchAlreadyIn("You are already in channel [%s%s§f] %s%s"),
	NoBedMessage("You don't have a bed to spawn in"),
	
	SevereLevel("SEVERE"),
	WarningLevel("WARNING"),
	InfoLevel("INFO"),
	
	MinecartMessedWith("Someone is messing with your towns minecart"),
	
	// nation status
	NationStatusName("§6--------[[ %s%s§6 ]]--------"),
	NationStatusGeneral("§2Size: §b%s§2/§b%s §2Members: §b%s"),
	NationStatusCapital("§2Capital: §b%s"),
	NationStatusTowns("§2Towns: §b%s"),
	
	// status
	TownStatusName("§6--------[[ %s%s§6 ]]--------"),
	TownStatusGeneral("§2Town blocks: §b%s§2/§b%s §2Nation: §b%s"),
	TownStatusMayor("§2Mayor: §f%s"),
	TownStatusAssistants("§2Assistants: §f%s"),
	TownStatusResidents("§2Residents: §f%s"),
	
	// resident status
	ResStatusName("§6--------[[ %s§6 ]]--------"),
	ResStatusLocation("§2Location: §b%s @ dim %s (%s,%s,%s)"),
	ResStatusGeneral1("§2Member from: §b%s"),
	ResStatusGeneral2("§2Last online: §b%s"),
	ResStatusTown("§2Member of: §b%s§2, §a%s"),
	ResStatusFriends("§2Friends: §f%s"),
	ResStatusFriends2("§2Friend of: §f%s"),
	
	// commands
	OnlineCommand("online"),
	OnlineCommandAliases(""),
	OnlineCmdListStart("§aOnline [%s]§f: %s"),
	
	TownCommand("mytown"),
	TownCommandAliases("t town"),
	
	TownAdmCommand("mytownadm"),
	TownAdmCommandAliases("ta"),
	
	ChannelCommand("ch"),
	ChannelCommandAliases(""),
	
	TownCommandDesc("§5MyTown §2- Be protected by towns"),
	LineSeperator("§6--------[[ §5MyTown§6 ]]--------"),
	
	CommandHelpStart("§4Commands: "),
	CommandHelpStartSub("§4%s commands: "),
	CommandHelp("help"),
	
	CommandHelpAssistant("assistant"),
	CommandHelpAssistantDesc("Show the town assistant commands"),
	
	CommandHelpMayor("mayor"),
	CommandHelpMayorDesc("Show the town mayor commands"),
	
	CommandHelpNation("nation"),
	CommandHelpNationDesc("Show the nation commands"),
	
	// all
	TownCmdMap("map"),
	TownCmdMapArgs("[on|off]"),
	TownCmdMapDesc("Shows map or toggles map mode on/off"),
	
	TownCmdInfo("info"),
	TownCmdInfoArgs("townname"),
	TownCmdInfoDesc("Shows info about the town"),
	
	TownCmdRes("res"),
	TownCmdResArgs("playername"),
	TownCmdResDesc("Shows info about the resident"),
	
	TownCmdList("list"),
	TownCmdListDesc("Lists all towns"),
	TownCmdListStart("§aTowns [%s]§f: %s"),
	TownCmdListEntry("§f%s§a[%s]"),
	
	TownCmdFriend("friend"),
	TownCmdFriendArgs("add|remove name"),
	TownCmdFriendArgsAdd("add"),
	TownCmdFriendArgsRemove("remove"),
	TownCmdFriendDesc("Adds or removes friends"),
	
	TownCmdSpawn("spawn"),
	TownCmdSpawnArgs("[name]"),
	TownCmdSpawnDesc("Teleports you to [the specified] town spawn"),
	
	// mayor commands
	TownCmdAssistant("assistant"),
	TownCmdAssistantArgs("add|remove name"),
	TownCmdAssistantArgs1("add"),
	TownCmdAssistantArgs2("remove"),
	TownCmdAssistantDesc("Allows people to manage residents and land"),
	
	TownCmdMayor("mayor"),
	TownCmdMayorArgs("[name]"),
	TownCmdMayorDesc("Sets a new mayor"),
	
	TownCmdRename("rename"),
	TownCmdRenameArgs("[name]"),
	TownCmdRenameDesc("Sets a new town name"),
	
	TownCmdBounce("bounce"),
	TownCmdBounceDesc("Toggle non-member bounce mode"),
	
	TownCmdDelete("delete"),
	TownCmdDeleteDesc("Deletes your town"),
	TownCmdDeleteAction("Are you sure? Use /t delete ok"),

	// assistant commands
	TownCmdClaim("claim"),
	TownCmdClaimArgs("[rect X]"),
	TownCmdClaimArgs1("rect"),
	TownCmdClaimDesc("Claim land for your town"),
	
	TownCmdUnclaim("unclaim"),
	TownCmdUnclaimArgs("[rect X]"),
	TownCmdUnclaimArgs1("rect"),
	TownCmdUnclaimDesc("Unclaim land"),
	
	TownCmdInvite("invite"),
	TownCmdInviteArgs("name"),
	TownCmdInviteDesc("Invite a player to join your town"),
	
	TownCmdKick("kick"),
	TownCmdKickArgs("name"),
	TownCmdKickDesc("Remove the player from your town"),
	
	TownCmdPlot("assignplot"),
	TownCmdPlotArgs("name"),
	TownCmdPlotDesc("Assigns your current plot to the player"),
	
	TownCmdSetSpawn("setspawn"),
	TownCmdSetSpawnDesc("Sets your town spawn"),
	
	// resident commands
	TownCmdLeave("leave"),
	TownCmdLeaveDesc("Leave the town"),
	
	TownCmdOnline("online"),
	TownCmdOnlineDesc("Shows the players online in your town"),
	
	// non-residents
	TownCmdNew("new"),
	TownCmdNewArgs("name"),
	TownCmdNewDesc("Creates a new town"),
	
	TownCmdAccept("accept"),
	TownCmdAcceptDesc("Accept a town invitation"),
	TownCmdAcceptDesc2("Accept a nation invitation"),
	
	TownCmdDeny("deny"),
	TownCmdDenyDesc("Deny a town invitation"),
	TownCmdDenyDesc2("Deny a nation invitation"),
	
	// nation commands
	TownCmdNation("nation"),
	
	TownCmdNationNew("new"),
	TownCmdNationNewArgs("name"),
	TownCmdNationNewDesc("Creates a new nation"),
	
	TownCmdNationDel("delete"),
	TownCmdNationDelDesc("Deletes your nation"),
	
	TownCmdNationInvite("invite"), // use /t accept & reject
	TownCmdNationInviteArgs("name"),
	TownCmdNationInviteDesc("Invites a town to your nation"),
	
	TownCmdNationLeave("leave"),
	TownCmdNationLeaveDesc("Leave the nation"),
	
	TownCmdNationKick("kick"),
	TownCmdNationKickArgs("name"),
	TownCmdNationKickDesc("Kicks a town from your nation"),
	
	TownCmdNationTransfer("transfer"),
	TownCmdNationTransferArgs("name"),
	TownCmdNationTransferDesc("Transfers the capitol to a member town"),

	TownCmdNationInfo("info"),
	TownCmdNationInfoArgs("[name]"),
	TownCmdNationInfoDesc("Shows info about a nation"),
	
	TownCmdNationList("list"),
	TownCmdNationListStart("§aNations [%s]§f: %s"),
	TownCmdNationListEntry("%s[%s]"),
	TownCmdNationListDesc("Lists all nations"),
	
	// admin commands
	TownadmCmdReload("reload"),
	TownadmCmdReloadDesc("Reload the config, db and terms"),
	
	TownadmCmdNew("new"),
	TownadmCmdNewArgs("townname mayorname"),
	TownadmCmdNewDesc("Creates a new town"),
	
	TownadmCmdDelete("delete"),
	TownadmCmdDeleteArgs("townname"),
	TownadmCmdDeleteDesc("Deletes a town"),
	
	TownadmCmdSet("set"),
	TownadmCmdSetArgs("townname rank name, name .."),
	TownadmCmdSetDesc("Adds members to a town or sets ranks"),
	
	TownadmCmdRem("rem"),
	TownadmCmdRemArgs("townname name, name .."),
	TownadmCmdRemDesc("Removes members from a town"),
	
	TownadmCmdExtra("extra"),
	TownadmCmdExtraArgs("townname count"),
	TownadmCmdExtraDesc("Adds or removes extra blocks in a town"),
	
	TownadmCmdExtraRes("extrares"),
	TownadmCmdExtraResArgs("playername add|sub|set amount"),
	TownadmCmdExtraResDesc("Adds, removes or sets a player extra blocks value"),
	
	TownadmCmdClaim("claim"),
	TownadmCmdClaimArgs("townname [playername] [x.y:x.y[:dim]]"),
	TownadmCmdClaimDesc("Sets the current (or specified) plot to the town (and player)"),

	TownadmCmdWipeDim("wipedim"),
	TownadmCmdWipeDimArgs("dimension_id"),
	TownadmCmdWipeDimDesc("Deletes all town blocks from the specified dimension id"),
	
	TownadmCmdResetFocusedChannels("reschannels"),
	TownadmCmdResetFocusedChannelsDesc("Reset the currently selected channel for all users to the default"),
	
	TownadmCmdSnoopPrivateChat("snoop"),
	TownadmCmdSnoopPrivateChatDesc("Starts or stops logging private chat into server log"),

	// Town errors
	ErrCannotAccessCommand("§4You cannot access this command"),
	TownErrAlreadyClaimed("This block is claimed by another town"),
	TownErrNotClaimedByYourTown("This block is not claimed by your town"),
	TownErrPlayerAlreadyInTown("That player is already part of a town"),
	TownErrPlayerNotFound("Player not found"),
	TownErrPlayerNotFoundOrOnline("Player not found or not online"),
	TownErrBlockTooCloseToAnotherTown("This block is too close to another town"),
	TownErrCreatorPartOfTown("You cannot be part of a town"),
	TownErrTownNameCannotBeEmpty("Cannot set a empty name"),
	TownErrTownNameAlreadyInUse("This town name has already need used"),
	TownErrNoFreeBlocks("You don't have any free blocks"),
	
	ErrUnknowCommand("§4Unknown command. Use tab for autocomplete or /t help to find correct commands. Commands are different based on your town rank."),
	
	TownErrCmdUnknownArgument("Unknown argument: §4%s"),
	TownErrCmdNumberFormatException("The input isn't numerical"),
	
	TownErrInvitationSelf("The fuck are you doing? Invite OTHERS"),
	TownErrInvitationAlreadyInYourTown("Hes in your town moron"),
	TownErrInvitationActive("The player has a pending invitation already"),
	TownErrInvitationInTown("The player is already in a town"),
	
	TownErrPlayerNotInYourTown("The player is not in your town"),
	TownErrPlayerDoesntHaveAccessToTownManagement("The player can't access town management by perm nodes"),
	TownErrCannotDoWithYourself("You cannot do this with yourself"),
	TownErrPlayerIsAlreadyMayor("The player is already a mayor"),
	TownErrPlayerIsAlreadyAssistant("The player is already an assistant"),
	TownErrPlayerIsNotAssistant("The player isn't an assistant"),
	TownErrCannotUseThisDemoteMayor("Cannot use this to demote a mayor"),
	
	TownErrCannotKickAssistants("You cannot kick another assistant"),
	TownErrCannotKickMayor("You cannot kick mayors"),
	TownErrMayorsCantLeaveTheTown("You cannot leave the town as a mayor"),
	TownErrCannotKickYourself("You cannot kick yourself"),
	
	TownErrYouDontHavePendingInvitations("You don't have any pending invitation"),
	TownErrNotFound("Town named %s cannot be found"),
	TownErrSpawnNotSet("Town spawn isn't set"),
	
	// Globals
	TownBroadcastCreated("%s has just founded a new town called %s"),
	TownBroadcastDeleted("The town of %s went like POOF"),
	
	TownBroadcastLoggedIn("%s just came online"),
	TownBroadcastLoggedOut("%s just went offline"),
	
	TownadmCreatedNewTown("Town named %s created for mayor %s"),
	TownadmDeletedTown("Town named %s deleted"),
	
	TownadmResidentsSet("Town residents set"),
	TownadmExtraSet("Town extra blocks value set"),
	TownadmResExtraSet("Resident extra blocks value set"),
	
	PlayerEnteredWild("§aYou just entered the §2wilderness"),
	PlayerEnteredTown("§aWelcome to §4%s"),
	PlayerEnteredOwnTown("§aWelcome back to §2%s"),
	PlayerEnteredOtherPlot("§6~%s"),
	PlayerEnteredOwnPlot("§2~%s"),
	PlayerEnteredUnclaimedPlot("§6~unassigned"),
	
	PlayerMapModeOn("§aTown map mode is now §2ON"),
	PlayerMapModeOff("§aTown map mode is now §4OFF"),
	
	TownBlocksClaimed("§a%s blocks claimed [%s]"),
	TownBlocksClaimedDisclaimer("§a%s blocks requested of which %s are able to be claimed and %s already owned"),
	TownBlocksClaimedDisclaimer2("§4First error: %s"),
	TownBlocksUnclaimed("§a%s blocks unclaimed [%s]"),
	TownMapHead("§6--------[[ §5MyTown§6 ]]--------"),
	
	TownInvitedPlayer("§aSent the town invitation to %s"),
	TownKickedPlayer("%s kicked the player %s from town"),
	TownPlayerLeft("%s left the town"),
	TownInvitation("§a%s would like you to join his town %s. Use /t §2accept §aor /t §4deny §ato reply"),
	TownRenamed("Your town charter has been renamed to %s"),
	
	TownPlayerPromotedToAssistant("Player %s promoted to be an assistant"),
	TownPlayerDemotedFromAssistant("Player %s demoted to a normal resident"),
	TownPlayerPromotedToMayor("Player %s promoted to be town mayor"),
	
	
	TownPlayerJoinedTown("The player %s has joined the town"),
	TownPlayerDeniedInvitation("You have denied the invitation"),
	
	TownadmModReloaded("The mod has been reloaded"),
	
	TownPlayersOnlineStart("§aPlayers online: %s"),
	TownYouCannotEnter("§aYou cannot enter the town §4%s§a. Town rules."),
	OutofBorderCannotEnter("§aYou cannot walk over the edge of the (pizza)world!"),
	
	TownSpawnSet("§aTown spawn has been set"),
	
	TownBouncingChanged("§aThe town is now in %s §astatus"),
	ChatTownLogFormat("§f[§a%s§f]%s"),
	ChatNationLogFormat("§f[§2%s§f]%s"),
	
	TownSpawnReset("Your spawn has been reset. Please set a new one"),
	
	// nation
	NationBroadcastCreated("§eThe town of %s grew into the nation of %s"),
	NationBroadcastDeleted("§eThe nation of %s has fallen"),
	NationTownJoinedNation("The town %s has joined the nation"),
	
	NationLeft("§aYou left the nation of %s"),
	NationNowCapital("§aYour town was made the capital of %s"),
	
	NationDeleteConfirmation("§aAre you sure? Use \"... delete yes\" to delete your nation"),
	NationCapitalTransfered("§aCapital transfered to %s"),
	TownKickedFromNation("§aTown '%s' kicked out of the nation"),
	NationInvitation("§a%s would like you to join his nation %s. Use /t §2accept §aor /t §4deny §ato reply"),
	NationInvitedPlayer("§aSent the town invitation to %s of %s"),
	NationPlayerDeniedInvitation("You have denied the invitation"),
	
	TownErrNationNotFound("Cannot find the nation by the name '%s'"),
	TownErrAlreadyInNation("This town is already part of a nation"),
	TownErrNationNameInUse("Name already in use"),
	TownErrNationNameCannotBeEmpty("Cannot set a empty name"),
	TownErrNationSelfNotPartOfNation("Your're not part of any nation"),
	TownErrNationNotPartOfNation("That town isn't part of this nation"),
	TownErrNationNoMayorOnline("The town of %s has no mayors currently online. Try again later"),
	TownErrNationCannotKickSelf("You cannot kick your own town"),
	TownErrNationCannotTransferSelf("You are already the capital"),
	TownErrNationYouDontHavePendingInvitations("You don't have any pending invitation"),
	TownErrNationInvitingSelf("Why are you inviting your own town?"),
	TownErrNationCantRemoveCapital("Cannot remove the capital town"),
	TownErrCannotDeleteInNation("Cannot delete the town when in a nation"),
	
	// permissions - town command
	TownCmdPerm("perm"),
	TownCmdPermArgs("town|res|plot [force [key]|set key [val]]"),
	TownCmdPermArgsTown("town"),
	TownCmdPermArgsResident("res"),
	TownCmdPermArgsPlot("plot"),
	TownCmdPermArgs2Set("set"),
	TownCmdPermArgs2Force("force"),
	TownCmdPermDesc("Shows, sets or forces the permissions"),

	// permissions - town admin command
	TownadmCmdPerm("perm"),
	TownadmCmdPermArgs("town|plot|server|wild|wild:# [(force [key])|(set key [val])]"),
	TownadmCmdPermArgsServer("server"),
	TownadmCmdPermArgsWild("wild"),
	TownadmCmdPermArgsWild2("wild:"),
	TownadmCmdPermArgs2Set("set"),
	TownadmCmdPermArgs2Force("force"),
	TownadmCmdPermDesc("Shows, sets or forces the permissions"),
	
	// per general
	PermForced("§aAll childs have been updated to inherit from the node '§2%s§a' for perm '§2%s§a'"),
	PermSetDone("§aPermission '§2%s§a' set for the node '§2%s§a'"),
	
	TownPlotAssigned("§aPlot(s) assigned to '%s'"),
	TownPlotUnAssigned("§aPlot(s) unassigned"),
	
	// per errors
	ErrPermSettingNotFound("The specified setting '%s' cannot be found"),
	ErrPermSettingCollectionNotFound("The specified node '%s' doesn't exist"),
	ErrPermNoChilds("The current permission node has no children"),
	ErrPermSupportedValues("§4Value type: §2%s§4, Supported values: §2%s"),
	ErrPermInvalidValue("§4Error: §2%s§4, Supported values: §2%s"),
	ErrPermYouDontHaveTown("You don't belong to any town"),
	ErrPermPlotNotInTown("The current block doesn't belong to any town"),
	ErrPermPlotNotInYourTown("The current block doesn't belong to your town"),
	ErrPermRankNotEnough("You have to be atleast the assistant of the town for this"),
	
	ErrPermCannotBuildHere("§4You cannot build in this area"),
	ErrPermCannotAccessHere("§4You cannot access things here"),
	ErrPermCannotPickup("§4You cannot pick items up here"),
	
	ErrPermCannotInteract("You cannot interact with the target here"),
	ErrPermCannotAttack("You cannot attack the target here"),
	
	ErrNotUsableByConsole("This command can't be run from console"),
	ErrPlayerAlreadyInFriendList("The player '%s' is already in your friends list"),
	ErrPlayerNotInFriendList("The player '%s' is not in your friends list"),
	
	// spawn command
	SpawnCmdTeleportStarted("§aStarted casting the spawn teleport spell. It will take %s seconds"),
	SpawnCmdTeleportNearStarted("§e%s started casting a %s second spawn teleport spell"),
	SpawnCmdTeleportEnded("§aWhoooOooOooooOoo"),
	SpawnCmdTeleportReset("§4Teleport spell casting interrupted"),
	
	// home commands
	HomeCmdNoHomeByName("No home by that name was found"),
	HomeCmdDimNotSpawnDim("You cannot set the bed location in this dimension"),
	HomeCmdOwnerNotOnline("The bed owner is not online"),
	HomeCmdCannotDeleteBed("You cannot remove the bed spawn location"),
	HomeCmdNoHomes("You have no homes set"),
	HomeCmdTeleportStarted("§aStarted casting the home teleport spell. It will take %s seconds"),
	HomeCmdTeleportNearStarted("§e%s started casting a %s second home teleport spell"),
	HomeCmdHomeSet("§aHome set"),
	HomeCmdHome2Set("§aHome '%s' set"),
	HomeCmdHomeDeleted("§aHome location deleted"),
	HomeCmdHome2Deleted("§aHome location '%s' deleted"),
	HomeCmdCannotSetHere("You can set your home only in places where you can build"),
	HomeCmdHomesTitle("§2Your homes: %s"),
	HomeCmdHomesItem("§a%s§2"),
	HomeCmdHomesUnaccessibleItem("§4%s§2"),
	HomeCmdHomesItem2("   §a%s§2 (%s, %s, %s, %s)"),
	HomeCmdHomesUnaccessibleItem2("   §4%s§2 (%s, %s, %s, %s)"),
	HomeCmdDontMove("§2Don't move! Or you will lose your payment and the teleport is canceled."),
	
	// Purchase
	PayByHandNotify("§2To complete this action, please pay %sx %s (%s)"),
	PayByHandNotify2("§a(Right click the requested item)"),
	;
	
	public String defaultVal;
	public static String language = null;
	public static HashMap<String, HashMap<Term, String>> translations = new HashMap<String, HashMap<Term, String>>();
	
	Term(String def)
	{
		defaultVal = def;
	}
	
	public String fname()
	{
		return super.toString();
	}
	
	@Override
	public String toString()
	{
		if (language == null)
			return defaultVal;
		
		HashMap<Term, String> terms = translations.get(language);
		if (terms == null)
			return defaultVal;
		
		String s = terms.get(this);
		
		return s == null || s.equals("") ? defaultVal : s;
	}
	
	public String toString(Object ... params)
	{
		return String.format(toString(), params);
	}
	
	public static void translate(String lang, Term term, String val)
	{
		HashMap<Term, String> terms = translations.get(lang);
		if (terms == null)
			translations.put(lang, terms = new HashMap<Term, String>());
		
		terms.put(term, val);
	}
}
