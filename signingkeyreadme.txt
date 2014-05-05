How to Setup Signing Key
========================

Place the Sciaps_Keystore.jks somewhere. A good place to keep it might be in your user's home
directory.

create a file ~/.gradle/gradle.properties with contents:

RELEASE_STORE_FILE=/home/paul/Sciaps_Keystore.jks
RELEASE_STORE_PASSWORD=rustyrules
RELEASE_KEY_ALIAS=LIBZ
RELEASE_KEY_PASSWORD=rustyrules

DEBUG_STORE_FILE=/home/paul/Sciaps_Keystore.jks
DEBUG_STORE_PASSWORD=rustyrules
DEBUG_KEY_ALIAS=debug
DEBUG_KEY_PASSWORD=rustyrules


**Note:** you should edit the STORE_FILE location to reflect where the Sciaps_Keystore.jks is located
on your machine.