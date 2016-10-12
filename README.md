# EyeOfAnubis

## Developers
* [Marcos Canales Mayo](https://github.com/MarcosCM) 
* [Jorge Cáncer Gil](https://github.com/jorcox)
* [Jorge Martínez Lascorz](https://github.com/JorgeCoke)

## Description
The application uses facial recognition technology to track the eyes and the face orientation of the user. Once the face is tracked it is possible to use "eye-commands" to perform actions. Joining Android and Arduino via BLE (Bluetooth Low Energy) we can create a physical interface that allows other software to use that "eye-commands" in order to perform actions (e.g. wheelchairs, drones, robots, boats, vehicles, mouse, etc...).
For this version we have code four possible commands (i.e. looking up, looking down, looking rigth and looking left). 

As a demo, we have built a differential traction-based "wheelchair" with two engines to try the performance of the eye tracker.

**Video demo:** https://www.youtube.com/watch?v=_5MnEo507lk

## Requirements
* Android version >= 4.1.x
* Qualcomm Snapdragon processor

## Hardware
* Arduino UNO
* BLE Blend Micro
* L293D controller
* Two 4,5V - 9V engines
