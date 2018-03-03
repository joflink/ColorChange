import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

/**
*Skapar client där man kan välja olika färger och skicka dem samt meddelande till hosten.
  *@author  Joakim Flink
 */
public class Client {


/**
*Skapar en addShutdownHook som avslutar clietens uppkoppling till hosten om man stänger ner via X eller om det uppstår error.
*@param args Startargument.
 */
public static void main (String[] args) {

  Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
   try {
    client.close();
    System.out.println("Disconnect!");
    System.exit(0);
  } catch (Exception e) {System.out.println("Error" + e); }
}});

  Client MyCon=new Client();
}
/**
*Kör metoden BuildIntroInterface direkt när programmet startas.
 */
public Client()
{
  BuildIntroInterface();
}

public String Name="";
/**
*Bygger ett interface där man får skriva in namn som ska symbolisera användaren och servern man vill konnekta tills ipaddress.
 */
private void BuildIntroInterface(){

 JFrame f = new JFrame("Logga in");
 f.setSize(200, 200);
 f.setLocation(300,200);
 JLabel Lmail = new JLabel("Namn:");
 JTextField Fmail= new JTextField(20);
 JLabel Lserver = new JLabel("Server adress:");
 JTextField Fserver= new JTextField(20);
 JButton button = new JButton("Logga in");
 JPanel labelPane = new JPanel(new GridLayout(2,2));
 labelPane.add(Lserver);
 labelPane.add(Fserver);
 labelPane.add(Lmail);
 labelPane.add(Fmail);
 labelPane.setBorder(BorderFactory.createTitledBorder(
  BorderFactory.createEtchedBorder(), "Login Panel"));
 JPanel buttonPane = new JPanel(new GridLayout(1,3));
 buttonPane.add(button);
 f.add(labelPane, BorderLayout.NORTH);
 f.add(buttonPane, BorderLayout.CENTER); 
 button.addActionListener(new ActionListener() {

  @Override
  public void actionPerformed(ActionEvent e) {
  	f.setVisible(false);
    Name= Fmail.getText();
    BuildInterface();
    ConnectToServer(Fserver.getText());
  }
});
 f.setVisible(true);
}

/**
*Skapar en connection till hosten och skickar meddelandet man valt.
*@param Message Meddelandet som ska skickas till hosten, det är antingen den färgen man trycker på eller det meddelandet man skirver in.
 */
private void SendToHost(String Message){

	try{
    new GetDataFromServer(client);
    PrintWriter  out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "ISO-8859-1"), true);
 String  sendMessage=Message;  
         out.println(sendMessage);      
        out.flush();                   
        
      }
      catch(Exception e)
      {
       System.out.println("Error" + e);
       
     }

   }
   public static Socket client;
   /**
*Skapar en koppling till serven på port 2000.Skickar sedan det valda användarnamnet som startmeddelande.
*@param serverName ip adressen till servern man vill ansluta till.
 */
   private void ConnectToServer(String serverName){
    int port = 2000;

    try {
     System.out.println("Connecting to " + serverName + " on port " + port);
     client = new Socket(serverName, port);
     SendToHost("N:"+Name);
   } catch (IOException e) {
     e.printStackTrace();
     System.exit(0);
   }
 }


/**
*Bygger upp interfacet med tre knappar, en grön, en gul och en röd samt en textarea med skicka knapp. Meddelandet skickar när man trycker
* på någon av knapparna, då är det olika meddelanden med beroende på vad man trycker.
 */
private void BuildInterface(){

 JFrame f = new JFrame("Klient");

 f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 f.setSize(500, 500);
 f.setLocation(300,200);
 JTextArea textArea;
 JLabel Ldes = new JLabel("Skriv en kommentar nedan:");
 JButton button = new JButton("Fungerar");
 JButton green = new JButton("Fungerar");
 JButton yellow = new JButton("Halvt");
 JButton red = new JButton("Inte alls");
 JButton send = new JButton("Skicka");
 textArea = new JTextArea(10, 60);
 green.setBackground(Color.GREEN);
 yellow.setBackground(Color.YELLOW);
 red.setBackground(Color.RED);
 send.setPreferredSize(new Dimension(100, 100));
 JPanel fieldPane = new JPanel(new GridLayout(1,1));
 JPanel textPane = new JPanel(new GridLayout(0,1));
 JPanel buttonPane = new JPanel(new GridLayout(1,3));
 buttonPane.setPreferredSize(new Dimension(100, 80));
 fieldPane.add(Ldes); 
 textPane.add(textArea);
 textPane.setBorder(BorderFactory.createTitledBorder(
  BorderFactory.createEtchedBorder(), "Skriv  ett meddelande till lararen har:"));
 
 GridBagConstraints constraints = new GridBagConstraints();
 
 constraints.gridheight = 2;
 textPane.add(send,constraints);
 buttonPane.add(red);
 buttonPane.add(yellow);
 buttonPane.add(green);
 f.add(textPane, BorderLayout.SOUTH);
 f.add(buttonPane, BorderLayout.NORTH); 

 red.addActionListener(new ActionListener() {  @Override
   public void actionPerformed(ActionEvent e) {SendToHost("red"); 
 }});
 yellow.addActionListener(new ActionListener() {  @Override
   public void actionPerformed(ActionEvent e) {SendToHost("yellow"); }});
 green.addActionListener(new ActionListener() {  @Override
   public void actionPerformed(ActionEvent e) {SendToHost("green"); }});
 send.addActionListener(new ActionListener() {  @Override
   public void actionPerformed(ActionEvent e) {SendToHost(textArea.getText()); }});
 f.setVisible(true);
}


}
/**
*Skapar subklass till Thread som får in en socket och skickar meddelanden som klickas på.
 */
class GetDataFromServer extends Thread {
  Socket client;
  /**
*Skapar skickar meddelanden till servern.
*@param input Socketen som skickas in.
 */
  public GetDataFromServer(Socket input) {
    client=input;
    start(); 
  }
  public void run() {
   try { 
     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
     String inStr="";
     while ((inStr = in.readLine()) != null) {
      System.out.println(inStr);
    }
  }catch (IOException e) {
   e.printStackTrace();
 }
 
}
}

