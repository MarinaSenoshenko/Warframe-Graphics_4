package view.editor;

import main.State;
import view.view3d.View3d;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;

public class EditorView extends JPanel {
    private final View view;
    private final View3d view3d;
    private final Menu menu;

    public EditorView(View3d view3d, State state) {
        this.view3d = view3d;
        setLayout(new GridBagLayout());
        view = new View(state);
        menu = new Menu(view3d, view, state);
        view.setMenu(menu);
        add(view, gridBagConstraintsInitializer(0.9f, 0));
        add(menu, gridBagConstraintsInitializer(0.1f, 1));
        setVisible(true);
    }

    private GridBagConstraints gridBagConstraintsInitializer(double weight, int gridY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = BOTH;
        constraints.gridwidth = REMAINDER;
        constraints.weightx = weight;
        constraints.weighty = weight;
        constraints.gridy = gridY;
        return constraints;
    }

    public void setState(State state) {
        view.setState(state);
        menu.setState(view.getState());
        view3d.setState(view.getState());
    }

    public State getState() {
        return view.getState();
    }

    public void OnClose() {
        menu.onClose();
    }
}