 // ~170 ~760

#define pin A0

void setup () {
        Serial.begin (9600);
        pinMode (pin, INPUT);
}

void loop () {
        uint16_t value = analogRead (pin);
        uint16_t range = get_gp2d12 (value);
        //Serial.println (value);
        Serial.print (range);
        Serial.println (" mm");
        //Serial.println ();
        if (range < 200){
          Serial.println(1);
        }else{
          Serial.println(0);
        }
        delay (500);
}

//return distance (mm)
uint16_t get_gp2d12 (uint16_t value) {
        if (value < 30)
                value = 30;
        return ((67870.0 / (value - 3.0)) - 40.0);
}

/******** end code ********/
