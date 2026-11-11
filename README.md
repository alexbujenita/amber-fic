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

## Build native
You need GraalVM.

```shell
./mvnw -Pnative package
```

Or even better use the option below so it targets the machine CPU.
Otherwise the binary might not use SHA512 CPU instructions, locally a hash of a 300MB file
takes 0.70s and with `-march=native` it drops down to 0.22s.

```shell
NATIVE_IMAGE_OPTIONS=-march=native ./mvnw -Pnative package
```

PS:

Use this on Windows

```powershell
$env:NATIVE_IMAGE_OPTIONS='-march=native'; .\mvnw.cmd -Pnative package
```

and remove the env var if not needed anymore

```powershell
Remove-Item Env:NATIVE_IMAGE_OPTIONS
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
