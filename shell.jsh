///usr/bin/env jbang --interactive "$0" "$@" ; exit $?
//DEPS com.example:jersc:0.0.1-SNAPSHOT

// N.B. "args" is a String[] with values from "$@".

import com.example.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// var file = SaveFile.from(java.nio.file.Paths.get("ER0000.sl2"))
// var game = file.getGames()[4]
// var matcher = new BytesMatcher(new byte[]{55,0,55,0,55,0})
// matcher.matches(game.getSaveData().getData())