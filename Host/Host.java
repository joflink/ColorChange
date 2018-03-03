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
*Skapar en uppkoppling till en mysqldatabas samt skapar en tcp server och får inputs av clienter
  *@author  Joakim Flink
 */
public class Host {


/**
*Skapar en instans av MydatabaseConnection och kör GetData metoden för att hämta värden.
*I main skapas först en shutdownhook för att se till att databaskopplingen och tcp kopplingarna är stängda , den senare är viktig att
*stänga för att andra processer ska kunna använda porten. Efter detta stängs programmet ner, denna hook är viktig om användaren använder
*X i den grafiska interfacet för att avsluta eller om error uppkommer.
*Efter detta så körs den statiska metoden ConnectToBase från Databaseandler för att ansluta till Mysqldatabasen.
*Därefter körs GetNames från DatabaseHandler för att se till att alla namnen på reggde klienter finns tillgängliga.
*@param args Startargument
 * @see         DatabaseHandler 
 */
  public static void main (String[] args) {

Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
     try {
        DatabaseHandler.conn.close();
        server.close();
        System.exit(0);
    } catch (Exception e) {System.out.println("Error" + e); }
    }});

  DatabaseHandler.ConnectTobase();
   DatabaseHandler.GetNames();
   Host MyCon=new Host();


 }
/**
*Skapar en server via CreateServer metoden
 */
public Host()
 {
  CreateServer();
}
/**
*Skapar en tcp server på port 2000.Tar fram datorns ipadress och skickar den till ett field i den grafiska layouten för att lätt kunna göra åtkomligt
*åt clienterna. Använder metoden BuildInterface för att bygga det grafiska komponenterna.
*Efter detta så skapas en loop som accepterar klienter som försöker ansluta och ger dem en task iform av Clienthandler objekt.
 * @see         DatabaseHandler 
 */
public void CreateServer(){
     int port= 2000;

  try {
    server = new ServerSocket(port);
   System.out.println("Host is ready for Clients");
   String hostadress = server.getInetAddress().getLocalHost().getHostName();
String hostip= server.getInetAddress().getLocalHost().getHostAddress();
  BuildInterface(hostip);
   while (true) {
    try {  

      Socket connection = server.accept();
      Thread task =  new ClientHandler(connection,hostadress);
      task.start();
    } catch (Exception ex) {}
  }
} catch (Exception ex) {
  System.err.println(ex);

  System.exit(0);
}

}


static ServerSocket server;
static JFrame f;
static JPanel Boxpanel;
 static  JTextArea TInputs;
/**
*Bygger upp interfacet, först en label där det står ipadress och ett textfield där hostens ip står. Därefter finns en tom ruta som populeras med boxar
*när klienter ansluter, dessa boxar visar den färg som klienten valt. Grön, gul, röd där den första är att det går bra, den andra sådär och tredje inte alls.
* Skriver Clienten ett meddelande så hamnar det i en textarea under boxarna.
*@param hostadress Ipadressen till hosten, skrivs ut i fieldbox för att kunna delas ut till användarna.
 */
private void BuildInterface(String hostadress){

  f = new JFrame("Host");
  f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
/**
*Skapar en koppling till databas där alla värden som kommer in från clienterna sparas, dessa värden länkas sedan till en webbsida
* där hosten kan se sammanställningar av datan.
 */
class DatabaseHandler 
{

  public  static String datum;
 private static HashSet<user> Users = new HashSet<user>();
 public static Connection conn;

/**
*Skapar en koppling till databasen, skapar även en datetimer som används för att ge det aktuella datumet för variabeln datum.
 */
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
    System.out.println("Connected to Mysql-server");
    conn= dbConnection;
  }
  catch(SQLException e)
  {
   System.out.println("Error:   "+ e);
 }

} catch (Exception e) { System.out.println("Error:   "+ e);   }

 
}
/**
*Använder databas kopplingen för att hämta alla namn på de elever som använt clienten tidigare, detta för att kunna kartlägga deras
* data över flera iterationer. Dessa användare läggs i objekt i en hashset av typen user där namn och senast inloggade sparas.
 * @see  user 
 */
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
  }
  st.close();
}
catch(SQLException e)
{
 System.out.println("Error:   "+ e);
}
}
/**
*Sparar färgerna clienten väljer i databasen. Detta görs med preparedstatement
*@param Name Namnet på clientens användare
*@param value värdet användaren valt(grön,gul,röd eller textmeddelande)
*@param timelength Längen i sekunder sedan senast använderen bytte värdet.S
 */
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
*Kollar om användaren finns i databasen, annars lägger den till användaren.
*Finns användaren så ökar antalet gånger han varit inloggad med ett.
*@param Name står för Namn
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
      String query = "update elever set lastactive = ?, timesactive= ? where name = ?";
      PreparedStatement preparedStmt = conn.prepareStatement(query);
      preparedStmt.setString(1, datum);
      preparedStmt.setInt(2, ggraktiv);
      preparedStmt.setString(3, Name);
      preparedStmt.executeUpdate();
      preparedStmt.close();
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
     String query = " insert into elever (name, lastactive, timesactive)"
     + " values (?, ?, ?)";
     PreparedStatement preparedStmt = conn.prepareStatement(query);
     preparedStmt.setString (1, Name);
     preparedStmt.setString (2, datum);
     preparedStmt.setInt(3, 1);
     preparedStmt.execute();
     Users.add(new user(Name,1));
     preparedStmt.close();
   }
   catch(SQLException e)
   {
     System.out.println("Error:   "+ e);
   }

 }
}

}
/**
*Klassen för användare, finns för att hämta användare från databasen och lägga dem i en och samma hashset.
 */
class user{
  public String name;
  public int logins;
  /**
*Konstruktor som lägger in det inskickade värdet till de globala variablerna.
*@param Name står för Namn
*@param Logins står för antalet inloggningar
 */
public user(String Name,int Logins)
{
  name=Name;
  logins=Logins;

}
}

/**
*Hanterar kontakten med Clienten
 */
class ClientHandler  extends Thread {
  Socket socket;
  ServerSocket server;
  BufferedReader reader;
  private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
  PrintWriter writer;
  String Namn; 

  /**
*Skapar en skriv och läsanslutning till användaren.
*@param inputsocket det är socketen som connectar till hosten.
*@param Hadress hostens ipadress, den skrivs till Clienten så den kan se att den är rätt ansluten.
 */
public ClientHandler (Socket inputsocket, String Hadress) {
  try {
   socket=inputsocket;
   reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   writer = new PrintWriter(socket.getOutputStream(), true);
   writer.println("Welcome to the host at "+Hadress);

   writers.add(writer); 
 } catch (IOException e) {
 }

 System.out.println("Client connected from " + socket.getLocalAddress().getHostAddress());
 Database = new DatabaseHandler();

}




private DatabaseHandler Database;
long startTime = System.currentTimeMillis();
  /**
*Tar in meddelanden från clienten, den kollar om meddelandet innehåller ":" och delar den på två då andra delen är namnet på användaren,
* detta för att hålla isär användarens namn och dess meddelanden.
*Om det är en ny anslutning så skapas en knapp som läggs in i det grafiska gränssnittet, är det inte det så uppdateras kanppen enligt 
* dess meddelande.
 */
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
 *Tar bort användaren och tömmer anslutningen om clienten inte svarar. Tar bort en aktiv användaren från listan.Samt tar bort den 
 *användarens knapp.
 */
private void killThread(){

  writers.remove(writer);
  Container parent = button.getParent();
parent.remove(button);
SwingUtilities.updateComponentTreeUI(Host.f);

  try {writer.close();
   reader.close();
   socket.close();
 } catch (IOException e) {
 }
}




 public JButton button;
   /**
*Skapar en ny knapp och lägger in i mitten av det grafiska gränssnittet, knappen anpassas i storlek efter hur mång andra knappar det finns.
 */
private void CreateButton(){


  button = new JButton(Namn);
 Host.Boxpanel.add(button);
Host.f.add( Host.Boxpanel, BorderLayout.CENTER);
Database.InputData(Namn);
SwingUtilities.updateComponentTreeUI(Host.f);
}
  /**
*Uppdaterar den befintliga knappen enligt meddelandets instruktioner, är det en färg som står så ändras knappens färg till detta
* är det inte det så skrivs namnet på clienten följt av meddelandet ut i textarean.
*@param Medd meddelandeet som skickas från klienten till hosten.
 */
private void UppdateButton(String Medd)
{
  if (Medd!=null && !Medd.isEmpty()) {

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
  /**
*Hämtar en long variabel angående hur lång tid det var mellan senaste ändringen och nuvarande från användaren.
 *@return en variabel där tiden mellan värdeändringarna räknas ut och returneras.
 */
public long getAgeInSeconds() {
        long nowMillis = System.currentTimeMillis();
        long output=((nowMillis - startTime) / 1000);
     startTime=System.currentTimeMillis();
    return output;
    }


}
