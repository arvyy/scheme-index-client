# Scheme index CLI client

A CLI Scheme index rest client implementation.

## Building

`mvn package` to build a jar file

`mvn package -Pnative` to build a native exectuable (requires graalvm native image)

`mvn test -Pintegration-test` to run tests that do rest calls

## Instalation

Build or download from github releases the binary suitable for your OS. 
Move executable to the directory of your choice, add said directory to path.

## Using

Run `scmindex -h` for information on its use.