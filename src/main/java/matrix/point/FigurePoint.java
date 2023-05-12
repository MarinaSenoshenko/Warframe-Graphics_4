package matrix.point;

import lombok.Getter;
import main.Main;

@Getter
public class FigurePoint extends AbstractFigurePoint {
    private int pointSize;

    public FigurePoint(double[][] matrix) {
        super(matrix);
    }

    public FigurePoint(double x, double y, int size) {
        super(x, y);
        pointSize = size;
    }

    public FigurePoint(FigurePoint splinePoint) {
        super();
        double[][] matrix = splinePoint.matrix;
        double[][] newMatrix = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix[0].length);
        }
        this.matrix = newMatrix;
        this.setPointSize(splinePoint.getPointSize());
    }

    public void normalizeByLastPoint() {
        int len = matrix.length;
        double lastPoint = matrix[len - 1][0];
        for (int i = 0; i < len; i++) {
            matrix[i][0] = matrix[i][0] / lastPoint;
        }
    }

    public void setPointSize(int pointSize) {
        if (pointSize >= 0) {
            this.pointSize = pointSize;
        }
        else {
            Main.log.error("Invalid point size");
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
