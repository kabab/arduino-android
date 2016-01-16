
#include <SoftwareSerial.h>
#include <AFMotor.h>

char c = 0;

void setup()  
{
  Serial.begin(9600);
  Serial1.begin(9600);
}

void loop() // run over and over
{
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
        c = Serial1.read();
        robot_speed(c);
        break;
      case 'd':
        Serial1.write(1);
        Serial1.write(120);
        Serial1.write(1);
        delay(10);
        break;
      case 'a':
        Serial1.write(2);
        Serial1.write(220);
        Serial1.write(2);
        delay(10);
        break;
    }

    
  }
}


void robot_left() {

}

void robot_right() {

}

void robot_forward() {

}

void robot_backward() {
}

void robot_speed(char speed) {
}
