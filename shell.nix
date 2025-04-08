with import <nixpkgs> { };
mkShell {
  buildInputs = [
    jbang
    lastpass-cli
    graalvm-ce
   ];
}
