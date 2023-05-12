package view.frame;

import lombok.AllArgsConstructor;
import java.io.File;
import javax.swing.filechooser.FileFilter;

@AllArgsConstructor
public class ExtensionFilter extends FileFilter {
    private final String extension, description;

    @Override
    public boolean accept(File file) {
        return file.isDirectory() || file.getName().toLowerCase().endsWith("." + extension.toLowerCase());
    }

    @Override
    public String getDescription() {
        return description + " (*." + extension + ")";
    }
}