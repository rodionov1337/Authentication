import org.rocksdb.*;

public class Database implements DB {

    RocksDB db;

    public Database(String path) throws RocksDBException {
        db = RocksDB.open(new Options().setCreateIfMissing(true).prepareForBulkLoad(), path);
    }

    @Override
    public byte[] get(byte[] input) throws RocksDBException {
        ReadOptions options = new ReadOptions().setSnapshot(db.getSnapshot());
        byte[] res = db.get(options, input);
        if (res != null) return res;
        else return new byte[0];
    }

    @Override
    public void put(byte[] k, byte[] v) throws RocksDBException {
        WriteOptions options =  new WriteOptions();
        WriteBatch batch = new WriteBatch();
        batch.put(k, v);
        db.write(options, batch);
    }

    @Override
    public boolean contains(byte[] key) throws RocksDBException {
        ReadOptions options = new ReadOptions().setSnapshot(db.getSnapshot());
        byte[] res = db.get(options, key);
        return res != null;
    }
}
