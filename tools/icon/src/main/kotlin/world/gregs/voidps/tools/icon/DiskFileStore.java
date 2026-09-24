package world.gregs.voidps.tools.icon;

import world.gregs.voidps.cache.Cache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A {@link ResourceProvider} that reads a single archive/index straight out of a
 * classic on-disk Jagex 555 cache (main_file_cache.dat2 + main_file_cache.idxN),
 * with no OnDemand/network/RSA verification involved. Lets the standalone
 * tooling (see {@link CacheItemSpriteDumper}) build {@link Js5} archives
 * directly from a cache directory instead of downloading via the client's
 * normal OnDemand bootstrap ({@link Class340}/{@link Class369_Sub2}).
 */
final class DiskFileStore extends ResourceProvider {

    private static final int SECTOR_SIZE = 520;
    private static final int HEADER_SIZE = 8;
    private static final int DATA_SIZE = SECTOR_SIZE - HEADER_SIZE;
    private static final int EXTENDED_HEADER_SIZE = 10;
    private static final int EXTENDED_DATA_SIZE = SECTOR_SIZE - EXTENDED_HEADER_SIZE;
    private static final int INDEX_META = 255;

    private final Cache cache;
    private final RandomAccessFile data;
    private final RandomAccessFile index;
    private final RandomAccessFile indexMeta;
    private final int idx;
    private Js5Index referenceTable;

    DiskFileStore(String cacheDir, int idx, Cache cache) throws IOException {
        this.idx = idx;
        this.cache = cache;
        data = new RandomAccessFile(new File(cacheDir, "main_file_cache.dat2"), "r");
        index = new RandomAccessFile(new File(cacheDir, "main_file_cache.idx" + idx), "r");
        indexMeta = new RandomAccessFile(new File(cacheDir, "main_file_cache.idx" + INDEX_META), "r");
    }

    final int method2335(int i, int i_0_) {
        return 100;
    }

    final void method2338(byte i, int i_6_) {
        /* no preload queue needed for direct disk reads */
    }

    final byte[] fetchGroup(int file, byte i_7_) {
//        System.out.println("Load " + file + " " + idx);
        try {
            byte[] data = read(index, file, idx);
//            System.out.println("Expected: " + Arrays.toString(data));
            return data;
        } catch (IOException ioexception) {
            throw new RuntimeException("Failed reading archive " + idx + " file " + file, ioexception);
        }
//        byte[] data1 = cache.data(idx, file, 0, null);
//        System.out.println("Actual: " + Arrays.toString(data1));
//        return data1;//read(index, file, archive);
    }

    final Js5Index index(byte i) {
        if (referenceTable != null) return referenceTable;
        try {
            byte[] raw = read(indexMeta, idx, INDEX_META);
            if (raw == null) return null;
            int crc = Class59_Sub1.method554(5126, raw.length, raw);
            referenceTable = new Js5Index(raw, crc, null);
            return referenceTable;
        } catch (IOException ioexception) {
            throw new RuntimeException("Failed reading reference table for archive " + idx, ioexception);
        }
    }

    private byte[] read(RandomAccessFile idxFile, int fileId, int expectedIndexId) throws IOException {
        long entryOffset = (long) fileId * 6;
        if (entryOffset + 6 > idxFile.length()) return null;
        byte[] entry = new byte[6];
        idxFile.seek(entryOffset);
        idxFile.readFully(entry);
        int size = ((entry[0] & 0xff) << 16) | ((entry[1] & 0xff) << 8) | (entry[2] & 0xff);
        int sector = ((entry[3] & 0xff) << 16) | ((entry[4] & 0xff) << 8) | (entry[5] & 0xff);
        if (size == 0 || sector == 0) return null;

        byte[] result = new byte[size];
        byte[] buf = new byte[SECTOR_SIZE];
        int remaining = size;
        int chunk = 0;
        boolean extended = fileId > 0xffff;
        while (remaining > 0) {
            if (sector == 0) throw new IOException("Unexpected end of sector chain (archive " + expectedIndexId + ", file " + fileId + ")");
            data.seek((long) sector * SECTOR_SIZE);
            data.readFully(buf);

            int currentFile, currentChunk, nextSector, currentIndex, headerSize, dataSize;
            if (extended) {
                currentFile = ((buf[0] & 0xff) << 24) | ((buf[1] & 0xff) << 16) | ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
                currentChunk = ((buf[4] & 0xff) << 8) | (buf[5] & 0xff);
                nextSector = ((buf[6] & 0xff) << 16) | ((buf[7] & 0xff) << 8) | (buf[8] & 0xff);
                currentIndex = buf[9] & 0xff;
                headerSize = EXTENDED_HEADER_SIZE;
                dataSize = EXTENDED_DATA_SIZE;
            } else {
                currentFile = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
                currentChunk = ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
                nextSector = ((buf[4] & 0xff) << 16) | ((buf[5] & 0xff) << 8) | (buf[6] & 0xff);
                currentIndex = buf[7] & 0xff;
                headerSize = HEADER_SIZE;
                dataSize = DATA_SIZE;
            }
            if (currentFile != fileId || currentChunk != chunk || currentIndex != expectedIndexId) {
                throw new IOException("Sector chain corrupt reading archive " + expectedIndexId + " file " + fileId + " at sector " + sector);
            }
            int len = Math.min(remaining, dataSize);
            System.arraycopy(buf, headerSize, result, size - remaining, len);
            remaining -= len;
            sector = nextSector;
            chunk++;
        }
        return result;
    }
}
