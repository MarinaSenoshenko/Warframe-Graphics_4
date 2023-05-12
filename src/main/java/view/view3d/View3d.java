package view.view3d;

import lombok.Getter;
import lombok.Setter;
import main.State;
import matrix.AbstractMatrix;
import matrix.Matrix;
import matrix.point.FigurePoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.*;
import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.lang.Math.*;
import static javax.swing.SwingUtilities.*;
import static matrix.AbstractMatrix.*;

@Getter
@Setter
public class View3d extends JPanel {
    private State state;
    private Matrix sumMatrix, sumFirstMatrix, turnXMatrix, turnZMatrix, turnYMatrix;
    private int prevX = -1, prevY = -1;
    private double zoom = 10;

    public View3d(State state) {
        this.state = state;
        setPreferredSize(new Dimension(700, 700));

        addMouseWheelListener(e -> {
            double proximityKoef = pow(1.1, -e.getWheelRotation());
            this.state.setZf(this.state.getZf() * proximityKoef);
            this.state.setZb(this.state.getZb() * proximityKoef);
            repaint();
        });

        addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }
            public void mouseClicked(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                int vectorDx = 0, vectorDy = 0, vectorDz = 0;
                if (isLeftMouseButton(e)) {
                    vectorDx = e.getX() - prevX;
                    vectorDy = e.getY() - prevY;
                }
                turnXMatrix = AbstractMatrix.getRxMatrix(toRadians(vectorDy) / 2);
                turnYMatrix = AbstractMatrix.getRyMatrix(toRadians(-vectorDx) / 2);
                turnZMatrix = AbstractMatrix.getRzMatrix(toRadians(vectorDz) / 2);
                sumMatrix = turnXMatrix.mul(turnYMatrix).mul(turnZMatrix).mul(sumMatrix);
                prevX = e.getX();
                prevY = e.getY();
                repaint();
            }
            public void mouseMoved(MouseEvent e) {}
        });
        resetTurn();
    }

    public void resetTurn() {
        sumFirstMatrix = getRxMatrix(0);
        turnXMatrix = getRxMatrix(toRadians(150));
        turnYMatrix = getRyMatrix(toRadians(150));
        turnZMatrix = getRzMatrix(toRadians(150));
        sumMatrix = turnXMatrix.mul(turnYMatrix).mul(turnZMatrix).mul(sumFirstMatrix);
        repaint();
    }

    private void setCoordsAndDrawLine(Graphics2D g2d, FigurePoint splineBeginPoint, FigurePoint splineEndPoint) {
        int beginPointCordX = (int)splineBeginPoint.getMatrix()[0][0];
        int beginPointCordY = (int)splineBeginPoint.getMatrix()[1][0];
        int endPointCordX = (int)splineEndPoint.getMatrix()[0][0];
        int endPointCordY = (int)splineEndPoint.getMatrix()[1][0];
        g2d.drawLine(beginPointCordX + getWidth() / 2, beginPointCordY + getHeight() / 2,
                endPointCordX + getWidth() / 2, endPointCordY + getHeight() / 2);
    }

    private void addWirePointsToCircles(Matrix RMatrix, List<List<FigurePoint>> circles) {
        double wiresBetweenCircles = (double)state.getSplinePoints().size() / state.getCircleM();
        for (int i = 0; i < circles.size(); i++) {
            int sn = (i == circles.size() - 1) ? state.getSplinePoints().size() - 1 :
                    (int)round(i * wiresBetweenCircles);
            FigurePoint splinePoint = state.getSplinePoints().get(sn);
            splinePoint = RMatrix.mul(splinePoint);
            splinePoint.normalizeByLastPoint();
            circles.get(i).add(splinePoint);
        }
    }

    public void setState(State state) {
        this.state = state;
        repaint();
    }

    private void paintCircles(Graphics2D g2d, Matrix transformMatrix) {
        double degreesBetweenWires = 360.0 / (state.getCircleN() * state.getM());
        List<List<FigurePoint>> circles = new ArrayList<>();
        for (int i = 0; i < state.getCircleM(); i++) {
            List<FigurePoint> circle = new ArrayList<>();
            circles.add(circle);
        }
        addWirePointsToCircles(transformMatrix, circles);
        for (int i = 1; i < state.getCircleN() * state.getM(); ++i) {
            Matrix RzMatrix = getRzMatrix(toRadians(i * degreesBetweenWires));
            Matrix RMatrix = transformMatrix.mul(RzMatrix);
            addWirePointsToCircles(RMatrix, circles);
        }
        for (List<FigurePoint> circle : circles) {
            for (int j = 0; j < circle.size(); j++) {
                setCoordsAndDrawLine(g2d, circle.get(j), circle.get((j + 1) % (circle.size())));
            }
        }
    }

    private void paintSpinWiresWithCircles(Graphics2D g2d, Matrix transformMatrix) {
        FigurePoint resultSplinePoint1, resultSplinePoint2;
        int m = state.getM(), splinePointsCount = state.getSplinePoints().size() - 1;
        double degreesBetweenSplines = 360.0 / m;
        for (int j = 0; j < m; j++) {
            Matrix RzMatrix = getRzMatrix(toRadians(j * degreesBetweenSplines));
            Matrix RMatrix = transformMatrix.mul(RzMatrix);
            for (int i = 0; i < splinePointsCount; i++) {
                resultSplinePoint1 = RMatrix.mul(state.getSplinePoints().get(i));
                resultSplinePoint2 = RMatrix.mul(state.getSplinePoints().get(i + 1));
                resultSplinePoint1.normalizeByLastPoint();
                resultSplinePoint2.normalizeByLastPoint();
                setCoordsAndDrawLine(g2d, resultSplinePoint1, resultSplinePoint2);
            }
        }
        paintCircles(g2d, transformMatrix);
    }

    private Matrix getTransformMatrix() {
        return Matrix.getPerspectiveProjectionMatrix(state.getSw(), state.getSh(), state.getZf(), state.getZb())
                .mul(AbstractMatrix.getCameraMatrix(zoom)).mul(sumMatrix);
    }

    private void setSize() {
        double maxX = 0, maxY = 0;
        for (FigurePoint point : state.getSplinePoints()) {
            maxX = max(maxX, abs(point.getX()));
            maxY = max(maxY, abs(point.getY()));
        }
        zoom = 7 * max(maxX, maxY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), TYPE_3BYTE_BGR);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
        setSize();
        g2d.setColor(BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(CYAN);
        paintSpinWiresWithCircles(g2d, getTransformMatrix());
        g.drawImage(image, 0, 0, null);
    }
}
