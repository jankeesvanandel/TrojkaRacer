# TrojkaRacer

## Project team
* Roy Braam (dead man's switch)
* Roy van Rijn (car electronics, image processing/preparation)
* Tim Blommerde (car electronics, hardware interface, runner, PID controller)
* Jan-Kees van Andel (AI)

## Hardware
* Raspberry Pi
* Raspberry Pi camera module
* Arduino (for PWM signals and possibly PID controllers)
* Dead Man's switch with electronic relays right on the engine power supply.

## Important guidelines
* Weight
* High FPS camera, low resolution, because of needed processing power
* The camera is the main input
* Java as the main programming language
* Lightweight architecture (no JEE or heavy frameworks).

## Architecture
![Module dependency graph](http://g.gravizo.com/g?
  digraph modules {
    PID Controller -> Servo
    PID Controller -> Engine
    PID Controller -> AI
    AI -> Processing
    Processing -> Camera
  }
)

It's a pull based system:

1. The PID Controller asks the AI for the next desired action.
2. If there is an action, it determines the best way to perform it and translate it to the correct output signal.
3. The AI asks the Processing module for environment information (track boundaries, red/green light, finish sign, etc).
4. The Processing unit reads images from the camera.

## Modules

### Processsing
Reads images from the camera, outputs them as a Java object containing the track information in a (sort of) readable format.

### AI
Reads the Processing info and determines the direction and speed of the car, based on the track (corners, start light, finish line).

### PID Controller
Reads the desired speed/direction from the AI module and translates it into efficient movements of the engine, using a PID algorithm.

### Web module
With the web module we should be able to configure the modules, start/stop it, etc.

### Hardware interface ###
Module for interfacing between the Raspberry Pi and the hardware of the race car (like the motor and the steering servo). The modules offers
an API for controlling the speed and the direction. The implementation of this interface does the actual work and communicates with the hardware
(through the Arduino).
