package matrix;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static java.lang.Math.*;

@Getter
@Setter
public abstract class AbstractMatrix implements Serializable, InterfaceMatrix {

    public static Matrix getRxMatrix(double turn) {
        return new Matrix(4, 4,
                new double[]{1, 0, 0, 0,
                        0, cos(turn), -sin(turn), 0,
                        0, sin(turn), cos(turn), 0,
                        0, 0, 0, 1
        });
    }

    public static Matrix getRyMatrix(double turn) {
        return new Matrix(4, 4,
                new double[]{
                        cos(turn), 0, sin(turn), 0,
                        0, 1, 0, 0,
                        -sin(turn), 0, cos(turn), 0,
                        0, 0, 0, 1
        });
    }

    public static Matrix getRzMatrix(double turn) {
        return new Matrix(4, 4,
                new double[]{
                        cos(turn), -sin(turn), 0, 0,
                        sin(turn), cos(turn), 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1
        });
    }

    public static Matrix getCameraMatrix(double zoom) {
        return new Matrix(4, 4,
                        new double[] {
                                1, 0, 0, 0,
                                0, 1, 0, 0,
                                0, 0, 1, zoom,
                                0, 0, 0, 1
                        });
    }

    public static Matrix getPerspectiveProjectionMatrix(double sw, double sh, double zf, double zb) {
        return new Matrix(4, 4,
                new double[]{
                        2.0 * zf * sw, 0, 0, 0,
                        0, 2.0 * zf * sh, 0, 0,
                        0, 0, zb / (zb - zf), (-zf * zb) / (zb - zf),
                        0, 0, 1, 0
        });
    }
}
