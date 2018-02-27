import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.HashSet;

/**
*Skapar en uppkoppling till en mysqldatabas och hämtar och visar upp värden i form av kommentarer, man kan lägga in egna värden. 
  *@author  Joakim Flink
 */
public class Host {


/**
*Skapar en instans av MydatabaseConnection och kör GetData metoden för att hämta värden.
*@param args Startargument.
 */
  public static void main (String[] args) {


   Host MyCon=new Host();
 
 }

public Host()
 {
  CreateServer();
}

public void CreateServer(){
     int port= 2000;
 /*  try {
    port = Integer.parseInt(args[0]);
  } catch (RuntimeException ex) {
    port = 2000;
  }*/

  try (ServerSocket server = new ServerSocket(port)) {
   System.out.println("Server  ready for chatting");
   String hostadress = server.getInetAddress().getLocalHost().getHostName();
String hostip= server.getInetAddress().getLocalHost().getHostAddress();
  BuildInterface(hostip);
   while (true) {
    try {  

      Socket connection = server.accept();
      Thread task =  new ClientHandler(connection,hostadress);
      task.start();
    } catch (IOException ex) {}
  }
} catch (IOException ex) {
  System.err.println(ex);
  System.exit(0);
}
}


static JFrame f;
static JPanel Boxpanel;
/**
*Bygger upp interfacet med hjälp av tre avdelningar, labels till vänster, textfält och knapp till höger och utskriftsruta i botten.
* Fyller man i ett nytt ,meddelande och klickar på knappen för att addera det så läggs det in i databasen via Inputdata metoden.
*Utskriftsfältet populeras av alla befintliga värden i databasen via GetData metoden.
 */
private void BuildInterface(String hostadress){

  f = new JFrame("Klient");
 f.setSize(500, 500);
 f.setLocation(300,200);
  JLabel Ladress = new JLabel("IP adress (delas till klienterna): ");
 JTextField Fadress= new JTextField(20);
 Fadress.setText(hostadress);
 JPanel labelPane = new JPanel(new GridLayout(1,2));
 labelPane.add(Ladress);
 labelPane.add(Fadress);
  Boxpanel= new JPanel(new GridLayout(3,3));
 f.add(labelPane, BorderLayout.NORTH);
 f.setVisible(true);
}


}


class ClientHandler  extends Thread {
  Socket socket;
  ServerSocket server;
  BufferedReader reader;
  private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
  PrintWriter writer;
  int id=0;
  String Namn;
  static int activetasks=0; 
  static int idcount=0;
  public ClientHandler (Socket inputsocket, String Hadress) {
    try {
     activetasks++;
     idcount++;
     id+=idcount;
     socket=inputsocket;
     reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     writer = new PrintWriter(socket.getOutputStream(), true);
     writer.println("Welcome to the server at "+Hadress);

     writers.add(writer); 
   } catch (IOException e) {
   }
   NewTaskJoin();
 }



 @Override
 public void run() {

  String msg;
  try {
    while ((msg = reader.readLine()) != null) {
   String[] Splitted= msg.split(":");
   if (Splitted.length==2) {
     
    Namn= Splitted[1];
    CreateButton(Splitted[1],true);
   }
else{

    CreateButton(msg,false);
      WriteToTasks(Namn+": "+msg);
}

    }
  } catch (IOException ex) {
    System.err.println(ex);
  }
  finally {
    if (writer != null)
      killThread();
  }
}

/**
 *Tar bort användaren och tömmer anslutningen om clienten inte svarar. Tar bort en aktiv användaren från listan.
 */
private void killThread(){

  writers.remove(writer);
  activetasks--;
  WriteToTasks(Activetasks());

  try {writer.close();
   reader.close();
   socket.close();
 } catch (IOException e) {
 }
}

/**
 *Skriver ut antalet aktiva användare, kärs när någon har anslutits eller avanslutits.
 *
  */
private String Activetasks(){
  return "Now live: "+activetasks;
}

/**
 *Skriver meddelande till alla ansluta användare.
 */
private void WriteToTasks(String message){
 for (PrintWriter write : writers) {
  write.println(message);
}
}
 public JButton button;
private void CreateButton(String Medd,boolean first){
  if (first==true) {
       button = new JButton(Namn);
 Host.Boxpanel.add(button);
Host.f.add( Host.Boxpanel, BorderLayout.SOUTH);
  }
  else if (Medd=="yellow") {
 button.setBackground(Color.YELLOW);
  }
    else if (Medd=="green") {
 button.setBackground(Color.GREEN);
  }
    else if (Medd=="red") {
 button.setBackground(Color.RED);
  }
writer.println(Medd);
SwingUtilities.updateComponentTreeUI(Host.f);
}

/**
 *Meddelar alla användare att en ny har anslutit sig.
 */
private void NewTaskJoin()
{
  WriteToTasks("Client connected from " + socket.getLocalAddress().getHostName());
  WriteToTasks(Activetasks());
} 
}

/*
public void AddClient(){

   System.out.println("Cleint add");
   JButton button = new JButton("Namn");
 button.setBackground(Color.GREEN);
Boxpanel.add(button);
f.add( Boxpanel, BorderLayout.SOUTH);
SwingUtilities.updateComponentTreeUI(f);
}
*/