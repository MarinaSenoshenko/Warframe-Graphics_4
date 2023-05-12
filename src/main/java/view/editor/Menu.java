package view.editor;

import lombok.AllArgsConstructor;
import main.Main;
import main.State;
import view.view3d.View3d;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.lang.Long.*;

public class Menu extends JPanel {
    private boolean defaultClose = false;
    private State state, newState;
    private final View view;
    private final View3d view3d;
    private final JSpinner spinnerN, spinnerM, spinnerPointNumber, spinnerCircleN,
            spinnerX, spinnerY/*, spinnerCircleM*/;

    public Menu(View3d view3d, View view, State state) {
        setPreferredSize(new Dimension(1250, 50));
        this.view3d = view3d;
        this.state = state;
        newState = new State(state);
        this.view = view;
        view.setState(newState);
        setLayout(new GridBagLayout());

        spinnerN = createSpinner(state.getN(), 2, 100);
        spinnerM = createSpinner(state.getM(), 1, 100);
        spinnerCircleN = createSpinner(state.getCircleN(), 1, Integer.MAX_VALUE);
        spinnerX = createDoubleSpinner(view.getSelectedX());
        spinnerY = createDoubleSpinner(view.getSelectedY());
        spinnerPointNumber = createSpinner(view.getSelectedNumber(), 0, Integer.MAX_VALUE);

        JButton applyButton = new JButton("apply");
        JButton autoScaleButton = new JButton("auto scale");
        JButton plusButton = new JButton(new ImageIcon("src/main/resources/plus.png"));
        JButton minusButton = new JButton(new ImageIcon("src/main/resources/minus.png"));

        spinnerX.addChangeListener(e -> view.setX((Double)spinnerX.getValue()));
        spinnerY.addChangeListener(e -> view.setY((Double)spinnerY.getValue()));
        spinnerN.addChangeListener(e -> {
            this.newState.setN(((int)(spinnerN.getValue())));
            view.repaint();
        });
        spinnerM.addChangeListener(e -> {
            this.newState.setM(((int)(spinnerM.getValue())));
            view.repaint();
        });
        spinnerPointNumber.addChangeListener(e -> {
            if ((int)(spinnerPointNumber.getValue()) >= this.newState.getPoints().size()) {
                spinnerPointNumber.setValue(this.newState.getPoints().size() - 1);
            }
            view.setSelectedPoint((int)(spinnerPointNumber.getValue()));
            view.repaint();
        });

        autoScaleButton.addActionListener(e -> view.calcBestScale());
        minusButton.addActionListener(e -> view.getPoint().removeSelectedPoint(view));
        plusButton.addActionListener(e -> view.getPoint().addPointAfterSelected());
        applyButton.addActionListener(e -> {
            applyParams();
            JDialog frame = (JDialog)this.getRootPane().getParent();
            defaultClose = true;
            frame.dispose();
        });

        Constructor constructor = new Constructor(this, 0, 0);

        List.of(new JLabel("n ="), spinnerN, new JLabel("m ="), spinnerM, /*new JLabel("circle m ="), spinnerCircleM,*/
                new JLabel("circle n ="), spinnerCircleN, new JLabel("x ="), spinnerX, new JLabel("y ="), spinnerY,
                new JLabel("selected point ="), spinnerPointNumber, plusButton, minusButton, autoScaleButton,
                applyButton).forEach(constructor::addOnRow);
    }

    private JSpinner createDoubleSpinner(double parameter) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(parameter, MIN_VALUE, MAX_VALUE, 1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.0##E0");
        spinner.setEditor(editor);
        JFormattedTextField textField = editor.getTextField();
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateValue();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateValue();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateValue();
            }
            private void updateValue() {
                try {
                    double value = Double.parseDouble(textField.getText());
                    if (value < MIN_VALUE) {
                        value = MIN_VALUE;
                    } else if (value > MAX_VALUE) {
                        value = MAX_VALUE;
                    }
                    spinner.setValue(value);
                } catch (NumberFormatException ex) {
                    Main.log.error("Some problem in number format");
                }
            }
        });
        spinner.setValue(parameter);
        return spinner;
    }

    private JSpinner createSpinner(int parameter, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(parameter, min, max, 1));
        spinner.addChangeListener(e -> {
            if ((int)spinner.getValue() < min) {
                spinner.setValue(min);
            } else if ((int)spinner.getValue() > max) {
                spinner.setValue(max);
            }
        });
        spinner.setValue(parameter);
        return spinner;
    }

    private void applyParams() {
        System.out.println(state.getSplinePoints().size());
        newState.setCircleM(state.getSplinePoints().size());
        newState.setCircleN((Integer)spinnerCircleN.getValue());
        newState.setM((Integer)spinnerM.getValue());
        newState.setN((Integer)spinnerN.getValue());
        state = newState;
        newState = new State(state);
        view3d.setState(state);
        view.setState(state);
    }

    private void resetParams() {
        view.setState(state);
        newState = new State(state);
        view3d.setState(state);
        spinnerN.setValue(state.getN());
        spinnerM.setValue(state.getM());
        spinnerCircleN.setValue(state.getCircleN());
        spinnerPointNumber.setValue(0);
    }

    public void setState(State state) {
        this.state = state;
        resetParams();
    }

    public void onClose() {
        if (!defaultClose) {
            resetParams();
        }
    }

    public void setSelectedPoint(int number) {
        spinnerPointNumber.setValue(number);
    }

    public void setX(double x) {
        spinnerX.setValue(x);
    }

    public void setY(double y) {
        spinnerY.setValue(y);
    }

    @AllArgsConstructor
    private static class Constructor {
        private final JPanel panel;
        private int xCursor;
        private int yCursor;

        private GridBagConstraints getConstraints(int x, int y) {
            GridBagConstraints constraints = new GridBagConstraints();
            getConstraints(x, y, constraints);
            return constraints;
        }

        private GridBagConstraints getConstraints(int x, int y, GridBagConstraints constraints) {
            constraints.anchor = FIRST_LINE_START;
            constraints.fill = BOTH;
            constraints.insets = new Insets(0, 5, 5, 0);
            constraints.gridx = x;
            constraints.gridy = y;
            return constraints;
        }

        public void addOnRow(Component component, int ipadx) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.ipadx = ipadx;
            addOnRow(component, constraints);
        }

        public void addOnRow(Component component) {
            panel.add(component, getConstraints(xCursor++, yCursor));
        }

        public void addOnRow(Component component, GridBagConstraints constraints) {
            panel.add(component, getConstraints(xCursor++, yCursor, constraints));
        }
    }
}
