# CrisTreeCapitator

## Tested on 1.21!
The latest build has been tested and works for 1.21.*, 1.20.*, 1.19.*, 1.18.*, 1.17.*, 1.16.*, 1.15.*, 1.14.* and 1.13.*. I recommend using the latest build always in servers running on those Minecraft versions. I'm also still around so please feel free to leave here any issues you may encounter or your suggestions if you want to, I'll read them.

## Features
Please note that only the latest build supports ALL of the following features.

* Let users destroy entire trees by breaking only 1 log. Even the biggest tree will fall!
* Works on nether trees (If server is +1.16).
* Auto-update. You will get a notification if a new update is out and you don't need to come back here to download the plugin again when new features come out.
* It also destroys all leaves around.
* Auto-replant. Replanted saplings can also be automatically protected so they cannot be broken until they grow a tree.
* Switch on/off if an axe is required and/or damaged.
* VIP mode. Use it to restrict access to the benefits of this plugin, for premium users for example.
* Each player can toggle on/off the plugin for themselves. You can also configure if you want a message to remind players of their current setting when they log in.
* Players are not able to break logs in protected areas (by WorldGuard or other protection plugins using WorldGuard as a dependency).
* Axes are damaged accordingly.
* Switch on/off if axes should be prevented from being broken. So that you can repair that nice axe with Mending or an anvil and never worry about it being broken. (only works when breaking logs with it, breaking anything else will destroy the axe anyway)
* Switch on/off if tree leaves should be ignored by the plugin. Enabling it will make the plugin take down only connected logs, reducing the lag and balancing a little bit.
* Crouch mode. Use true in this configuration so that crouching players not trigger this plugin. Use inverted to make standing players not trigger this plugin, or false to make crouching not change how this plugin works.
* Customize what are trees! Configure any block to be treated as tree logs or as tree leaves for this plugin. Scroll down to see more specific steps on this page.
* Ask for new features by DM or by commenting here! I'll try to consider and answer every suggestion.

## Commands/Configuration
* /tc help: Lists all commands.
* /tc toggle: Toggles the plugin for you. Just in case you made your home using lots of logs.
* /tc update: Checks for new updates, and updates if able.
* /tc reload: Reload config.yml changes.
* /tc setLimit <number>: Sets the limit of blocks this plugin can destroy at once. (-1 for unbounded)
* /tc setReplant <true/false>: Sets if trees should be replanted. True by default.
* /tc setInvincibleReplant <true/false>: Sets if replanted saplings should be unbreakable by survival players. False by default.
* /tc setAxeNeeded <true/false>: Sets if an axe is required for the plugin to work. True by default
* /tc setDamageAxe <true/false>: Sets if the axe used is damage (only takes place if an axe is needed). True by default. 
* /tc setBreakAxe <true/false>: Sets if the axe used can be broken (only takes place if an axe is needed and damaged). False by default. 
* /tc setVipMode <true/false>: Sets if vip mode is on. False by default.
* /tc setNetherTrees <true/false>: Sets if the plugin works on the new nether "trees". False by default.
* /tc setStartActivated <true/false>: Sets if this plugin is activated for players when they enter the server. If false, players will need to use /tc toggle to activate it for themselves. True by default.
* /tc setJoinMsg <true/false>: Enables or disables the join message that remind players about /tc toggle. True by default.
* /tc setIgnoreLeaves <true/false>: Makes the plugin ignore or not the leaves aroung logs. False by default.
* /tc setCrouchPrevention <true/false/inverted>: Defines if crouching allows players to break logs individually. If set to inverted, players must crouch to break multiple logs with this plugin. False by default.

You may also use /treecap or /treecapitator as valid aliases for the /tc command.

## Permission nodes
* cristreecapitator.user: Always required to take down trees fast.
* cristreecapitator.admin: Required to change options by commands.
* cristreecapitator.vip: Required to take down trees fast when Vip Mode is enabled.

## How to Install
* Download the latest version on https://dev.bukkit.org/projects/cristichis-tree-capitator/files.
* Place your downloaded .jar file into YourServerDirectory/plugins/.
* Start or reload server.
* Then you may configure the plugin just the way you want, either using commands OR editing the config file and then typing /tc reload in the in-game chat (with cristreecapitator.admin permission/OP) or in the server console.

## How to use VIP mode
VIP Mode enables an easy switch for the plugin. If VIP Mode is enabled regular users won't be able to use this plugin by default because they will also need an additional permission.
* Enable vip mode (/tc setVipMode true).
* Give 'cristreecapitator.vip' permission node to VIP players.
* It's already done! VIP players will be the only ones allowed to cut down trees faster! Don't worry, average players can still get wood, but the vanilla way.

## How to add blocks as logs or leaves
You can configure other blocks to be detected as if they were logs or leaves, like the stripped logs or blocks from mods, or any block you want.
* Make sure you are using v6.4.0 or higher of this plugin.
* Check the name of the blocks in this link https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html.
* Go to the folder YourServerDirectory/plugins/CrisTreeCapitator where the configuration file is and open extra_logs.json or extra_leaves.json, and edit the files to include the blocks you want (as named in the previously mentioned link). Be careful to follow the format.
* Reload the server OR type /tc reload to apply the changes.

## Keep it always up to date!
During startup you will receive a notification in the chat and the server console if the plugin needs to be updated. With a simple command you will be able to check new updates and update the plugin in-game.
I always recommend to keep the plugin up to date to make sure that you are benefiting from new features, general improvements and any bug fixes.

## Compatibility with older Minecraft versions.
If your server is running a Minecraft version older than 1.13, please click here to download v3.0.1 instead. It won't have all the advantages and performance of the latest version so please consider updating your server to at least Minecraft 1.13.

## Need help?
Please feel free to contact me through DM or by commenting in this page. I do not have much free time to answer, but I will be sure to answer you as soon as possible. I will be happy to help on any issues that you are having, and consider every feature request that would make the plugin better for your server. Do not hesitate to insist with new messages if the matter is urgent for you.

## Future plans
My intention for the future of the plugin is to keep providing updates for new Minecraft versions, especially if there are any significant changes to the game like new trees or new features that would help this plugin's implementation. Additionally, I will provide support for any issues or bugs encountered by players no a best effort basis, and I will be open for suggestions as well. Please, feel free to let me know your feedback, good or bad.


Thank you for reading, and happy minecrafting!
