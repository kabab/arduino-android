#include <SoftwareSerial.h>
#include <Servo.h>

#define M1  1  
#define M2  2
#define M3  3
#define M4  4

#define FORWARD 1
#define BACKWARD 2
#define BRAKE 3
#define RELEASE 4

#define MOTORLATCH 12
#define MOTORCLK 4
#define MOTORENABLE 7
#define MOTORDATA 8

#define MOTOR1_PWM 11
#define MOTOR2_PWM 3
#define MOTOR3_PWM 6
#define MOTOR4_PWM 5
#define SERVO1_PWM 10
#define SERVO2_PWM 9

#define MOTOR1_A 2
#define MOTOR1_B 3
#define MOTOR2_A 1
#define MOTOR2_B 4
#define MOTOR3_A 5
#define MOTOR3_B 7
#define MOTOR4_A 0
#define MOTOR4_B 6

const int trigPin = 38;
const int echoPin = 39;
int Sp = 230;
char c = 's';

void setup() {
  Serial.begin(9600);
}

void loop() {
  if(Serial1.available()) {
    c = Serial1.read();
    switch(c) {
    case 'f':
      robot_forward();
      break;
    case 'l':
      robot_left();
      break;
    case 'r':
      robot_right();
      break;
    case 'b':
      robot_backward();
      break;
    case 's':
      robot_stop();
      break;
    case 't':
      Serial.println("Sound : " + String(getDistance())  + " | Infra : " + String(getDistance2()) );
      break;
    }
  }
}

long getDistance2 () {
  long ans = 4800/analogRead(15);
  return ans; 
}

long getDistance ()
{
  long duration, cm;
  pinMode(trigPin, OUTPUT);
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  pinMode(echoPin, INPUT);
  duration = pulseIn(echoPin, HIGH);
  cm = microsecondsToCentimeters(duration);
  return cm;
}


long microsecondsToInches(long microseconds)

{
  return microseconds / 74 / 2;
}

long microsecondsToCentimeters(long microseconds)

{
  return microseconds / 29 / 2;
}

void robot_forward() {
  motor(M1, FORWARD, 255);
  motor(M2, FORWARD, 255);
  motor(M3, FORWARD, 255);
  motor(M4, FORWARD, 255);
}

void robot_backward() {
  motor(M1, BACKWARD, 255);
  motor(M2, BACKWARD, 255);
  motor(M3, BACKWARD, 255);
  motor(M4, BACKWARD, 255);

} 

void robot_left() {
  motor(M1, FORWARD, Sp);
  motor(M2, BACKWARD, Sp);
  motor(M3, FORWARD, Sp);
  motor(M4, BACKWARD, Sp);
}

void robot_right() {
  motor(M1, BACKWARD, Sp);
  motor(M2, FORWARD, Sp);
  motor(M3, BACKWARD, Sp);
  motor(M4, FORWARD, Sp);
}

void robot_stop() {
  motor(M1, RELEASE, 255);
  motor(M2, RELEASE, 255);
  motor(M3, RELEASE, 255);
  motor(M4, RELEASE, 255);
}

void motor(int nMotor, int command, int speed)

{
  int motorA, motorB;
  if (nMotor >= 1 && nMotor <= 4)
  {
    switch (nMotor)
    {

    case 1:

      motorA   = MOTOR1_A;

      motorB   = MOTOR1_B;

      break;

    case 2:

      motorA   = MOTOR2_A;

      motorB   = MOTOR2_B;

      break;

    case 3:

      motorA   = MOTOR3_A;

      motorB   = MOTOR3_B;

      break;

    case 4:

      motorA   = MOTOR4_A;

      motorB   = MOTOR4_B;

      break;

    default:

      break;

    }



    switch (command)

    {

    case FORWARD:

      motor_output (motorA, HIGH, speed);

      motor_output (motorB, LOW, -1);     // -1: no PWM set

      break;

    case BACKWARD:

      motor_output (motorA, LOW, speed);

      motor_output (motorB, HIGH, -1);    // -1: no PWM set

      break;

    case BRAKE:

      // The AdaFruit library didn't implement a brake.

      // The L293D motor driver ic doesn't have a good

      // brake anyway.

      // It uses transistors inside, and not mosfets.

      // Some use a software break, by using a short

      // reverse voltage.

      // This brake will try to brake, by enabling

      // the output and by pulling both outputs to ground.

      // But it isn't a good break.

      motor_output (motorA, LOW, 255); // 255: fully on.

      motor_output (motorB, LOW, -1);  // -1: no PWM set

      break;

    case RELEASE:

      motor_output (motorA, LOW, 0);  // 0: output floating.

      motor_output (motorB, LOW, -1); // -1: no PWM set

      break;

    default:

      break;

    }

  }

}



void motor_output (int output, int high_low, int speed)

{

  int motorPWM;



  switch (output)

  {

  case MOTOR1_A:

  case MOTOR1_B:

    motorPWM = MOTOR1_PWM;

    break;

  case MOTOR2_A:

  case MOTOR2_B:

    motorPWM = MOTOR2_PWM;

    break;

  case MOTOR3_A:

  case MOTOR3_B:

    motorPWM = MOTOR3_PWM;

    break;

  case MOTOR4_A:

  case MOTOR4_B:

    motorPWM = MOTOR4_PWM;

    break;

  default:

    // Use speed as error flag, -3333 = invalid output.

    speed = -3333;

    break;

  }



  if (speed != -3333)

  {

    // Set the direction with the shift register

    // on the MotorShield, even if the speed = -1.

    // In that case the direction will be set, but

    // not the PWM.

    shiftWrite(output, high_low);



    // set PWM only if it is valid

    if (speed >= 0 && speed <= 255)    

    {

      analogWrite(motorPWM, speed);

    }

  }

}





void shiftWrite(int output, int high_low)

{

  static int latch_copy;

  static int shift_register_initialized = false;



  // Do the initialization on the fly,

  // at the first time it is used.

  if (!shift_register_initialized)

  {

    // Set pins for shift register to output

    pinMode(MOTORLATCH, OUTPUT);

    pinMode(MOTORENABLE, OUTPUT);

    pinMode(MOTORDATA, OUTPUT);

    pinMode(MOTORCLK, OUTPUT);



    // Set pins for shift register to default value (low);

    digitalWrite(MOTORDATA, LOW);

    digitalWrite(MOTORLATCH, LOW);

    digitalWrite(MOTORCLK, LOW);

    // Enable the shift register, set Enable pin Low.

    digitalWrite(MOTORENABLE, LOW);



    // start with all outputs (of the shift register) low

    latch_copy = 0;



    shift_register_initialized = true;

  }



  // The defines HIGH and LOW are 1 and 0.

  // So this is valid.

  bitWrite(latch_copy, output, high_low);



  // Use the default Arduino 'shiftOut()' function to

  // shift the bits with the MOTORCLK as clock pulse.

  // The 74HC595 shiftregister wants the MSB first.

  // After that, generate a latch pulse with MOTORLATCH.

  shiftOut(MOTORDATA, MOTORCLK, MSBFIRST, latch_copy);

  delayMicroseconds(5);    // For safety, not really needed.

  digitalWrite(MOTORLATCH, HIGH);

  delayMicroseconds(5);    // For safety, not really needed.

  digitalWrite(MOTORLATCH, LOW);

}
