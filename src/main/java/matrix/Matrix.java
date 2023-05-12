package matrix;

import lombok.*;
import main.Main;
import matrix.point.FigurePoint;

import static java.lang.System.arraycopy;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Matrix extends AbstractMatrix {
    protected double[][] matrix;

    public Matrix(int rows, int cols, double[] matrix) {
        this.matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            arraycopy(matrix, i * cols, this.matrix[i], 0, cols);
        }
    }

    public double getValue() {
        if (matrix.length != 1 || matrix[0].length != 1) {
            Main.log.error("Not 1X1 matrix");
            throw new UnsupportedOperationException();
        }
        return matrix[0][0];
    }

    private double[][] matrixMul(double[][] matrix1, double[][] matrix2) {
        int x = matrix1.length, y = matrix2[0].length, z = matrix2.length;
        double[][] matrix = new double[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    matrix[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return matrix;
    }

    @Override
    public Matrix mul(double k) {
        double[][] matrix1 = matrix;
        int x = matrix1.length, y = matrix1[0].length;
        double[][] matrix = new double[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrix[i][j] = matrix1[i][j] * k;
            }
        }
        return new Matrix(matrix);
    }

    public double getX() {
        return matrix[0][0];
    }
    public double getY() {
        return matrix[1][0];
    }

    @Override
    public FigurePoint mul(FigurePoint operand) {
        return new FigurePoint(matrixMul(matrix, operand.getMatrix()));
    }

    @Override
    public Matrix mul(Matrix operand) {
        return new FigurePoint(matrixMul(matrix, operand.getMatrix()));
    }
}
