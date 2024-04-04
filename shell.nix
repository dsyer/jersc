with import <nixpkgs> { };
mkShell {
  buildInputs = [
    zlib.static
    gcc
    jbang
   ];
}

# libdir=`dirname $(echo $PATH | tr : '\n' | grep gcc | tail -1)`/lib
# chmod +w $libdir
# ln -s /nix/store/*zlib*static/lib/* $libdir
