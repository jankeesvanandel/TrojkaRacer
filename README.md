# TrojkaRacer

## Project team
* Roy Braam (dead man's switch)
* Roy van Rijn (car electronics, image processing/preparation)
* Tim Blommerde (PID controller)
* Jan-Kees van Andel (AI)

## Hardware
* Raspberry Pi
* Raspberry Pi camera module
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


