package me.wphillips.fsmedit;

import java.io.*;

/**
 * Utility class for saving and loading {@link GraphModel} objects.
 */
public final class GraphIO {
    private GraphIO() {}

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
