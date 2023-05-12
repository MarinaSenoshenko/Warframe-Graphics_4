package view.editor;

import lombok.*;
import main.*;
import matrix.Matrix;
import matrix.point.FigurePoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import static java.awt.Color.*;
import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.lang.Math.*;


@Setter
@Getter
public class View extends JPanel {
    private State state;
    private Menu menu;
    private Point point;
    private CordConverter cordConverter;
    private final int pointSize = 16;
    private int selectedPoint, movingPoint;
    private List<FigurePoint> subPoints = new LinkedList<>();

    public View(State state) {
        this.state = state;
        this.point = new Point();
        setPreferredSize(new Dimension(1200, 600));
        cordConverter = new CordConverter(getWidth() / 2, getHeight() / 2, 16);
        point.generateSubPoint();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                cordConverter = new CordConverter(getWidth() / 2, getHeight() / 2,
                        cordConverter.getScale());
                repaint();
            }
        });
        View view = this;

        addMouseWheelListener(e -> {
            if (e.getUnitsToScroll() > 0) {
                cordConverter.setScale(cordConverter.getScale() / 2);
            } else if (e.getUnitsToScroll() < 0) {
                cordConverter.setScale(cordConverter.getScale() * 2);
            }
            repaint();
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (movingPoint != -1) {
                    if (e.getX() < 0 || e.getX() > getWidth() - 1 || e.getY() < 0 || e.getY() > getHeight() - 1) {
                        return;
                    }
                    view.state.getPoints().get(movingPoint).setX(cordConverter.convertFromPixelToCordX(e.getX()));
                    view.state.getPoints().get(movingPoint).setY(cordConverter.convertFromPixelToCordY(e.getY()));
                    point.setSelectedPointInfoToMenu();
                    point.generateSubPoint();
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                for (int i = 0; i < view.subPoints.size(); i++) {
                    FigurePoint subPoint = view.subPoints.get(i);
                    int pointX = cordConverter.convertFromCordXToPixel(subPoint.getX());
                    int pointY = cordConverter.convertFromCordYToPixel(subPoint.getY());
                    int size = pointSize / 2;
                    if ((pointX > x - size && pointX < x + size) && (pointY > y - size && pointY < y + size)) {
                        point.createPointAfterLast(x, y, i + 1);
                        return;
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (view.state.getPoints().size() < 5) {
                        JOptionPane.showMessageDialog(view, "Points number should be more than 3",
                                "Points number", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    for (int i = 0; i < view.state.getPoints().size(); i++) {
                        FigurePoint point1 = view.state.getPoints().get(i);
                        int pointX = cordConverter.convertFromCordXToPixel(point1.getX());
                        int pointY = cordConverter.convertFromCordYToPixel(point1.getY());
                        int subPointSize = pointSize / 2;
                        if ((pointX > x - subPointSize && pointX < x + subPointSize) &&
                                (pointY > y - subPointSize && pointY < y + subPointSize)) {
                            view.state.getPoints().remove(point1);
                            point.resetPoint();
                            point.generateSubPoint();
                            point.setSelectedPointInfoToMenu();
                            repaint();
                            break;
                        }
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                point.findPoint(e.getX(), e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                point.resetPoint();
                repaint();
            }
        });
    }

    public void calcPointsToDraw() {
        Matrix matrix = new Matrix(
                new double[][]{
                {-1, 3, -3, 1},
                {3, -6, 3, 0},
                {-3, 0, 3, 0},
                {1, 4, 1, 0}
        }) {
            @Override
            public FigurePoint mul(FigurePoint operand){return null;}
            @Override
            public Matrix mul(Matrix operand) {
                return null;
            }
            @Override
            public Matrix mul(double k) {
                return null;
            }
        };
        state.setSplinePoints(new LinkedList<>());
        int pointsCount = state.getPoints().size();
        for (int i = 3; i < pointsCount; i++) {
            Matrix matrixGX = new Matrix(4, 1, new double[]{
                    state.getPoints().get(i - 3).getX(),
                    state.getPoints().get(i - 2).getX(),
                    state.getPoints().get(i - 1).getX(),
                    state.getPoints().get(i).getX()
            });
            Matrix matrixGY = new Matrix(4, 1, new double[]{
                    state.getPoints().get(i - 3).getY(),
                    state.getPoints().get(i - 2).getY(),
                    state.getPoints().get(i - 1).getY(),
                    state.getPoints().get(i).getY()
            });
            double dt = 1.0 / state.getN();
            for (int j = 0; j < state.getN() + 1; j++) {
                double t = j * dt;
                Matrix matrixT = new Matrix(1, 4, new double[]{pow(t, 3), pow(t, 2), pow(t, 1), 1.0});
                Matrix matrixTMulM = matrixT.mul(matrix);
                double pointToDrawX = matrixTMulM.mul(matrixGX).mul(1.0 / 6.0).getValue();
                double pointToDrawY = matrixTMulM.mul(matrixGY).mul(1.0 / 6.0).getValue();
                state.getSplinePoints().add(new FigurePoint(pointToDrawX, pointToDrawY, 0));
            }
        }
    }

    public void setX(double x) {
        if (selectedPoint >= state.getPoints().size()) {
            Main.log.error("Selected point out of range");
            return;
        }
        state.getPoints().get(selectedPoint).setX(x);
        repaint();
    }

    public void setY(double y) {
        if (selectedPoint >= state.getPoints().size()) {
            Main.log.error("Selected point out of range");
            return;
        }
        state.getPoints().get(selectedPoint).setY(y);
        repaint();
    }

    public void calcBestScale() {
        double maxX = 0, maxY = 0;
        for (FigurePoint point : state.getPoints()) {
            maxX = max(maxX, abs(point.getX()));
            maxY = max(maxY, abs(point.getY()));
        }
        double dx = maxX * 1.4, dy = maxY * 1.4;
        int height = getHeight(), width = getWidth();

        width = (width == 0) ? 800 : width;
        height = (height == 0) ? 600 : height;

        double scaleX = (double)width / 2 / dx;
        double scaleY = (double)height / 2 / dy;
        double newScale = min(scaleX, scaleY);
        cordConverter.setScale(newScale);
        repaint();
    }

    public void setState(State newState) {
        state = newState;
        calcBestScale();
        calcPointsToDraw();
    }

    public double getSelectedY() {
        if (selectedPoint >= state.getPoints().size() || selectedPoint < 0) {
            Main.log.error("Selected point y cord out of range");
            return -1;
        }
        return state.getPoints().get(selectedPoint).getY();
    }

    public double getSelectedX() {
        if (selectedPoint >= state.getPoints().size() || selectedPoint < 0) {
            Main.log.error("Selected point x cord out of range");
            return -1;
        }
        return state.getPoints().get(selectedPoint).getX();
    }

    public int getSelectedNumber() {
        return max(selectedPoint, 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        point.generateSubPoint();
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), TYPE_3BYTE_BGR);
        Graphics gi = image.getGraphics();
        ((Graphics2D)gi).setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        ((Graphics2D)gi).setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
        ((Graphics2D)gi).setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
        gi.setColor(BLACK);
        gi.fillRect(0, 0, image.getWidth(), image.getHeight());
        gi.setColor(WHITE);
        gi.drawLine(0, cordConverter.convertFromCordYToPixel(0), getWidth(),
                cordConverter.convertFromCordYToPixel(0));
        gi.drawLine(cordConverter.convertFromCordXToPixel(0), 0,
                cordConverter.convertFromCordXToPixel(0), getHeight());
        gi.setColor(WHITE);
        int shift = 5, x = 1, y = 1;
        while (x < getWidth() / 2) {
            gi.drawLine(cordConverter.convertFromCordXToPixel(x), getHeight() / 2 - shift,
                    cordConverter.convertFromCordXToPixel(x), getHeight() / 2 + shift);
            gi.drawLine(cordConverter.convertFromCordXToPixel(-x), getHeight() / 2 - shift,
                    cordConverter.convertFromCordXToPixel(-x), getHeight() / 2 + shift);
            x++;
        }
        while (y < getHeight() / 2) {
            gi.drawLine(getWidth() / 2 - shift, cordConverter.convertFromCordYToPixel(y),
                    getWidth() / 2 + shift, cordConverter.convertFromCordYToPixel(y));
            gi.drawLine(getWidth() / 2 - shift, cordConverter.convertFromCordYToPixel(-y),
                    getWidth() / 2 + shift, cordConverter.convertFromCordYToPixel(-y));
            y++;
        }
        calcPointsToDraw();
        gi.setColor(CYAN);
        for (int i = 0; i < state.getSplinePoints().size() - 1; i++) {
            FigurePoint splinePoint1 = state.getSplinePoints().get(i);
            FigurePoint splinePoint2 = state.getSplinePoints().get(i + 1);
            gi.drawLine(cordConverter.convertFromCordXToPixel(splinePoint1.getX()),
                    cordConverter.convertFromCordYToPixel(splinePoint1.getY()),
                    cordConverter.convertFromCordXToPixel(splinePoint2.getX()),
                    cordConverter.convertFromCordYToPixel(splinePoint2.getY()));
        }
        gi.setColor(GREEN);
        for (int i = 0; i < state.getPoints().size() - 1; i++) {
            FigurePoint point1 = state.getPoints().get(i);
            FigurePoint point2 = state.getPoints().get(i + 1);
            gi.drawLine(cordConverter.convertFromCordXToPixel(point1.getX()),
                    cordConverter.convertFromCordYToPixel(point1.getY()),
                    cordConverter.convertFromCordXToPixel(point2.getX()),
                    cordConverter.convertFromCordYToPixel(point2.getY()));
        }
        gi.setColor(RED);
        for (int i = 0; i < state.getPoints().size(); i++) {
            FigurePoint point1 = state.getPoints().get(i);
            point.drawPoint(image, cordConverter.convertFromCordXToPixel(point1.getX()),
                    cordConverter.convertFromCordYToPixel(point1.getY()), point1.getPointSize(), GREEN);
        }

        for (FigurePoint subPoint : subPoints) {
            point.drawPoint(image, cordConverter.convertFromCordXToPixel(subPoint.getX()),
                    cordConverter.convertFromCordYToPixel(subPoint.getY()), subPoint.getPointSize(), GREEN);
        }
        if (selectedPoint != -1 && selectedPoint < state.getPoints().size()) {
            FigurePoint point1 = state.getPoints().get(selectedPoint);
            point.drawPoint(image, cordConverter.convertFromCordXToPixel(point1.getX()),
                    cordConverter.convertFromCordYToPixel(point1.getY()), point1.getPointSize(), RED);
        }
        gi.setColor(BLUE);
        g.drawImage(image, 0, 0, null);
    }


    public class Point {

        public void addPointAfterSelected() {
            int pointToDrawX, pointToDrawY;
            if (selectedPoint >= subPoints.size()) {
                FigurePoint subPoint = subPoints.get(selectedPoint - 1);
                FigurePoint splinePoint = state.getPoints().get(selectedPoint);
                pointToDrawX = cordConverter.convertFromCordXToPixel(2 * splinePoint.getX() - subPoint.getX());
                pointToDrawY = cordConverter.convertFromCordYToPixel(2 * splinePoint.getY() - subPoint.getY());
            } else {
                FigurePoint subPoint = subPoints.get(selectedPoint);
                pointToDrawX = cordConverter.convertFromCordXToPixel(subPoint.getX());
                pointToDrawY = cordConverter.convertFromCordYToPixel(subPoint.getY());
            }
            createPointAfterLast(pointToDrawX, pointToDrawY, selectedPoint + 1);
            resetPoint();
            generateSubPoint();
            repaint();
        }

        public void removeSelectedPoint(View view) {
            if (state.getPoints().size() < 5) {
                JOptionPane.showMessageDialog(view, "Points number should be more than 3",
                        "Points number", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            state.getPoints().remove(selectedPoint);
            if (selectedPoint >= state.getPoints().size()) {
                setSelectedPoint(state.getPoints().size() - 1);
            }
            resetPoint();
            generateSubPoint();
            setSelectedPointInfoToMenu();
            repaint();
        }

        public void createPointAfterLast(int pointToDrawX, int pointToDrawY, int num) {
            state.getPoints().add(num, new FigurePoint(cordConverter.convertFromPixelToCordX(pointToDrawX),
                    cordConverter.convertFromPixelToCordY(pointToDrawY), pointSize));
            generateSubPoint();
        }

        public void resetPoint() {
            movingPoint = -1;
        }

        public void findPoint(int pixelX, int pixelY) {
            int pointXCord, pointYCord;
            for (int i = 0; i < state.getPoints().size(); i++) {
                FigurePoint splinePoint = state.getPoints().get(i);
                pointXCord = cordConverter.convertFromCordXToPixel(splinePoint.getX());
                pointYCord = cordConverter.convertFromCordYToPixel(splinePoint.getY());
                int size = splinePoint.getPointSize();
                if ((pointXCord > pixelX - size && pointXCord < pixelX + size / 2) &&
                        (pointYCord > pixelY - size / 2 && pointYCord < pixelY + size / 2)) {
                    selectedPoint = i;
                    movingPoint = i;
                    menu.setSelectedPoint(selectedPoint);
                    setSelectedPointInfoToMenu();
                    return;
                }
            }
            movingPoint = -1;
        }

        public void setSelectedPoint(int selectedPointNumber) {
            if (selectedPointNumber >= state.getPoints().size()) {
                Main.log.error("Selected point out of range");
                return;
            }
            selectedPoint = selectedPointNumber;
            setSelectedPointInfoToMenu();
            repaint();
        }

        public void setSelectedPointInfoToMenu() {
            if (selectedPoint >= state.getPoints().size()) {
                Main.log.error("Selected point out of range");
                return;
            }
            menu.setSelectedPoint(selectedPoint);
            menu.setX(state.getPoints().get(selectedPoint).getX());
            menu.setY(state.getPoints().get(selectedPoint).getY());
        }

        public void drawPoint(BufferedImage image, int x, int y, int size, Color color) {
            Graphics2D g2d = (Graphics2D)image.getGraphics();
            g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - size / 2, y - size / 2, size, size);
        }

        public void generateSubPoint() {
            subPoints = new LinkedList<>();
            for (int i = 0; i < state.getPoints().size() - 1; i++) {
                FigurePoint point1 = state.getPoints().get(i);
                FigurePoint point2 = state.getPoints().get(i + 1);
                FigurePoint subPoint = new FigurePoint((point2.getX() - point1.getX()) / 2 + point1.getX(),
                        (point2.getY() - point1.getY()) / 2 + point1.getY(), pointSize / 2);
                subPoints.add(subPoint);
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class CordConverter {
        private final int centerXPixel, centerYPixel;
        private double scale;

        public double convertFromPixelToCordX(int x) {
            return (x - centerXPixel) / scale;
        }
        public double convertFromPixelToCordY(int y) {
            return (y - centerYPixel) / scale;
        }
        public int convertFromCordXToPixel(double x) {
            return (int)round(x * scale + centerXPixel);
        }
        public int convertFromCordYToPixel(double y) {
            return (int)round(y * scale + centerYPixel);
        }
    }
}