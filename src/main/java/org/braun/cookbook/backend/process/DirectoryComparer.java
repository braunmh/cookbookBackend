package org.braun.cookbook.backend.process;

import java.io.*;
import java.util.*;

/**
 *
 * @author mbraun
 */
public class DirectoryComparer implements Closeable {

    private String fileName;
    private int fileNameLength;
    private boolean incremental;
    private FileFilter filter;
    private Entry newEntry, oldEntry, entry;
    private EntryReader oldValues;
    private EntryReader newValues;

    private File ov, nv;

    EntryStatus lastCompare;

    public DirectoryComparer(String fileName, FileFilter filter) throws IOException {
        this(fileName, filter, true);
        open();
    }

    public DirectoryComparer(String fileName, FileFilter filter, boolean incremental) {
        this.fileName = fileName;
        File file = new File(fileName);
        this.fileNameLength = file.getPath().length();
        this.filter = filter;
        lastCompare = EntryStatus.equals;
        this.incremental = incremental;
    }

    public boolean hasNext() throws IOException {
        if (newValues.eof && oldValues.eof) {
            return false;
        }
        if (newValues.eof) {
            entry = oldValues.getNextEntry();
            if (oldValues.eof) {
                return false;
            }
            entry.setOperation(Operation.remove);
            return true;
        }
        if (oldValues.eof) {
            entry = newValues.getNextEntry();
            if (newValues.eof) {
                return false;
            }
            entry.setOperation(Operation.persist);
            return true;
        }
        while (!newValues.eof && !oldValues.eof) {
            switch (lastCompare) {
                case modified, equals -> {
                    newEntry = newValues.getNextEntry();
                    oldEntry = oldValues.getNextEntry();
                }
                case less -> newEntry = newValues.getNextEntry();
                case greater -> oldEntry = oldValues.getNextEntry();
            }
            if (newValues.eof && oldValues.eof) {
                return false;
            }
            if (newValues.eof) {
                entry = setEntry(oldEntry, Operation.remove);
                return true;
            }
            if (oldValues.eof) {
                entry = setEntry(newEntry, Operation.persist);
                return true;
            }
            lastCompare = newEntry.status(oldEntry);
            if (lastCompare != EntryStatus.equals) {
                break;
            }
        }
        if (newValues.eof && oldValues.eof) {
            return false;
        }
        switch (lastCompare) {
            case greater -> entry = setEntry(oldEntry, Operation.remove);
            case less -> entry = setEntry(newEntry, Operation.persist);
            case modified -> entry = setEntry(newEntry, Operation.merge);
        }
        return entry != null;
    }

    public Entry next() {
        return entry;
    }

    private Entry setEntry(Entry entry, Operation operation) {
        entry.setOperation(operation);
        return entry;
    }

    private void open() throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
        ov = new File(file, "directoryComparer.dc");

        nv = new File(file, "directoryComparer.tmp");
        if (ov.exists() && incremental) {
            oldValues = new EntryReader(ov);
        } else {
            oldValues = new EntryReader(null);
        }
        readNewValues(nv);
        newValues = new EntryReader(nv);
    }

    private void readNewValues(File outputFile) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));) {
            readNewValues(outputFile.getParentFile(), bw);
        }
    }

    private void readNewValues(File directory, Writer writer) throws IOException {
        File[] files = directory.listFiles(filter);
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                readNewValues(file, writer);
            } else {
                writer.write(file.getPath().substring(fileNameLength + 1) + "\t" + file.lastModified() + "\n");
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (oldValues != null) {
            oldValues.close();
        }
        newValues.close();

        if (ov.exists()) {
            ov.delete();
        }
        nv.renameTo(ov);
    }

    public class Entry {

        String name;
        long modified;
        Operation operation;

        public Entry(String line) {
            int index = line.indexOf('\t');
            name = line.substring(0, index);
            modified = getLong(line.substring(index + 1));
        }

        public Entry(String name, long modified) {
            this.name = name;
            this.modified = modified;
        }

        public String getName() {
            return name;
        }

        public long getModified() {
            return modified;
        }

        public Operation getOperation() {
            return operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        private long getLong(String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return 0l;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return this.modified == other.modified;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 97 * hash + (int) (this.modified ^ (this.modified >>> 32));
            return hash;
        }

        public EntryStatus status(Entry entry) {
            if (entry == null) {
                return EntryStatus.modified;
            }
            int c = name.compareTo(entry.name);
            if (c > 0) {
                return EntryStatus.greater;
            }
            if (c < 0) {
                return EntryStatus.less;
            }
            if (modified == entry.modified) {
                return EntryStatus.equals;
            }
            return EntryStatus.modified;
        }

        @Override
        public String toString() {
            return String.format("name=%s, modified=%s, operation=%s", name, modified, operation);
        }

    }

    public enum Operation {
        persist, merge, remove;
    }

    public enum EntryStatus {
        equals, less, greater, modified;
    }

    class EntryReader implements Closeable {

        BufferedReader reader;
        boolean eof;

        public EntryReader(File file) throws IOException {
            if (file == null) {
                eof = true;
                return;
            }
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            eof = false;
        }

        @Override
        public void close() throws IOException {
            if (reader != null) {
                reader.close();
            }
        }

        public Entry getNextEntry() throws IOException {
            if (eof) {
                return null;
            }
            String line = reader.readLine();
            if (line == null) {
                eof = true;
                return null;
            }
            return new Entry(line);
        }
    }
}
