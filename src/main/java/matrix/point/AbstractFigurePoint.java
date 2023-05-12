package matrix.point;

import matrix.Matrix;

import java.io.Serializable;

public class AbstractFigurePoint extends Matrix implements Serializable {

    public AbstractFigurePoint() {
        super();
    }

    public AbstractFigurePoint(double[][] matrix) {
        super(matrix);
    }

    public AbstractFigurePoint(double x, double y) {
        super(4, 1, new double[]{0, y, x, 1});
    }

    public void setY(double y) {
        matrix[1][0] = y;
    }

    public void setX(double x) {
        matrix[2][0] = x;
    }

    @Override
    public double getX() {
        return matrix[2][0];
    }

    @Override
    public double getY() {
        return matrix[1][0];
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
