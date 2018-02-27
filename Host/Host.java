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
  // MyCon.GetData();
 
 }

public Host()
 {
  CreateServer();
 	//BuildIntroInterface();
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



/**
*Bygger upp interfacet med hjälp av tre avdelningar, labels till vänster, textfält och knapp till höger och utskriftsruta i botten.
* Fyller man i ett nytt ,meddelande och klickar på knappen för att addera det så läggs det in i databasen via Inputdata metoden.
*Utskriftsfältet populeras av alla befintliga värden i databasen via GetData metoden.
 */
private void BuildInterface(String hostadress){

 JFrame f = new JFrame("Klient");
 f.setSize(500, 500);
 f.setLocation(300,200);
  JLabel Ladress = new JLabel("IP adress (delas till klienterna): ");
 JTextField Fadress= new JTextField(20);
 Fadress.setText(hostadress);
 JPanel labelPane = new JPanel(new GridLayout(1,2));
 labelPane.add(Ladress);
 labelPane.add(Fadress);
 f.add(labelPane, BorderLayout.NORTH);
 /* JTextArea textArea;
 JLabel Ldes = new JLabel("Skriv en kommentar nedan:");
 JButton button = new JButton("Fungerar");
 JButton green = new JButton("Fungerar");
 JButton yellow = new JButton("Halvt");
 JButton red = new JButton("Inte alls");
 textArea = new JTextArea(10, 40);
green.setBackground(Color.GREEN);
yellow.setBackground(Color.YELLOW);
red.setBackground(Color.RED);

 JPanel fieldPane = new JPanel(new GridLayout(0,1));
// fieldPane.add(button);
 fieldPane.add(Ldes);
fieldPane.add(textArea);
 JPanel bottPane = new JPanel(new GridLayout(1,3));


 bottPane.add(red);
 bottPane.add(yellow);
  bottPane.add(green);
 //f.add(labelPane, BorderLayout.NORTH);
 f.add(fieldPane, BorderLayout.SOUTH);
 f.add(bottPane, BorderLayout.CENTER); 

 button.addActionListener(new ActionListener() {

  @Override
  public void actionPerformed(ActionEvent e) {

    //SendMail(Fmailto.getText(),Fmailfrom.getText(),Fsubject.getText(),textArea.getText(),Fserver.getText(),Fnamn.getText(),Fpass.getText());
  }
});
*/
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
      WriteToTasks(id+": "+msg);

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

/**
 *Meddelar alla användare att en ny har anslutit sig.
 */
private void NewTaskJoin()
{
  WriteToTasks("Client connected from " + socket.getLocalAddress().getHostName());
  WriteToTasks(Activetasks());
} 
}