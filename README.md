# JamSketch

JamSketch is a jam session system that enables the user to enjoy improvisation just by drawing curves that represent the overall shape of melodies.

## Requirements

* Java Runtime Environment(JRE) Version 21

## Installation

### From Source Code

```
git clone https://github.com/kitaharalab/JamSketch.git
```

#### Usage

```
gradlew runApp
```
or
```
gradlew run --args="jp.kthrlab.jamsketch.view.JamSketch" 
```

### From Executable File


* Download the latest release

    Go to the [Releases Page](https://github.com/kitaharalab/JamSketch/releases) and download JamSketch-executable.zip.

* Unzip the downloaded file

* Run the executable
  * Windows
    * Run JamSketch.exe
  * JRE
    ```
    java -cp "JamSketch.jar;resources/;lib/*" jp.kthrlab.jamsketch.view.JamSketch "jp.kthrlab.jamsketch.view.JamSketch"
    ```

## Settings

####  Networked JamSketch

* Server
```json:config.json
  "general" : {
    ... (code omitted) ...
    "mode": "server",
    "host": "XXX.XXX.XXX.XXX",  // Hostname or IP address of the server
    "port": 8181,
    ... (code omitted) ...
  },
```

* Client
```json:config.json
  "general" : {
    ... (code omitted) ...
    "mode": "client",
    "host": "XXX.XXX.XXX.XXX",  // Hostname or IP address of the server
    "port": 8181,
    ... (code omitted) ...
  },
```

## References

See for details of the system:
* Tetsuro Kitahara, Sergio Giraldo, and Rafael Ramírez: "JamSketch: A Drawing-based Real-time Evolutionary Improvisation Support System", Proceedings of the 2017 International Conference on New Interfaces for Musial Expression (NIME 2017), pp.506--507, May 2017.
* Tetsuro Kitahara, Sergio Giraldo and Rafael Ramírez: "JamSketch: Improvisation Support System with GA-based Melody Creation from User's Drawing", Proceedings of the 13th International Simposium on Computer Music Multidisiplinary Research (CMMR 2017), Sept. 2017. 

## Authors

* Tetsuro Kitahara (Nihon University, Japan)  
kitahara@kthrlab.jp  
http://www.kthrlab.jp/  
