import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.HashSet;
import java.util.Date;
import java.util.*;
import java.text.*;

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
  DatabaseHandler.ConnectTobase();
   DatabaseHandler.GetNames();
   Host MyCon=new Host();
 }

public Host()
 {
  CreateServer();
}

public void CreateServer(){
     int port= 2000;

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
 static  JTextArea TInputs;
/**
*Bygger upp interfacet med hjälp av tre avdelningar, labels till vänster, textfält och knapp till höger och utskriftsruta i botten.
* Fyller man i ett nytt ,meddelande och klickar på knappen för att addera det så läggs det in i databasen via Inputdata metoden.
*Utskriftsfältet populeras av alla befintliga värden i databasen via GetData metoden.
 */
private void BuildInterface(String hostadress){

  f = new JFrame("Host");
 f.setSize(500, 500);
 f.setLocation(300,200);
  JLabel Ladress = new JLabel("IP adress (delas till klienterna): ");
 JTextField Fadress= new JTextField(20);
 Fadress.setText(hostadress);
  TInputs = new JTextArea(10, 40);
 JPanel labelPane = new JPanel(new GridLayout(1,2));
 JPanel TextPane = new JPanel(new GridLayout(0,1));
  TextPane.add(TInputs);
 labelPane.add(Ladress);
 labelPane.add(Fadress);
  Boxpanel= new JPanel(new GridLayout(1,3));
 f.add(labelPane, BorderLayout.NORTH);
 f.add(TextPane, BorderLayout.SOUTH);
 f.setVisible(true);
}


}

class DatabaseHandler 
{


  private static HashSet<user> Users = new HashSet<user>();
  private static HashSet<String> names = new HashSet<String>();
  private static HashSet<Integer> timesactivelist = new HashSet<Integer>();
public static Connection conn;
public DatabaseHandler()
{
    
}

public static void ConnectTobase(){

 Date date = new Date();
      SimpleDateFormat ft =  new SimpleDateFormat ("yyyy.MM.dd");
       datum=ft.format(date);


  try { 
   Class.forName("com.mysql.jdbc.Driver").newInstance();
   String computer = "217.78.20.215";
   String db_name = "colorchange";
   String username = "colorlog";
   String password = "colorkid";
   String url = "jdbc:mysql://" + computer + "/" + db_name;
   try { 
    Connection dbConnection = DriverManager.getConnection(url, username, password);
    System.out.println("Connected to server");
    conn= dbConnection;
  }
  catch(SQLException e)
  {
   System.out.println("Error:   "+ e);
 }

} catch (ClassNotFoundException e) {    }
catch (InstantiationException e) {    } 
catch (IllegalAccessException e) {    }

 
}
   public  static String datum;

public static void GetNames(){
 try { 

  String query = "SELECT name, timesactive FROM elever";
  Statement st = conn.createStatement();

  ResultSet rs = st.executeQuery(query);

  while (rs.next())
  {
    String Name = rs.getString("name");

    Integer tid = rs.getInt("timesactive");
    Users.add(new user(Name,tid));
    System.out.println(Name+tid);
 }
  st.close();

    System.out.println("ints set = "+timesactivelist);
}
catch(SQLException e)
{
 System.out.println("Error:   "+ e);
}
}

public void SaveColorchange(String Name, String value, long timelength){

 try { 
   String query = " insert into tider (name,value, timelength, date)"
   + " values (?, ?, ?, ?)";
   PreparedStatement preparedStmt = conn.prepareStatement(query);
   preparedStmt.setString (1, Name);
   preparedStmt.setString (2, value);
   preparedStmt.setLong(3, timelength);
   preparedStmt.setString(4, datum);
   preparedStmt.execute();
 preparedStmt.close();

  System.out.println("namn,tid: "+Name+timelength);
 }
 catch(SQLException e)
 {
   System.out.println("Error:   "+ e);
 }
}

/**
*Tar in 4 värden (Namn, Mail,Meddelande och Hemsida) lägger sedan in dem i databasen via preparedstatement.
*@param N står för Namn
*@param M står för Mail 
*@param C står för Meddelande 
*@param H står för Hemsida  
 */
public void InputData(String Name){

boolean nameExist= false;



int ggraktiv=0;

Iterator<user> iterator = Users.iterator();
    while(iterator.hasNext()){
      user usr = iterator.next();
    if (usr.name.equals(Name)) {
  nameExist=true;
ggraktiv=usr.logins;
ggraktiv++;
 System.out.println("Användaren:   "+Name+ "finns");
  break;
}
    }

if (nameExist==true) {
  try{
      // create the java mysql update preparedstatement
      String query = "update elever set lastactive = ?, timesactive= ? where name = ?";
      PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, datum);
      preparedStmt.setInt(2, ggraktiv);
      preparedStmt.setString(3, Name);

      // execute the java preparedstatement
      preparedStmt.executeUpdate();
       preparedStmt.close();
     //conn.close();
    }
    catch (Exception e)
    {
      System.err.println("Got an exception! ");
      System.err.println(e.getMessage());
    }
}
else
{
  try { 
  // Connection conn=ConnectTobase();
   String query = " insert into elever (name, lastactive, timesactive)"
   + " values (?, ?, ?)";
   PreparedStatement preparedStmt = conn.prepareStatement(query);
   preparedStmt.setString (1, Name);
   preparedStmt.setString (2, datum);
   preparedStmt.setInt(3, 1);
   preparedStmt.execute();

    Users.add(new user(Name,1));
  // conn.close();
 preparedStmt.close();
 }
 catch(SQLException e)
 {
   System.out.println("Error:   "+ e);
 }

}
}

}

class user{
  public String name;
  public int logins;
  public user(String Name,int Logins)
  {
name=Name;
logins=Logins;

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

   Database = new DatabaseHandler();

 }
private DatabaseHandler Database;
long startTime = System.currentTimeMillis();

 @Override
 public void run() {

  String msg;
  try {
    while ((msg = reader.readLine()) != null) {
   String[] Splitted= msg.split(":");
   if (Splitted.length==2) {
     
    Namn= Splitted[1];
    CreateButton();
   }
else{

   UppdateButton(msg);
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
  Container parent = button.getParent();
parent.remove(button);
SwingUtilities.updateComponentTreeUI(Host.f);

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
private void CreateButton(){


  button = new JButton(Namn);
 Host.Boxpanel.add(button);
Host.f.add( Host.Boxpanel, BorderLayout.CENTER);
Database.InputData(Namn);
SwingUtilities.updateComponentTreeUI(Host.f);
}

private void UppdateButton(String Medd)
{
  if (Medd!=null && !Medd.isEmpty()) {

      WriteToTasks(Namn+": "+Medd);
 if (Medd.equals("yellow")) {
 button.setBackground(Color.YELLOW);
  }
    else if (Medd.equals("green")) {
 button.setBackground(Color.GREEN);
  }
    else if (Medd.equals("red")) {
 button.setBackground(Color.RED);
  }
  else {
    Host.TInputs.append(Namn+": "+Medd+"\n");
  }

long time=getAgeInSeconds();
  Database.SaveColorchange(Namn,Medd,time);
}
}

public long getAgeInSeconds() {
        long nowMillis = System.currentTimeMillis();
        long output=((nowMillis - startTime) / 1000);
     startTime=System.currentTimeMillis();
    return output;
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
