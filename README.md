# (Java) Elden Ring Save Copier

This project is a library and command line application (written in Java) for reading and copying Elden Ring save files. It is useful for copying characters between save files or slots, or for duplicating a character before you go and do something dangerous. The save file is a binary file that contains your character slots, up to a maximum of 10. The application can read and write these files, and it can also copy characters between slots or files.

Locate your save file and (optionally) copy it to an empty directory. The save file from the game is `ER0000.sl2` typically located in a folder called `AppData/Roaming/EldenRing/<steamid>`. On Windows you can usually find it at `C:\Users\<username>/` and on Steam Deck it will be in a Proton virtual drive `/home/deck/.steam/steam/steamapps/compatdata/1245620/pfx/drive_c/users/steamuser/`. The `steamid` is a 17-digit number that corresponds to your Steam account - you can see it in your profile details in the Steam web UI at https://store.steampowered.com/account/ - and `1245620` is a unique identifier for Elden Ring. The `ER0000.sl2` file is a binary file that contains your character slots.

You don't have to run the application on the same machine as where you play the game. Steam Decks are fine for running Java but if you want to develop new features it might be easier to copy the file over. What I do with my Steam Deck is [enable SSH](https://shendrick.net/Gaming/2022/05/30/sshonsteamdeck.html) and then copy the save file to my laptop with `scp`. You can also use a USB stick to copy the file between the Steam Deck and your computer.

You can also download other player's save files from the internet and copy their characters into your save file (e.g. at [Nexusmods](https://www.nexusmods.com/eldenring/mods/categories/10/)). This is useful for trying out different builds or for recovering a lost character, but be careful if you don't know the other player because there might (allegedly) be stuff in other people's save files that could get you banned from multiplayer. You can also use the application to copy your characters between different computers or operating systems, but if that's all you need Steam Cloud works fine for me.

## Installation

Build the project with `./mvnw package`. You can run it with `java -jar target/*-exec.jar` or compile it to a native image with GraalVM using `native-image -o jersc -jar target/*-exec.jar`. The native image will be faster to start up and use less memory. The tests will be skipped unless there is a valid save file in the current directory.

## Usage

Basic usage is to scan a file to inspect the character slots - just exit with `Ctrl-C` or enter "n" when prompted:

```bash
$ ./jersc 
Input file (ER0000.sl2)? 
SaveFile: [id=76561197960267366, valid=true]
Slot=0: Foo=[Level=99,Played=1534s]
Do you want to copy a game from the file (Y/n)? n
```

You can also copy a game slot, duplicating it into an existing slot. Example (assuming `ER0000.sl2.out` does not exist):

```bash
$ ./jersc 
Input file (ER0000.sl2)? 
SaveFile: [id=76561197960267366, valid=true]
Slot=0: Foo=[Level=99,Played=1534s]
Do you want to copy a game from the file (Y/n)? 
Output file (ER0000.sl2.out)? 
Which slot do you want to copy from [0]? 
Writing to:
SaveFile: [id=76561197960267366, valid=true]
Slot=0: Foo=[Level=99,Played=1534s]
Which slot do you want to copy to [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]? 1
New name for character (777)? Bar
SaveFile: [id=76561197960267366, valid=true]
Slot=0: Foo=[Level=99,Played=1534s]
Slot=1: Bar=[Level=99,Played=1534s]
Continue (Y/n)?
```
You can also copy a game slot from one file to another. The output file will be from the steam id that you intend to play with. The input file could be from someone else, and you can adopt their character including all the stats and inventory.  Here we copy a game from a file, updating the player ID and saving it into an empty slot in an existing save file (assuming `ER0000.sl2.out` already exists):

```bash
$ ./jersc 
Input file (ER0000.sl2)? 
SaveFile: [id=76561197960267366, valid=true]
Slot=0: Foo=[Level=99,Played=1534s]
Do you want to copy a game from the file (Y/n)? 
Output file (ER0000.sl2.out)? 
File ER0000.sl2.out already exists.
Do you want to overwrite this file (Y/n)? 
Which slot do you want to copy from [0]? 
Writing to:
SaveFile: [id=76561199114987181, valid=true]
Slot=0: BigFish=[Level=197,Played=646829s]
Slot=1: RedFish=[Level=138,Played=165214s]
Slot=2: LittleFish=[Level=184,Played=573239s]
Which slot do you want to copy to [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]? 3
New name for character (Foo)? Bar
SaveFile: [id=76561199114987181, valid=true]
Slot=0: BigFish=[Level=197,Played=646829s]
Slot=1: RedFish=[Level=138,Played=165214s]
Slot=2: LittleFish=[Level=184,Played=573239s]
Slot=3: Bar=[Level=99,Played=1534s]
Continue (Y/n)? 
```