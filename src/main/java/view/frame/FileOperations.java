package view.frame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Objects;

import static java.net.URLDecoder.decode;
import static java.nio.charset.Charset.defaultCharset;
import static javax.swing.JFileChooser.APPROVE_OPTION;

public class FileOperations {
    private static File dataDirectory = null;

    public static File getDataDirectory() {
        if (dataDirectory == null) {
            String path = decode(Frame.class.getProtectionDomain().getCodeSource().getLocation().getFile(), defaultCharset());
            dataDirectory = new File(path).getParentFile();
            if (dataDirectory == null || !dataDirectory.exists()) dataDirectory = new File(".");
            for (File file: Objects.requireNonNull(dataDirectory.listFiles())) {
                if (file.isDirectory() && file.getName().endsWith("_Data")) {
                    dataDirectory = file;
                    break;
                }
            }
        }
        return dataDirectory;
    }

    public static File getFileName(JFrame parent, String extension, String description, String option) {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter fileFilter = new ExtensionFilter(extension, description);
        fileChooser.addChoosableFileFilter(fileFilter);
        fileChooser.setCurrentDirectory(getDataDirectory());
        if (action(option, fileChooser, parent)) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getParent(), file.getName() + "." + extension);
            }
            return file;
        }
        return null;
    }

    private static boolean action(String option, JFileChooser fileChooser, JFrame parent) {
        if (Objects.equals(option, "save")) {
            return fileChooser.showSaveDialog(parent) == APPROVE_OPTION;
        }
        if (Objects.equals(option, "open")) {
            return fileChooser.showOpenDialog(parent) == APPROVE_OPTION;
        }
        return false;
    }
}

