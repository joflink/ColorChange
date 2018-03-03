import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

/**
*Skapar en uppkoppling till en mysqldatabas och hämtar och visar upp värden i form av kommentarer, man kan lägga in egna värden. 
  *@author  Joakim Flink
 */
public class Client {


/**
*Skapar en instans av MydatabaseConnection och kör GetData metoden för att hämta värden.
*@param args Startargument.
 */
public static void main (String[] args) {

  Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
   try {
    client.close();
    System.out.println("Dis Connect!");
    System.exit(0);
  } catch (Exception e) {System.out.println("Error" + e); }
}});

  Client MyCon=new Client();

  // MyCon.GetData();
  
}

public Client()
{
  BuildIntroInterface();
}
public String Name="";
private void BuildIntroInterface(){

 JFrame f = new JFrame("Logga in");
 f.setSize(200, 200);
 f.setLocation(300,200);
 JLabel Lmail = new JLabel("Mail:");
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
    //SendMail(Fmailto.getText(),Fmailfrom.getText(),Fsubject.getText(),textArea.getText(),Fserver.getText(),Fnamn.getText(),Fpass.getText());
  }
});
 f.setVisible(true);
}

private void SendToHost(String Message){

	try{
    new GetDataFromServer(client);
        //Skriv till server
       // System.out.println("Just connected to " + client.getRemoteSocketAddress());
    PrintWriter  out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "ISO-8859-1"), true);
  //  BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
 String  sendMessage=Message;//Name+"|"+Message;   
         out.println(sendMessage);       // skickar meddelandet till servern
        out.flush();                    //            
        
      }
      catch(Exception e)
      {
       System.out.println("Error" + e);
       
     }

   }
   public static Socket client;
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
*Bygger upp interfacet med hjälp av tre avdelningar, labels till vänster, textfält och knapp till höger och utskriftsruta i botten.
* Fyller man i ett nytt ,meddelande och klickar på knappen för att addera det så läggs det in i databasen via Inputdata metoden.
*Utskriftsfältet populeras av alla befintliga värden i databasen via GetData metoden.
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

//yellow.setPreferredSize(new Dimension(350, 40));
//red.setPreferredSize(new Dimension(350, 40));
 send.setPreferredSize(new Dimension(100, 100));
 JPanel fieldPane = new JPanel(new GridLayout(1,1));
 JPanel textPane = new JPanel(new GridLayout(0,1));
// fieldPane.add(button);

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
 //f.add(labelPane, BorderLayout.NORTH);
 f.add(textPane, BorderLayout.SOUTH);
 //f.add(fieldPane, BorderLayout.SOUTH);
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

class GetDataFromServer extends Thread {
  Socket client;
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

