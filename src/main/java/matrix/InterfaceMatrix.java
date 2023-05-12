package matrix;

import matrix.point.FigurePoint;

public interface InterfaceMatrix {
    FigurePoint mul(FigurePoint operand);
    Matrix mul(Matrix operand);
    Matrix mul(double k);
}
