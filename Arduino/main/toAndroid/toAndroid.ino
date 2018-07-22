#include <Usb.h>
#include <AndroidAccessory.h>
AndroidAccessory acc("BuaaITR",
"Demo",
"DemoKit Arduino Board",
"1.0",
"http://www.android.com",
"0000000012345678");

void setup(){
  Serial.begin(115200);
  Serial.print("\r\nStart");
  acc.powerOn();
}

void loop(){
byte msg[1024];
if (acc.isConnected()){
  while(Serial.available()>0){
    msg[0]=Serial.read();
    acc.write(msg,1);
  }
  int len = acc.read(msg, sizeof(msg), 1);
  if (len > 0){
  Serial.write(msg,len);
  }
}
delay(200);
}

