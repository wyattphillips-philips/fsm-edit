package me.wphillips.fsmedit;

import java.io.*;

/**
 * Utility class for saving and loading {@link GraphModel} objects.
 */
public final class GraphIO {
    private GraphIO() {}

    /** Default file extension for graph files. */
    public static final String EXTENSION = "fsm";

    /**
     * Ensure the provided file has the {@link #EXTENSION} suffix.
     *
     * @param file original file selected by the user
     * @return file guaranteed to end with "." + {@link #EXTENSION}
     */
    public static File withExtension(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith("." + EXTENSION)) {
            return new File(file.getParentFile(), file.getName() + "." + EXTENSION);
        }
        return file;
    }

    public static void save(File file, GraphModel model) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(model);
        }
    }

    public static GraphModel load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (GraphModel) in.readObject();
        }
    }
}
