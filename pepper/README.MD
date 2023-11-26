# Pepper (ACP 2)

## System Requirements (Windows)
- Android Studio 3.4
- Pepper SDK 7
- OpenJDK 11
- Android Gradle Plugin 3.4.0
- Gradle 5.1.1
- Windows 10 (Version 10.0.19045.2846 ?)
- Intel CPU

## System Requirements (MacOS)

I have not been able to launch the emulator successfully on MacOS yet.

## System Requirements (Linux)

- Android Studio 3.6.3
- Pepper SDK 7
- Oracle JRE 8 (See below)
- Android Gradle Plugin 3.6.3
- Gradle 5.6.4
- KVM (see below)
- Pop!_OS 22.04 LTS
- Intel CPU

### KVM

1. Install KVM

`sudo apt install qemu-kvm`

2. Add yourself to the kvm group

`sudo adduser yourusername kvm`

Relink the correct libraries:

3. Navigate to the API lib folder

`cd /home/$USER/.local/share/Softbank Robotics/RobotSDK/API 7/tools/lib`

4. Back up the old library

`mv libz.so.1 libz.so.1.bak`

5. Relink the System one

`ln -s /usr/lib/x86_64-linux-gnu/libz.so libz.so.1`

6. Restart Android Studio & start the Emulator

### JDK 8

Download Oracle JRE: https://www.java.com/en/download/manual.jsp

And then launch Android Studio 3.6.3 using this JRE:

`STUDIO_JDK=/usr/local/java/ ~/dev/android-studio-3.6.3/bin/studio.sh`

## Potential Issues

### App not installing to emulator

```
Device emulator-5574 disconnected, monitoring stopped.
Error while Installing APK
```

Reinstall API 7, or reset to default state (keep clean copy).

### Compilation Failure: Unrecognized Attribute Name 
If you have multiple installations of JDK, please ensure that you add the appropriate version's location using the project structure settings.
