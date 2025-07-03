Project description page + downloads: https://dev.bukkit.org/projects/claimclassifier

[Commands and permissions reference](src/main/resources/plugin.yml)

# ClaimClassifier
This plugin is a GriefPrevention addon that extends the functionality of player claims.

## Features/Commands
- Adds a `/nameclaim` command that lets players assign names to individual claims.
- Sorts the claims listed in the `/claimslist` command.
- Shows the assigned name of the claim next to each claim. 
- Adds clickable locations within the claim list output, allows players teleport to their claims with ease!
    - This feature currently uses spigot chat components which may not be available in other bukkit implementations such as Paper.
- Adds a confirmation message to the dangerous `/abandonclaim` command.
- Adds a `/trustedclaimslist` command to list claims you are trusted to.
- Adds a `/claimexpire` command for admins to check the expiration date of a claim, and manually extend the claim's expiration time if necessary.

## Installation
To install this plugin, simply drop it in your server's /plugins directory. On first load, the plugin will create a configuration file in the /plugins/ClaimClassifier directory. Within this file you can enable/disable features.

# Support

RoboMWM does not currently maintain this plugin. Feel free to reach out to RoboMWM or Jikoo if you'd like access to maintain this plugin and publish to BukkitDev.

Bug reports can be [submitted here](../../issues).
