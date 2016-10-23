#include <Servo.h>

const short THROTTLE_PIN_NR = 9;
const short STEERING_PIN_NR = 6; 
const short DEFAULT_DURATION_THROTTLE = 1500;
const short DEFAULT_DURATION_STEERING = 1500;
const int DEFAULT_VALUE_STEP = 10;
const long DEFAULT_SERIAL_COMMUNICATION_DATA_RATE = 57600;

Servo throttleServo;
Servo steeringServo;

int valueStep;
int steeringValue;
int steeringTargetValue;
int throttleValue;
int throttleTargetValue;

void setup() {
  setupServos();
  setupSerial();

  throttleValue = DEFAULT_DURATION_THROTTLE;
  steeringValue = DEFAULT_DURATION_STEERING;
  throttleTargetValue = DEFAULT_DURATION_THROTTLE;
  steeringTargetValue = DEFAULT_DURATION_STEERING;

  valueStep = DEFAULT_VALUE_STEP;
}

void setupServos() {
  // attach servo instances to PWM pins
  throttleServo.attach(THROTTLE_PIN_NR);
  steeringServo.attach(STEERING_PIN_NR);

  // small delay for attaching to kick in
  delay(10);

  // set Servo's to default values
  throttleServo.writeMicroseconds(DEFAULT_DURATION_THROTTLE);
  steeringServo.writeMicroseconds(DEFAULT_DURATION_STEERING);
}

void setupSerial() {
  Serial.begin(DEFAULT_SERIAL_COMMUNICATION_DATA_RATE);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  Serial.println("INITIALIZED COMMUNICATION");
}

void loop() {
  throttleValue = checkAndUpdateValue(throttleServo, throttleValue, throttleTargetValue);
  steeringValue = checkAndUpdateValue(steeringServo, steeringValue, steeringTargetValue);
}

int checkAndUpdateValue(Servo servo, int currentValue, int targetValue) {
  if (currentValue != targetValue) {
    int newValue = currentValue;
    if (targetValue > currentValue) {
      newValue += min(valueStep, targetValue - currentValue);
    } else if (targetValue < currentValue) {
      newValue -= min(valueStep, currentValue - targetValue);
    }
    updateServo(servo, newValue);

    return newValue;
  } else {
    return currentValue;
  }
}

void serialEvent() {
  while (Serial.available()) {
    String msg = Serial.readStringUntil('\n');
    String cmd = msg.substring(0, 3);
    int value = msg.substring(3).toInt();

    executeCommand(cmd, value);
  }
}

void executeCommand(String cmd, int value) {
  if (cmd == "THR") {
    throttleTargetValue = value;
  } else if (cmd == "STE") {
    steeringTargetValue = value;
  } else if (cmd == "INI") {
    valueStep = value;
  }
}

void updateServo(Servo servo, int value) {
  servo.writeMicroseconds(value);
}

