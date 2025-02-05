import java.sql.*;
import java.util.Scanner;

public class Atmapp{
    private static final String DB_URL="jdbc:mysql://localhost:3306/BANK";
    private static final String user="root";      
    private static final String password="Kumara.@01";  
    private static final double Daily_withdraw_limit=10000.00;

    public static void main(String[] args){
        Scanner sc=new Scanner(System.in);
        try (Connection conn=DriverManager.getConnection(DB_URL,user,password)) {
            System.out.println("Welcome to the ATM.");
            
            
            // Authenticate User
            int accountId=authenticateUser(conn,sc);
            if (accountId==-1){
                System.out.println("Authentication failed.Exiting...");
                return;
            }

            //ATM Menu
            boolean running=true;
            while(running){
                System.out.println("\n---ATM Menu---");
                System.out.println("1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. Check Balance");
                System.out.println("4. Exit");
                System.out.print("Enter your choice: ");
                int choice=sc.nextInt();
                sc.nextLine(); // consume newline

                switch(choice){
                    case 1:
                        deposit(conn,sc,accountId);
                        break;
                    case 2:
                        withdraw(conn,sc,accountId);
                        break;
                    case 3:
                        checkBalance(conn,accountId);
                        break;
                    case 4:
                        running=false;
                        System.out.println("Thank you for using the ATM.");
                        break;
                    default:
                        System.out.println("Invalid choice.Please try again.");
                        break;
                }
            }
        } 
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // User Authentication
    public static int authenticateUser(Connection conn,Scanner sc)throws Exception{
        System.out.println("Enter Account ID:");
        int accountId=sc.nextInt();
        System.out.println("Enter PIN:");
        int pin=sc.nextInt();
        sc.nextLine();

        String sql="SELECT account_id FROM accounts WHERE account_id=? AND pin=?";
        try (PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setInt(1,accountId);
            pstmt.setInt(2,pin);
            try (ResultSet rs=pstmt.executeQuery()){
                if(rs.next()){
                    System.out.println("Authentication successful.");
                    return accountId;
                } 
                else{
                    System.out.println("Invalid Account ID or PIN.");
                    return -1;
                }
            }
        }
    }

    // Withdraw Money with Limit
    public static void withdraw(Connection conn,Scanner sc,int accountId)throws Exception{
        System.out.print("Enter withdrawal amount: ");
        double amount=sc.nextDouble();
        sc.nextLine();
        if(amount>Daily_withdraw_limit){
            System.out.println("Withdrawal amount exceeds daily limit of $"+Daily_withdraw_limit);
            return;
        }

        String selectSQL="SELECT balance FROM accounts WHERE account_id=?";
        try(PreparedStatement selectStmt=conn.prepareStatement(selectSQL)){
            selectStmt.setInt(1,accountId);
            try(ResultSet rs=selectStmt.executeQuery()){
                if(rs.next()){
                    double balance=rs.getDouble("balance");
                    if(balance<amount){
                        System.out.println("Insufficient funds.Current balance: $"+balance);
                        return;
                    }
                } 
                else{
                    System.out.println("Account not found.");
                    return;
                }
            }
        }

        String updateSQL="UPDATE accounts SET balance=balance -? WHERE account_id=?";
        try(PreparedStatement updateStmt=conn.prepareStatement(updateSQL)){
            updateStmt.setDouble(1,amount);
            updateStmt.setInt(2,accountId);
            int rowsUpdated=updateStmt.executeUpdate();
            if(rowsUpdated>0){
                System.out.println("Successfully withdraw $"+amount);
            } 
            else{
                System.out.println("Error processing withdrawal.");
            }
        }
    }

    // Deposit money
    public static void deposit(Connection conn,Scanner sc,int accountId)throws Exception{
        System.out.print("Enter deposit amount: ");
        double amount=sc.nextDouble();
        sc.nextLine();

        String sql="UPDATE accounts SET balance=balance +? WHERE account_id=?";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setDouble(1,amount);
            pstmt.setInt(2,accountId);
            int rowsUpdated=pstmt.executeUpdate();
            if(rowsUpdated>0){
                System.out.println("Deposited $"+amount+" successfully.");
            } 
            else{
                System.out.println("Error processing deposit.");
            }
        }
    }

    // Check Balance
    public static void checkBalance(Connection conn,int accountId)throws Exception {
        String sql="SELECT balance FROM accounts WHERE account_id=?";
        try(PreparedStatement pstmt=conn.prepareStatement(sql)){
            pstmt.setInt(1,accountId);
            try(ResultSet rs=pstmt.executeQuery()){
                if(rs.next()){
                    double balance=rs.getDouble("balance");
                    System.out.println("Current Balance: $"+balance);
                } 
                else{
                    System.out.println("Account not found.");
                }
            }
        }
    }
}