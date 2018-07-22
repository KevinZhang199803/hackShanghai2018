int prvValue;

void setup() {
  Serial.begin(9600);
  pinMode(13, OUTPUT);
  
  prvValue = 0;
}

void loop() {
  if(Serial.available()){
    byte cmd = Serial.read();
    if(cmd == 0x02){
      digitalWrite(13, LOW);
    }else if(cmd == 0x01){
      digitalWrite(13, HIGH);
    }

  }
  
  int sensorValue = analogRead(A0) >> 4;
  byte dataToSent;
  if(prvValue != sensorValue){
    prvValue = sensorValue;
    
    if (prvValue==0x00){
      dataToSent = (byte)0x01;
    }else{
      dataToSent = (byte)prvValue;
    }
    
    Serial.write(dataToSent);
    delay(100);
  }

}
