#include <SoftwareSerial.h>
#include <Servo.h>
#include <stdlib.h>
#include <math.h>

int speedPort = 5;
int directionPort1 = 4;
int directionPort2 = 7;
int rudderPort = 6;
int bluetoothRxPort = 9;
int bluetoothTxPort = 10;
SoftwareSerial BTSerial(bluetoothRxPort, bluetoothTxPort);

class Driver {
  private:
    const byte MAX_SPEED = 255;
    const byte MAX_ROTATE_ANGLE = 70;
    const byte D_SPEED = 50; // приращение к скорости
    const byte DELAY = 30; // задержка при изменении скорости 
    const byte START_ANGLE = 78; //значение, полученное при настроке
    Servo servo;
    int currentSpeed;
    int currentAngle;
    bool currentForward;

  public:
    void init(Servo serv) {
      currentAngle = 0;
      currentSpeed = 0;
      digitalWrite(directionPort1, HIGH);
      digitalWrite(directionPort2, LOW);
      currentForward = true;
      servo = serv;
      rotate(50);
      move(0);
    }

    /**
    * Постепенное изменение скорости чтобы не сжечь драйвер
    */
    void smoothSpeedChange(int speed) {
      if (speed > currentSpeed) {
        for (int i = currentSpeed; i < speed; i += D_SPEED) {
          analogWrite(speedPort, i);
          delay(DELAY);
        }
      } else {
        for (int i = currentSpeed; i > speed; i -= D_SPEED) {
          analogWrite(speedPort, i);
          delay(DELAY);
        }
      }

      currentSpeed = speed;
    }

    /**
    * Двигаться
    * @param speed принадлежит отрезку [-100; 100]
    */
    void move(int speed) {
      if (speed < -100 || speed > 100) {
        return;
        }
        
      int newSpeed = speed == 0 ? 0 : (int)((double)MAX_SPEED * ((double)abs(speed) / 100));// speed было в процентах, стало в единицах относительно MAX_SPEED
      
      if (speed < 0 && currentForward || speed > 0 && !currentForward) {// если нужно поменять направление
        smoothSpeedChange(0);

        if (speed > 0) {
          digitalWrite(directionPort1, HIGH);
          digitalWrite(directionPort2, LOW);
          currentForward = true;
        } else {
          digitalWrite(directionPort2, HIGH);
          digitalWrite(directionPort1, LOW);
          currentForward = false;
        }
      }
        
      smoothSpeedChange(newSpeed);
      analogWrite(speedPort, currentSpeed);
    }

    /**
    * Поворот рулем
    * @param angle принадлежит отрезку [0; 100]
    */
    void rotate(int angle) {
      if (angle < 0 || angle > 100) {
        return;
        }
      angle = (START_ANGLE - MAX_ROTATE_ANGLE) + ((double)(2*MAX_ROTATE_ANGLE) * ((double)angle / 100));
      currentAngle = angle;
      servo.write(currentAngle);
    }

    void stop() {
      rotate(50);
      move(0);
    }	
};

Driver driver;
Servo servo;

void setup() {
  pinMode(speedPort, OUTPUT);
  pinMode(directionPort1, OUTPUT);
  pinMode(directionPort2, OUTPUT);
  BTSerial.begin(9600);
  Serial.begin(9600);
  servo.attach(rudderPort);
  driver.init(servo);
  Serial.begin(9600);
}

void loop() {
  if (BTSerial.available() == 2) {
    byte first = BTSerial.read();
    byte second = BTSerial.read();

    switch (first) {
      case 0://движение вперед
        driver.move(second);
        break;

       case 1://движение назад
       driver.move(-second);
        break;

      case 2://поворот
        driver.rotate(second);
        break;
		
  	  case 3://стоп
  		  driver.stop();
  		break;
    }
  }
}
