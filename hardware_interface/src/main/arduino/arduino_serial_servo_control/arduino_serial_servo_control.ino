#include <Servo.h>

const short THROTTLE_PIN_NR = 9;
const short STEERING_PIN_NR = 6; 
const short DEFAULT_DURATION_THROTTLE = 1500;
const short DEFAULT_DURATION_STEERING = 1500;
const long DEFAULT_SERIAL_COMMUNICATION_DATA_RATE = 115200;

Servo throttleServo;
Servo steeringServo;

void setup() {
  initializeServos();
  initializeSerial();
}

void initializeServos() {
  // attach servo instances to PWM pins
  throttleServo.attach(THROTTLE_PIN_NR);
  steeringServo.attach(STEERING_PIN_NR);

  // small delay for attaching to kick in
  delay(10);

  // set Servo's to default values
  throttleServo.writeMicroseconds(DEFAULT_DURATION_THROTTLE);
  steeringServo.writeMicroseconds(DEFAULT_DURATION_STEERING);
}

void initializeSerial() {
  Serial.begin(DEFAULT_SERIAL_COMMUNICATION_DATA_RATE);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  Serial.println("INITIALIZED COMMUNICATION");
}

void loop() {  
    // No standard loop code as all is done through Serial Events
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
    updateServo(throttleServo, value);
  } else if (cmd == "STE") {
    updateServo(steeringServo, value);
  } else if (cmd == "XXX") {
    shutdown();
  }
}

void updateServo(Servo servo, int value) {
  servo.writeMicroseconds(value);
  Serial.println(String(steeringServo.read()) + ";" + String(throttleServo.read()));
}

void shutdown() {
  Serial.println("SHUTTING DOWN...");

  throttleServo.writeMicroseconds(DEFAULT_DURATION_THROTTLE);
  steeringServo.writeMicroseconds(DEFAULT_DURATION_STEERING);

  delay(100);
  
  steeringServo.detach();
  throttleServo.detach();
  Serial.end();
}

