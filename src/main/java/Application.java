import org.rocksdb.RocksDBException;

import java.util.Scanner;

public class Application {
    public static void main(String[] args) throws RocksDBException {
        Auth auth = new Auth();
        while (true) {
            System.out.println("For login enter <1> \nFor register enter <2>");
            Scanner in = new Scanner(System.in);
            int mode = in.nextInt();
            switch (mode) {
                case 1:
                    auth.login();
                    break;
                case 2:
                    auth.register();
                    break;
                default:
                    break;
            }
        }
    }
}
