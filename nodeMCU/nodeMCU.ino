//Definindo WI-FI
#include <ESP8266WiFi.h>
#include <SPI.h>
WiFiClient espClient;
//Parametros Wi-Fi
const char* ssid = ""; //Nome da rede
const char* password = ""; //Senha da Rede

//Definindo LCD
#include <Wire.h>  
#include <LiquidCrystal_I2C.h> 
LiquidCrystal_I2C lcd(0x27, 16, 2);

//Definindo RFID
#include <MFRC522.h>
#define RST_PIN D3 // RST-PIN for RC522 - RFID - SPI - Modul GPIO15 
#define SS_PIN  D4  // SDA-PIN for RC522 - RFID - SPI - Modul GPIO2
MFRC522 mfrc522(SS_PIN, RST_PIN);   // Create MFRC522 inst

//Definindo MQTT
#include <PubSubClient.h>
PubSubClient client(espClient);
//Parametros MQTT
const char* mqtt_server = "m13.cloudmqtt.com"; //server MQTT
const int mqtt_port = 13988; //Porta MQTT

//Biblioteca para tratar Json
#include <ArduinoJson.h>

int rele = D8;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);    // Initialize serial communications

  setup_wifi(); // Conecta ao WiFi

  client.setServer(mqtt_server, mqtt_port); // definindo server mqtt do client
  client.setCallback(callback);
   
  SPI.begin();           // Init SPI bus

  mfrc522.PCD_Init();    // Init MFRC522 lcd.init();

  Wire.begin(4, 5);

  pinMode(rele, OUTPUT);
  
  lcd.begin();
  lcd.backlight();
  lcd.setCursor(0,0);
  lcd.print("  Passe o Cartao");
}


//Configurando e Conectando Wi-Fi
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Conectando");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi conectado");
  Serial.println("Endereco IP : ");
  Serial.println(WiFi.localIP());
  }

//Conectando a fila do MQTT
void conectMqtt() {
  while (!client.connected()) {    
    Serial.print("ConectandoQTT ...");    
    
    //Parametros são nodeMCUClient, usuárioMQTT, senhaMQTT
    if (client.connect("ESP8266Client","romero","123")) {
      Serial.println("Conectado");
      //Inscrevendo-se no tópico retorno.
      client.subscribe("retornoNode");
    } else {
      Serial.print("Falha");      
      Serial.print(client.state());      
      Serial.println(" Tentando novamente em 5 segundos");      
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

//Tratando resposta do MQTT
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.println();

  String mensagem = "";
  //Conversão da mensagem recebidade de byte pra String
  for (int i = 0; i < length; i++) {
    mensagem += (char)payload[i];
  }
  Serial.println(mensagem);
  Serial.println();

  //converte mensagem para Char
  char jsonChar[mensagem.length()];
  mensagem.toCharArray(jsonChar, mensagem.length() + 1);
  
  //chamada ao Metodo que trata o Json
  jsonDecode(jsonChar);
//  jsonString = "";
  //Chamada ao método que controla o acesso
//  verificaAcesso(mensagem);
}

//Metodo para tratar o Json
void jsonDecode(char *json){
  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(json);
  int op = root["STATUS"];
  if (op == 0){
    String saldo = root["saldoDescontado"];
    digitalWrite(rele, HIGH);
    printLCD("Saldo: " + saldo);
    
    delay(3000);
    digitalWrite(rele, LOW);
    }
  else if (op == 1){
    printLCD("Usuario", "Inexistente");
    }
  else if (op == 2) {
    printLCD(" Saldo", "Insuficiente");
    }
}
//Enviando Mensagem ao MQTT
void sendMessage(String mfrc522){
  lcd.setCursor(2,0);
  lcd.print("Lendo o Cartao");
  String mensagem = "{\"RFID\":\""+mfrc522+"\"}";

  // Transformando a String em char para poder publicar no mqtt
  char charpub[mensagem.length() + 1];
  mensagem.toCharArray(charpub, mensagem.length()+1);
  Serial.print("Card ");
  Serial.print(mensagem);

  //Publicando na fila acesso o id do cartão lido
  client.publish("acessoNode", charpub);  
  
  Serial.println();
  lcd.setCursor(2, 1);
  lcd.print("Verificando...");
  return;
}

//Mostrar Mensagem na Tela
void printLCD(String mensagem){
    lcd.clear();
    lcd.setCursor(5,0);
    lcd.print("Sucesso!");
    lcd.setCursor(3,1);
    lcd.print(mensagem);
    delay(4000);
    lcd.clear();
    lcd.print("  Passe o Cartao");

}

void printLCD(String linha1, String linha2){
    lcd.clear();
    lcd.setCursor(5,0);
    lcd.print(linha1);
    lcd.setCursor(3,1);
    lcd.print(linha2);
    delay(4000);
    lcd.clear();
    lcd.print("  Passe o Cartao");

  }

// Metodo que Retorna o ID da Tag como String
String dump_byte_array(byte *buffer, byte bufferSize) {
  String uuid;
  for (byte i = 0; i < bufferSize; i++) {
    uuid = uuid + String(buffer[i], HEX);
  }
  return uuid;
}


void loop() { 
  //Verificando Status do ClientMQTT
  if (!client.connected()) {
    conectMqtt();
  }
  client.loop();
  // Look for new cards
  if ( ! mfrc522.PICC_IsNewCardPresent()) {
    delay(50);
    return;
  }
  // Select one of the cards
  if ( ! mfrc522.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }
  lcd.clear();
  String uid = dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
  sendMessage(uid);
  delay(4000);
  lcd.clear();
  lcd.print("  Passe o Cartao");
}

