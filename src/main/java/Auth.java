import org.rocksdb.RocksDBException;

import java.util.Arrays;
import java.util.Scanner;

public class Auth {
    DB db = new Database("");// enter path to database

    public Auth() throws RocksDBException {
    }
    void login() throws RocksDBException {
        System.out.println("Enter LOGIN");
        Scanner in = new Scanner(System.in);
        String login = in.nextLine();
        boolean isContains = db.contains(login.getBytes());
        if (isContains) {
            System.out.println("Enter password");
            String password = in.nextLine();
            byte[] passHash = Hash.hash(password.getBytes());
            byte[] dbHash = db.get(login.getBytes());
            if (Arrays.equals(passHash, dbHash)) System.out.println("###########\nYou entered \n###########");
            else {
                System.out.println("Not correct password");
                login();
            }
        }
        else {
            System.out.println("No user with login " + login);
        }
    }
    void register() throws RocksDBException {
        System.out.println("Enter login");
        Scanner in = new Scanner(System.in);
        String login = in.nextLine();
        System.out.println("Enter password");
        String password = in.nextLine();
        byte[] hashPassword = Hash.hash(password.getBytes());
        db.put(login.getBytes(), hashPassword);
    }

}
