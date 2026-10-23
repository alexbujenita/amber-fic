# Amber FIC

**Highly work in progress!**

Amber file integrity checker.

A utility to minimise losing files by hashing them and then comparing them at a later
date with a checksum.

## Motivation

It happened quite a few times for me to have multiple backups of my photos, on a computer, on two HDDs and 
on two USB sticks, looking at the USB sticks some photos would look corrupt.

Wanted to have a more proactive approach, instead of going through all photos, hash them all and check from time to time.

## Build

```shell
mvn verify
```

## TODO

A lot!

Once an MVP is working:

- Have a smarter detection, like moved or renamed files
- Having it replace corrupted files from a healthy source
- Have an interactive shell
- Have a GUI
