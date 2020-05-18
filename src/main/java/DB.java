import org.rocksdb.RocksDBException;

public interface DB {
    byte[] get(byte[] input) throws RocksDBException;
    void put(byte[] k, byte[] v) throws RocksDBException;
    boolean contains(byte[] key) throws RocksDBException;
}
