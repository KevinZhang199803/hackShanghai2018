#define trigPin 2
#define echoPin 3
#include <stdlib.h>
#include <math.h>
#include <SoftwareSerial.h>

float previous_dis = -1;
long int count = 0;

void setup(){
	Serial.begin(9600);
	pinMode(trigPin, OUTPUT);
	pinMode(echoPin, INPUT);
	while (!Serial){}
}

void loop(){
	int result;
	float duration, distance;

	digitalWrite(trigPin, LOW);
	delayMicroseconds(2); // Added this line
	digitalWrite(trigPin, HIGH);
	//delayMicroseconds(1000); - Removed this line
	delayMicroseconds(10); // Added this line
	digitalWrite(trigPin, LOW);
	duration = pulseIn(echoPin, HIGH);
	distance = (duration/2) / 29.1;
  if (distance > 0 and distance < 500){
	  Serial.println(distance);
    if (previous_dis == -1){
      previous_dis = distance;
      count = 1;
      Serial.println(0);
    }else{
      if (previous_dis - distance > 100){
        Serial.println(1);
      }else{
        previous_dis = (previous_dis*count + distance) / (count+1);
        count++;
        Serial.println(0);
      }
    }
  }
  
  
  delay(200);

}
