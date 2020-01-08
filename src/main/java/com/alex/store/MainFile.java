package com.alex.store;

import com.alex.utils.Constants;

import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * Created by Alex(Dragonkk)
 * 23/10/11
 */
public final class MainFile {

    private int id;

    private RandomAccessFile data;

    private RandomAccessFile index;

    private byte[] readCachedBuffer;

    private boolean newProtocol;

    protected MainFile(int id, RandomAccessFile data, RandomAccessFile index, byte[] readCachedBuffer, boolean newProtocol) throws IOException {
        this.id = id;
        this.data = data;
        this.index = index;
        this.readCachedBuffer = readCachedBuffer;
        this.newProtocol = newProtocol;
    }

    public Archive getArchive(int id) {
        return getArchive(id, null);
    }

    public Archive getArchive(int id, int[] keys) {
        byte[] data = getArchiveData(id);
        if (data == null) {
            return null;
        }
        return new Archive(id, data, keys);
    }

    public byte[] getArchiveData(int id) {
        synchronized (data) {
            try {
                if (index.length() < (6 * id + 6)) {
                    return null;
                }
                index.seek(6 * id);
                index.read(readCachedBuffer, 0, 6);
                int archiveLength = (readCachedBuffer[2] & 0xff) + (((0xff & readCachedBuffer[0]) << 16) + (readCachedBuffer[1] << 8 & 0xff00));
                int sector = ((readCachedBuffer[3] & 0xff) << 16) - (-(0xff00 & readCachedBuffer[4] << 8) - (readCachedBuffer[5] & 0xff));
                if (archiveLength < 0 || archiveLength > Constants.MAX_VALID_ARCHIVE_LENGTH) {
                    return null;
                }
                if (sector <= 0 || data.length() / 520L < sector) {
                    return null;
                }
                byte archive[] = new byte[archiveLength];
                int readBytesCount = 0;
                int part = 0;
                while (archiveLength > readBytesCount) {
                    if (sector == 0) {
                        return null;
                    }
                    data.seek(520 * sector);
                    int dataBlockSize = archiveLength - readBytesCount;
                    byte headerSize;
                    int currentIndex;
                    int currentPart;
                    int nextSector;
                    int currentArchive;
                    if ('\uffff' < id && newProtocol) {
                        headerSize = 10;
                        if (dataBlockSize > 510) {
                            dataBlockSize = 510;
                        }
                        data.read(readCachedBuffer, 0, headerSize + dataBlockSize);
                        currentArchive = ((readCachedBuffer[1] & 255) << 16) + ((readCachedBuffer[0] & 255) << 24) + (('\uff00' & readCachedBuffer[2] << 8) - -(readCachedBuffer[3] & 255));
                        currentPart = ((readCachedBuffer[4] & 0xff) << 8) + (0xff & readCachedBuffer[5]);
                        nextSector = (readCachedBuffer[8] & 0xff) + (0xff00 & readCachedBuffer[7] << 8) + ((0xff & readCachedBuffer[6]) << 16);
                        currentIndex = readCachedBuffer[9] & 0xff;
                    } else {
                        headerSize = 8;
                        if (dataBlockSize > 512) {
                            dataBlockSize = 512;
                        }
                        data.read(readCachedBuffer, 0, headerSize + dataBlockSize);
                        currentArchive = (0xff & readCachedBuffer[1]) + (0xff00 & readCachedBuffer[0] << 8);
                        currentPart = ((readCachedBuffer[2] & 0xff) << 8) + (0xff & readCachedBuffer[3]);
                        nextSector = (readCachedBuffer[6] & 0xff) + (0xff00 & readCachedBuffer[5] << 8) + ((0xff & readCachedBuffer[4]) << 16);
                        currentIndex = readCachedBuffer[7] & 0xff;
                    }
                    if ((newProtocol && id != currentArchive) || currentPart != part || this.id != currentIndex) {
                        return null;
                    }
                    if (nextSector < 0 || (data.length() / 520L) < nextSector) {
                        return null;
                    }
                    for (int index = headerSize; dataBlockSize + headerSize > index; index++) {
                        archive[readBytesCount++] = readCachedBuffer[index];
                    }
                    part++;
                    sector = nextSector;
                }
                return archive;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public boolean putArchive(Archive archive) {
        return putArchiveData(archive.getId(), archive.getData());
    }

    public boolean putArchiveData(int id, byte[] archive) {
        boolean done = putArchiveData(id, archive, true);
        if (!done) {
            done = putArchiveData(id, archive, false);
        }
        return done;
    }

    public boolean putArchiveData(int id, byte[] archive, boolean exists) {

        synchronized (data) {
            try {
                int sector;
                if (!exists) {
                    sector = (int) ((data.length() + 519L) / 520L);
                    if (sector == 0) {
                        sector = 1;
                    }
                } else {
                    if ((6 * id + 6) > index.length()) {
                        return false;
                    }
                    index.seek(id * 6);
                    index.read(readCachedBuffer, 0, 6);
                    sector = (readCachedBuffer[5] & 0xff) + (((readCachedBuffer[4] & 0xff) << 8) + (readCachedBuffer[3] << 16 & 0xff0000));
                    if (sector <= 0 || sector > data.length() / 520L) {
                        return false;
                    }
                }
                readCachedBuffer[1] = (byte) (archive.length >> 8);
                readCachedBuffer[3] = (byte) (sector >> 16);
                readCachedBuffer[2] = (byte) archive.length;
                readCachedBuffer[0] = (byte) (archive.length >> 16);
                readCachedBuffer[4] = (byte) (sector >> 8);
                readCachedBuffer[5] = (byte) sector;
                index.seek(id * 6);
                index.write(readCachedBuffer, 0, 6);
                int dataWritten = 0;
                for (int part = 0; dataWritten < archive.length; part++) {
                    int nextSector = 0;
                    if (exists) {
                        data.seek(sector * 520);
                        data.read(readCachedBuffer, 0, 8);
                        int currentContainerId = (0xff & readCachedBuffer[1]) + (0xff00 & readCachedBuffer[0] << 8);
                        int currentPart = (0xff & readCachedBuffer[3]) + (0xff00 & readCachedBuffer[2] << 8);
                        nextSector = ((0xff & readCachedBuffer[4]) << 16) + (((0xff & readCachedBuffer[5]) << 8) + (0xff & readCachedBuffer[6]));
                        int currentIndexFileId = readCachedBuffer[7] & 0xff;
                        if (currentContainerId != id || part != currentPart || this.id != currentIndexFileId) {
                            return false;
                        }
                        if (nextSector < 0 || data.length() / 520L < nextSector) {
                            return false;
                        }
                    }
                    if (nextSector == 0) {
                        exists = false;
                        nextSector = (int) ((data.length() + 519L) / 520L);
                        if (nextSector == 0) {
                            nextSector++;
                        }
                        if (nextSector == sector) {
                            nextSector++;
                        }
                    }
                    readCachedBuffer[3] = (byte) part;
                    if (archive.length - dataWritten <= 512) {
                        nextSector = 0;
                    }
                    readCachedBuffer[0] = (byte) (id >> 8);
                    readCachedBuffer[1] = (byte) id;
                    readCachedBuffer[2] = (byte) (part >> 8);
                    readCachedBuffer[7] = (byte) this.id;
                    readCachedBuffer[4] = (byte) (nextSector >> 16);
                    readCachedBuffer[5] = (byte) (nextSector >> 8);
                    readCachedBuffer[6] = (byte) nextSector;
                    data.seek(sector * 520);
                    data.write(readCachedBuffer, 0, 8);
                    int dataToWrite = archive.length - dataWritten;
                    if (dataToWrite > 512) {
                        dataToWrite = 512;
                    }
                    data.write(archive, dataWritten, dataToWrite);
                    dataWritten += dataToWrite;
                    sector = nextSector;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getArchivesCount() throws IOException {
        synchronized (index) {
            return (int) (index.length() / 6);
        }
    }
}
