
#include <SoftwareSerial.h> //Libreria modulo bluetooth
#include <AccelStepper.h> //Libreria para control del motor 

#define LEDon 9 //Pin Encendido
#define LEDfoto 7 //Pin intervalometro
#define dirPin 6 //Pin Direccion
#define stepPin 3 //Pin Pasos
#define enablePin 8 //Pin Enable
#define FCpin 11 //Pin para el final de carrera
#define motorInterfaceType 1 //Indicamos a la librería accelstepper que estamos usando un driver
#define STEPS 47950 //Pasos totales del recorrido

SoftwareSerial BT(2,4); //Pines (TX,RX) modulo bluetooth
AccelStepper stepper = AccelStepper(motorInterfaceType, stepPin, dirPin); //Creamos una instancia de AccelStepper


int val; //variable de control APP
int horas; //Variable Horas
int minutos; //Variable Minutos
int segundos;//Variable segundos
int fotos;//Numero de fotos intervalometro
long Parar; //Cantidad de pasos antes de tomar foto
long captura;
int Ttotal; //Tiempo total en segundos
int Trecorrido; //Duracion del recorrido sin intervalometro
long i=1; //Contador
int disparo;//
long int Vel = 0; // velocidad del slider, se expresa en pasos por segundo.
long int acc =0;

void setup(){
  
  Serial.begin(9600); // Inicializamos la comunicacion USB
  BT.begin(9600); //Inicializamos la comunicacion Bluetooth

  pinMode(FCpin, OUTPUT); //Configuracion pin   
  pinMode(enablePin, OUTPUT); //Configuracion pin
  digitalWrite(FCpin, HIGH); //Configuracion pin
  stepper.setMaxSpeed(3200); //Velocidad maxima del motor
  stepper.setAcceleration(3200);

  pinMode(LEDon, OUTPUT);//LED indicador encendido
  digitalWrite(LEDon, HIGH);//Encendemos indicador
  pinMode(LEDfoto, OUTPUT); //lED Fotos

  regresa();
}

void loop() {
  while (BT.available()>0)
    {
        val = BT.parseInt(); //Metemos cada valor de la APP en una variable
        horas = BT.parseInt(); //Valor Horas app
        minutos = BT.parseInt(); //Valor Minutos app
        segundos = BT.parseInt(); //Valor Segundos app
        fotos = BT.parseInt(); //Numero de fotos
        disparo = BT.parseInt();//Intervalo captura
        
        //Cuando lea el carácter fin de línea ('\n') quiere decir que ha finalizado el envío de los datos del recorrido y si se recibe un 1, hemos pulsado el boton Start y comienza el recorrido
        if (BT.read() == '\n'&& val == 1){
          Serial.println("ON");
          Trecorrido= ((horas*3600)+(minutos*60)+segundos)-((disparo/10)*fotos)-0.4*fotos; //Pasamos todo a segundos
          Ttotal= (Trecorrido);
          if (Ttotal < 15){
            Vel = 3200;
          }
          if (Ttotal > 15){
          Vel = STEPS/Ttotal; //Calculamos la velocidad en pasos por segundo
          }
          Parar = round(47500/fotos);//Calculamos numero de pasos por foto
          captura = Parar * i;//Definimos variable de seguimiento
          inicio(); 
        }
  
        if(val == 0){
          Serial.println("OFF");
        }
    }
}

/*Esta funcion hace que al enchufar el slider este se posicione en el incio y se detiene una vez activa el final de carrera*/
void regresa(){
    while(digitalRead(FCpin) == HIGH){
    digitalWrite(enablePin,LOW); 
    stepper.setSpeed(-2500);
    stepper.setAcceleration(-2500);
    stepper.runSpeed();
   }
   digitalWrite(enablePin,HIGH);
   stepper.setCurrentPosition(0); //Una vez llega al inicio regresamos el valor 0
   val=0;
   i=1
   ;
   horas,minutos,segundos = 0; //Reseteamos las variables
}

void inicio(){
  digitalWrite(enablePin,LOW);
  while(stepper.currentPosition() != STEPS){ // va a mover el motor hasta que se completen todos los pasos
    stepper.setSpeed(Vel); // seteamos la velocidad en funcion de los valores introducidos en la app
    stepper.setAcceleration(Vel);
    stepper.runSpeed(); // arrancamos el recorrido
    captura = Parar * i;//Variable de seguimiento
      if (stepper.currentPosition() == captura ){ //Cuando la posicion sea igual al numero de pasos por foto se detiene y ejecuta
        delay(200);
        takePhoto(); //secuencia de fotos NIKON
        delay(((disparo/10)*1000)+200);
        i++; //Variable de conteo de paradas
      }
    if (BT.read() == '0' ){ // si pulsamos el boton OFF se detiene el recorrido
      break;
    }
  }
  horas,minutos,segundos = 0; //Una vez acabado el recorrido las variables vuelven a ser 0
  regresa(); //Hacemos que el slider vuelva a la posicion inicial
}

void takePhoto() {
  int a;
  for (a = 0; a < 76; a++) {
    digitalWrite(LEDfoto, HIGH);
    delayMicroseconds(7);
    digitalWrite(LEDfoto, LOW);
    delayMicroseconds(7);
  }
  delay(27);
  delayMicroseconds(810);
  for (a = 0; a < 16; a++) {
    digitalWrite(LEDfoto, HIGH);
    delayMicroseconds(7);
    digitalWrite(LEDfoto, LOW);
    delayMicroseconds(7);
  }
  delayMicroseconds(1540);
  for (a = 0; a < 16; a++) {
  digitalWrite(LEDfoto, HIGH);
  delayMicroseconds(7);
  digitalWrite(LEDfoto, LOW);
  delayMicroseconds(7);
  }
  delayMicroseconds(3545);
  for (a = 0; a < 16; a++) {
  digitalWrite(LEDfoto, HIGH);
  delayMicroseconds(7);
  digitalWrite(LEDfoto, LOW);
  delayMicroseconds(7);
  }
}