# Minigames plugin

JAR DOWNLOAD IN RELEASES - https://github.com/CoOwner/Minigames/releases/tag/LATEST

(Ugly code) this is a public spigot plugin, decided to post the 1.7.10 version SRC. Have fun!

The main reason I am posting this is cause the owner doesn't care about this anymore.

**Mini Game Events**
Dependencies are required to be installed for full features and full accessibility


Use of Waterdrop requires a 1.8.8 (1.7 in this case) spigot instead of a normal 1.8 spigot

Mini Game Events is supposed to give a good example of what Invaded Lands events are.
No this is not a copy, but it is supposed to look somewhat like it.

READ:
Make sure any arena is located in a different world to every other arena. For example, if my sumo arena was located in the world SUMO, and red rover was being made, make sure it's in a different world. Also make sure they don't exist in your spawn world to stop unwanted errors.

  The aim of this plugin is to create cool and unique game-modes such as;
Sumo
Red Rover (RR)
Brackets
Last Man Standing (LMS)
Race of Death (RoD)
Maze
Water Drop (WD)
Spleef

Placeholders for winner commands: {user}, {uuid}.

Commands;

/event
/event join (Perm: events.join)
/event spectate (Perm: events.spectate)
/eventstats
/event leave
/event host (Perms: events.host.<event>, events.bypass_cooldown)
/event forceend (Perm: events.admin)
/event kick <player> (Perm: events.admin)
/eventsconfig [event] [setup1] [setup2] (Perm events.admin)

Sumo;

Sumo is setup by setting a spectate area, 2 player positions (so they can 1v1 each other) and a minY loc. The minY is by setting the lowest Y co-ordinate (basically water underneath the players), and a Scoreboard name (Color Codes not supported). A Winner Command to give the winner a prize.

RedRover;

RedRover is setup by setting a spectate area, 2 player positions (1 is the runners, the other is the killer.). 2 Block positions. [​IMG]
Stand on these positions (Depending on where you want them) and when you start the event, sometimes blocks will spawn in set locations.
There are regions needed for some features, the killer is not allowed to leave the middle so you need to setup a region where the killer is allowed to be, then call it middle. There are regions players are allowed to be in at times. Select a area in the red side and call it red, Do the same with blue and call it blue. Then in the safe zone for red, Select it and call it safered, same with safeblue.
Scoreboard name and a Winner Command

Brackets;

Brackets is setup by setting a spectate area, 2 player positions (so they can 1v1 each other), Scoreboard name and a Winner Command.
You can also setup Node the same way with this too.


LMS;

LMS is setup by setting a spectate area, 2 player positions (so they can 1v1 each other), Scoreboard name and a Winner Command.

Maze;

Maze is setup by setting a spectate area, 2 player positions (so they can 1v1 each other), then proceed to setup a 1 block region called finish. This will trigger the player that stepped on it to win the event. (Multiple Arena Support is allowed but not tested), Scoreboard name and a Winner Command.

WaterDrop;

To run the plugin, you must install these plugins,

[WorldEdit, WorldGuard, PlaceHolderAPI].


MainSpawn;

MainSpawn is setup by going to the spawn of the server and running the command /eventsconfig setMainSpawn.

Authors:
- Cuteunderwear
- StarZorrow

                                 ____  ____
                                /    \/    \
			       |	    |
                                \          /
				 \        /
				  \______/


