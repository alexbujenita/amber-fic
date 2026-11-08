# Amberfic

<p align="center">
  <img src="amberfic_logo.svg" alt="Logo Name" width="250" />
</p>


## **Highly work in progress! USE AT YOUR OWN RISK**

Amberfic is a utility to hash all files recursively in a folder
that the user passes as an argument.

Currently only the creation of the manifest is "working".

It will overwrite any existing manifest, and it can't yet cope
with OSs not being able to write to a FS.

## Motivation

Many times I've spotted corrupted photos in my usb sticks and had to check them all again.

I wanted a simple tool to create a manifest for those files and later on compare them
and tell me which are corrupted.

## Build

```shell
mvn verify
```

## MVP tracking

- Create a manifest deals with existing ones
- Deal with different FSs
- Have a verify command
- Have an update command

## TODO

A lot!

Once an MVP is working:

- Have a smarter detection, like moved or renamed files
- Having it replace corrupted files from a healthy source
- Have an interactive shell
- Have a GUI
