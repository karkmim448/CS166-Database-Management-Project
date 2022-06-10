/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql); break;
                   case 2: UpdateProfile(esql); break;
                   case 3: PlaceOrder(esql); break;
                   case 4: UpdateOrder(esql); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

 public static void Menu(Cafe esql){
  try{   
      boolean cafemenu = true;
     
     //Allow customers to view menu items but not change them
      while(cafemenu){
         System.out.println("MENU");
         System.out.println("----");
         System.out.println("1. View Menu Items");
	      System.out.println("2. Modify Menu Items");
         System.out.println(".........................");
         System.out.println("9. < EXIT");
         String Manageruser = null;
	      switch (readChoice()){ 
            //TODO For managers only
            //System.out.println("2. Modify Menu Items");
            case 1: ItemMenu(esql); break;
            case 2: Manageruser = ManagerLogin(esql); break;
            case 9: cafemenu = false; break;
            default: System.out.println("Unrecognized choice!"); break;
     	   }//end switch
	
	if(Manageruser != null){
		boolean modifymenu = true;
		while(modifymenu){
			System.out.println("1. Add Items");
			System.out.println("2. Delete Items");
			System.out.println("3. Update Items");
         System.out.println(".........................");
         System.out.println("9. < EXIT");
			
			switch(readChoice()){
				case 1:  System.out.print("\tEnter new Item's Name: ");
                     String itemName1 = in.readLine();
                     System.out.print("\tEnter new Item's Type: ");
                     String type1 = in.readLine();
                     System.out.print("\tEnter new Item's Price: ");
                     String price1 = in.readLine();
                     System.out.print("\tEnter new Item's Description: ");
                     String description1 = in.readLine();
                     System.out.print("\tEnter new Item's ImageURL: ");
                     String imageURL1 = in.readLine();
                     String query1 = String.format("INSERT INTO MENU (itemName, type, price, description, imageURL) VALUES ('%s', '%s', '%s', '%s', '%s')", itemName1, type1, price1, description1, imageURL1);
                     esql.executeUpdate(query1);
                     System.out.println ("Item successfully added!");
                     break; 
				case 2:  System.out.print("\tEnter name of the Item you would like to delete: ");
                     String itemName = in.readLine();
                     String query2 = String.format("DELETE FROM MENU WHERE itemName = '%s'", itemName);
                     esql.executeUpdate(query2);
                     System.out.println ("Item successfully deleted!");
                     break;
				case 3:  UpdateItem(esql); break;
            case 9:  modifymenu = false; break;
				default: System.out.println("Unrecognized choice!"); break;
			}//end switch
		}//end if
	}//end while

      }//end while
   }catch(Exception e){
      System.err.println (e.getMessage ());
   }//end catch

  }//end function

   //For viewing the actual menu items for customers
  public static void ItemMenu(Cafe esql){
   try{  
     boolean viewmenu = true;

     //Separate Name and Type into two options because its easiest to implement for me
     while(viewmenu){
        System.out.println("1. Search by Item Name");
        System.out.println("2. Search by Item Type");
        System.out.println(".........................");
        System.out.println("9. < EXIT");

	switch (readChoice()){
        case 1: EnterItemName(esql); break;
        case 2: EnterItemType(esql); break;
        case 9: viewmenu = false; break;
	default: System.out.println("Unrecognized choice!"); break;
	}
     }
   }catch(Exception e){
      System.err.println (e.getMessage ());
   }
  }

public static void EnterItemName(Cafe esql){
   try{
      System.out.println("Enter Item Name:");
      String itemName = in.readLine();        
      String query = String.format("SELECT * FROM MENU WHERE itemName = '%s'", itemName);
	
      esql.executeQueryAndPrintResult(query);

   }catch(Exception e){
	System.err.println (e.getMessage ());
    }  
}

public static void EnterItemType(Cafe esql){
   try{
      System.out.println("Enter Item Type");
      String itemType = in.readLine();       
      String query = String.format("SELECT * FROM MENU WHERE type = '%s'", itemType);

      esql.executeQueryAndPrintResult(query);
      
   }catch(Exception e){
      System.err.println (e.getMessage ());
   }   
  }

//Wasn't sure how to implement this without simply inputting user login and password again
public static String ManagerLogin(Cafe esql){
	try{
		System.out.println("FOR MANAGERS ONLY");
		
		System.out.print("\tEnter user login: ");
         	String login = in.readLine();
         	System.out.print("\tEnter user password: ");
         	String password = in.readLine();

         	String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s' AND type = 'Manager'", login, password);
		
		int userNum = esql.executeQuery(query);
		if(userNum > 0){
			return login;
		}
		else{		
			System.out.println("You are not a manager.");
			return null;
		}					
	}catch(Exception e){
		System.err.println (e.getMessage ());
		return null;
	}
}//end ManagerLogin function

public static void UpdateItem(Cafe esql){
   try{
      boolean updatemenu = true;
      while(updatemenu){
         System.out.print("\tEnter the name of the Item you would like to update: ");
         String itemName = in.readLine();

	 //to check if the item that is being searched for exists
         String query0 = String.format("SELECT * FROM MENU WHERE itemName = '%s'", itemName);
         int itemExists = esql.executeQuery(query0);
         if(itemExists < 1){
            System.out.println("This item doesn't exist");
            break;
         }//end if

         System.out.println("Which attribute would you like to change?");
         System.out.println("---------");
         System.out.println("1. Item Name");
         System.out.println("2. Type");
         System.out.println("3. Price");
         System.out.println("4. Description");
         System.out.println("5. Image URL");
         System.out.println(".........................");
         System.out.println("9. < Exit");

         switch(readChoice()){
            case 1: System.out.print("\tEnter new Item Name: ");
                    String newitemName = in.readLine();
                    String query1 = String.format("UPDATE MENU SET itemName = '%s' WHERE itemName = '%s'", newitemName, itemName);
                    esql.executeUpdate(query1);
		    System.out.println (itemName + " successfully updated to: " + newitemName);
                    break;
            case 2: System.out.print("\tEnter new Item type: ");
                    String newtype = in.readLine();
                    String query2 = String.format("UPDATE MENU SET type = '%s' WHERE itemName = '%s'", newtype, itemName);
                    esql.executeUpdate(query2);
		    System.out.println (itemName + "'s type successfully updated to: " + newtype);
                    break;
            case 3: System.out.print("\tEnter new Item price: ");
                    String newprice = in.readLine();
                    String query3 = String.format("UPDATE MENU SET price = '%s' WHERE itemName = '%s'", newprice, itemName);
                    esql.executeUpdate(query3);
		    System.out.println (itemName + "'s price successfully updated to: " + newprice);
                    break;
            case 4: System.out.print("\tEnter new Item description: ");
                    String newdes = in.readLine();
                    String query4 = String.format("UPDATE MENU SET description = '%s' WHERE itemName = '%s'", newdes, itemName);
                    esql.executeUpdate(query4);
		    System.out.println (itemName + "'s description successfully updated to: " + newdes);
                    break;
            case 5: System.out.print("\tEnter new Item imageURL: ");
                    String newURL = in.readLine();
                    String query5 = String.format("UPDATE MENU SET type = '%s' WHERE itemName = '%s'", newURL, itemName);
                    esql.executeUpdate(query5);
		    System.out.println (itemName + "'s imageURL successfully updated to: " + newURL);
                    break;
            case 9: updatemenu = false; break;
            default: System.out.println("Unrecognized choice!"); break;
         }//end switch
      }//end while
   }catch(Exception e){
         System.err.println (e.getMessage ());
         }//end try and catch
}//end UpdateItem function

  public static void UpdateProfile(Cafe esql){
  try {
         System.out.println ("For your safety please...");
         System.out.print("\tRenter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         boolean profile = true;
         while(profile) {
                System.out.println("1. Update login");
                System.out.println("2. Update phone number");
                System.out.println("3. Update password");
                System.out.println("4. Update fav. items");
                System.out.println("5. Update type (manager only)");
                System.out.println(".........................");
                System.out.println("9. Go back to MAIN MENU");
                switch (readChoice()){
                   case 1: System.out.print("\tEnter new login: ");
                           String login2 = in.readLine();
                           String query1 = String.format("UPDATE USERS SET login = '%s' WHERE login = '%s' AND password = '%s'", login2, login, password);
                           esql.executeUpdate(query1);
                           System.out.println ("Login successfully updated!");
                           break;
                   case 2: System.out.print("\tEnter new phone number: ");
                           String phone = in.readLine();
                           String query2 = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s' AND password = '%s'", phone, login, password);
                           esql.executeUpdate(query2);
                           System.out.println ("Phone successfully updated!");
                           break;
                   case 3: System.out.print("\tEnter new password: ");
                           String pass = in.readLine();
                           String query3 = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s' AND password = '%s'", pass, login, password);
                           esql.executeUpdate(query3);
                           System.out.println ("Password successfully updated!");
                           break;
                   case 4: System.out.print("\tEnter fav items: ");
                           String fav = in.readLine();
                           String query4 = String.format("UPDATE USERS SET favItems = '%s' WHERE login = '%s' AND password = '%s'", fav, login, password);
                           esql.executeUpdate(query4);
                           System.out.println ("Fav items successfully updated!");
                           break;
                   case 5: String type="Manager";
                           String query5 = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s' AND type = '%s'", login, password, type);
		           int userNum = esql.executeQuery(query5);
		           if(userNum > 0)
                           {
                              System.out.print("\tEnter user login: ");
                              String u_login = in.readLine();
                              System.out.println("UPDATE TO");
                              System.out.println("---------");
                              System.out.println("1. Manager");
                              System.out.println("2. Employee");
                              System.out.println("3. Customer");
                              String u_type = "";
                              switch (readChoice()){
                                 case 1: u_type="Manager"; break;
                                 case 2: u_type="Employee"; break;
                                 case 3: u_type="Customer"; break;
                                 default : System.out.println("Unrecognized choice!"); break;
                              }
                              String query6 = String.format("UPDATE USERS SET type = '%s' WHERE login = '%s'", u_type, u_login);
                              esql.executeUpdate(query6);
                              System.out.println ("User type successfully updated!");
                              String query7 = String.format("SELECT * FROM Users WHERE login = '%s'", u_login);
                              int rowCount = esql.executeQueryAndPrintResult(query7);
                              System.out.println ("total row(s): " + rowCount);
                           }
			   else System.out.println("You are not a manager.");
                           break;
                   case 9: profile = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                  }
              }

         }catch(Exception e){
         System.err.println (e.getMessage ());
         }
  }

  public static void PlaceOrder(Cafe esql){}

  public static void UpdateOrder(Cafe esql){
     try{
        boolean updateorder = true;

        while(updateorder){
           System.out.print("\tEnter the ID of the order you would like to update: ");
           String inputorderid = in.readLine();

           //to check if the item that is being searched for exists
         String query0 = String.format("SELECT * FROM ORDERS WHERE orderid = '%s'", inputorderid);
         int orderExists = esql.executeQuery(query0);
         if(orderExists < 1){
            System.out.println("This OrderID doesn't exist");
            break;
         }//end if


         //now checking if the order is not paid yet
            String query1 = String.format("SELECT * FROM ORDERS WHERE orderid = '%s' AND paid = 'false'", inputorderid);
            int notpaid = esql.executeQuery(query1);
            if (notpaid > 0){
               while(updateorder){
                  System.out.println("What would you like to update?");
                  System.out.println("------------------------------");
                  System.out.println("1. OrderID");
                  System.out.println("2. Login");
                  System.out.println("3. Paid (FOR MANAGERS/EMPLOYEES ONLY)");
                  System.out.println("4. Timestamp Received");
                  System.out.println("5. Total");
                  System.out.println(".........................");
                  System.out.println("9. < Exit");

                  switch (readChoice()){
                     case 1: System.out.print("\tEnter new OrderID: ");
                           String neworderid = in.readLine();
                           String query2 = String.format("UPDATE ORDERS SET orderid = '%s' orderid = '%s'" , neworderid, inputorderid);
                           esql.executeUpdate(query2);
                           System.out.println ("OrderID successfully updated!");
                           break;
                     case 2: System.out.print("\tEnter new Login: ");
                           String newlogin = in.readLine();
                           String query3 = String.format("UPDATE ORDERS SET login = '%s' orderid = '%s'" , newlogin, inputorderid);
                           esql.executeUpdate(query3);
                           System.out.println ("Login successfully updated!");
                           break;
                     case 3: System.out.println ("Please verify that you are a manager or an employee.");
                           System.out.print("\tEnter user login: ");
                           String login = in.readLine();
                           System.out.print("\tEnter user password: ");
                           String password = in.readLine();
                           String query4 = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s' AND type = 'Customer'", login, password);
                           int cus = esql.executeQuery(query4);
                           if (cus > 0){
                              System.out.println("You are not a manager or an employee.");
                              break;
                           }//end if
                           else{
                              String query5 = String.format("UPDATE ORDERS SET paid = true WHERE orderid = '%s'", inputorderid);
                              esql.executeQuery(query5);
                              System.out.println("Updated order to paid!");
                              break;
                           }//end else
                     case 4: System.out.print("\tEnter new Timestamp: ");
                           String newtimestamp = in.readLine();
                           String query6 = String.format("UPDATE ORDERS SET timeStampRecieved = '%s' orderid = '%s'" , newtimestamp, inputorderid);
                           esql.executeUpdate(query6);
                           System.out.println ("Timestamp successfully updated!");
                           break;
                     case 5: System.out.print("\tEnter new Total: ");
                           String newtotal = in.readLine();
                           String query7 = String.format("UPDATE ORDERS SET total = '%s' orderid = '%s'" , newtotal, inputorderid);
                           esql.executeUpdate(query7);
                           System.out.println ("Total successfully updated!");
                           break;
                     case 9: updateorder = false; break;
                     default: System.out.println("Unrecognized choice!"); break;
                  }//end switch
               }//end while
               
            }//end if
	    else{
		System.out.println("This order is already paid.");
		break;
	    }//end else
        }//end while
     }catch(Exception e){
         System.err.println (e.getMessage ());
         }//end try and catch
  }//end UpdateOrder function

}//end Cafe

