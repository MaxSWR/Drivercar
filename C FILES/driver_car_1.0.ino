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
    const byte SPEED_BOUNDS = 10; // рамки изменения скорости
    const byte ANGLE_BOUNDS = 10; // рамки изменения угла поворота
    Servo servo;
    int currentSpeed;
    int currentAngle;
    bool currentForward;

  public:
    Driver() {
      currentAngle = 0;
      currentSpeed = 0;
      currentForward = true;
      servo.attach(rudderPort);
    }

    /**
    * Постепенное изменение скорости чтобы не сжечь драйвер
    */
    void smoothSpeedChange(byte speed) {
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

        currentSpeed = speed;
      }

      analogWrite(speedPort, currentSpeed);
    }

    /**
    * Двигаться
    * @param speed принадлежит отрезку [-100; 100]
    */
    void move(int speed) {
      byte newSpeed = speed == 0 ? 0 : (byte)((double)MAX_SPEED * ((double)abs(speed) / 100));// speed было в процентах, стало в единицах относительно MAX_SPEED

      if (currentSpeed - SPEED_BOUNDS > newSpeed || newSpeed > currentSpeed + SPEED_BOUNDS) {//если нужно менять скорость
        if (speed <= 0 && currentForward || speed >= 0 && !currentForward) {// если нужно поменять направление
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
      }

      analogWrite(speedPort, currentSpeed);
    }

    /**
    * Поворот рулем
    * @param angle принадлежит отрезку [-100; 100]
    */
    void rotate(int angle) {
      angle = angle == 0 ? 0 : (angle/abs(angle)) * (byte)((double)MAX_ROTATE_ANGLE * ((double)abs(angle) / 100));// speed было в процентах, стало в единицах относительно MAX_ROTATE_ANGLE

      if (currentAngle - ANGLE_BOUNDS > angle || angle > currentAngle + ANGLE_BOUNDS) {//если нужно менять угол
        currentAngle = angle;
      }

      servo.write(currentAngle);
    }

    void stop() {
      rotate(0);
      move(0);
    }
};

Driver driver;

void setup() {
  pinMode(speedPort, OUTPUT);
  pinMode(directionPort1, OUTPUT);
  pinMode(directionPort2, OUTPUT);
  BTSerial.begin(9600);
  driver.stop();
}

void loop() {
  if (BTSerial.available()) {
    switch (BTSerial.read()) {
      case 0://движение вперед
        driver.move(BTSerial.read());
        break;

       case 1://движение назад
       driver.move(-BTSerial.read());
        break;

      case 2://поворот направо
        driver.rotate(BTSerial.read());
        break;

      case 3://поворот налево
        driver.rotate(-BTSerial.read());
        break;
    }
  } else {
    driver.stop();
  }
 
}
