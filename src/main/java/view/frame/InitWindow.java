package view.frame;

import lombok.Setter;
import main.Main;
import main.State;
import view.editor.EditorView;
import view.view3d.View3d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.*;
import static java.nio.file.Files.readString;
import static java.nio.file.Paths.get;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

@Setter
public class InitWindow extends Frame {
	private EditorView editorView;
	private String aboutText;
	private final View3d view3d;

	public InitWindow() {
		super(600, 400, "Wireframe");
		setIconImage(new ImageIcon("src/main/resources/spline.png").getImage());
		try {
			aboutText = readString(get("src/main/resources/about.txt"));
		} catch (IOException e) {
			Main.log.error(e.getMessage());
		}
		try {
			addSubMenu("File",VK_F);
			addMenuItem("File/Open", "open figure", VK_A, "open.png", "onLoad");
			addMenuItem("File/Save", "save figure",VK_X, "save.png", "onSave");
			addSubMenu("Scene", VK_H);
			addSubMenu("Help", VK_H);
			addMenuItem("Scene/Spline", "open spline menu", VK_A, "spline.png", "onParameters");
			addMenuItem("Scene/Reset", "reset turn", VK_A, "reset.png", "onReset");
			addMenuItem("Help/About", "about program", VK_A, "about.png", "onAbout");
			addToolBarButton("File/Open");
			addToolBarButton("File/Save");
			addToolBarSeparator();
			addToolBarButton("Scene/Spline");
			addToolBarButton("Scene/Reset");
			addToolBarSeparator();
			addToolBarButton("Help/About");
		} catch (Exception e) {
			Main.log.error(e.getMessage());
			System.exit(1);
		}
		State state = State.createInitialState();
		view3d = new View3d(state);
		editorView = new EditorView(view3d, state);
		view3d.setState(editorView.getState());
		add(view3d);
		onParameters();
		pack();
	}

	public void saveStateInFile(State state, File file) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(state);
		objectOutputStream.flush();
		objectOutputStream.close();
	}

	public void onParameters() {
		JDialog dialog = new JDialog(this, "Set parameters", true);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				editorView.OnClose();
			}
		});
		Dimension screenSize = getDefaultToolkit().getScreenSize();
		editorView = new EditorView(view3d, editorView.getState());
		dialog.add(editorView);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setBounds((int)(screenSize.getWidth() / 2.0 - dialog.getWidth() / 2.0),
				(int)(screenSize.getHeight() / 2.0 - dialog.getHeight() / 2.0), dialog.getWidth(), dialog.getHeight());
		dialog.setVisible(true);
	}

	private State readState(File file) {
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		State state = null;
		try {
			fileInputStream = new FileInputStream(file);
			objectInputStream = new ObjectInputStream(fileInputStream);
			state = (State)objectInputStream.readObject();
		} catch (Exception e) {
			Main.log.error("Some problem in file read");
		}
		finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
				if (objectInputStream != null) {
					objectInputStream.close();
				}
			} catch (IOException e) {
				Main.log.error("File stream operations exception");
			}
		}
		return state;
	}

	@SuppressWarnings("unused")
	public void onAbout() {
		showMessageDialog(this, aboutText, "About", INFORMATION_MESSAGE);
	}

	@SuppressWarnings("unused")
	public void onReset() {
		view3d.resetTurn();
	}

	@SuppressWarnings("unused")
	public void onSave() {
		File file = FileOperations.getFileName(this,"figure", "state", "save");
		if (file == null) {
			Main.log.error("No file present");
			return;
		}
		try {
			saveStateInFile(editorView.getState(), file);
		} catch (IOException e) {
			Main.log.error("Some problem in save file");
		}
		remove(editorView);
	}

    @SuppressWarnings("unused")
	public void onLoad() {
		File file = FileOperations.getFileName(this,"figure", "state", "open");
		if (file == null) {
			Main.log.error("No file present");
			return;
		}
		editorView.setState(readState(file));
		view3d.setState(editorView.getState());
	}
}
