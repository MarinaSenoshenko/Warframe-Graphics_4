package main;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import matrix.point.FigurePoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class State implements Serializable {
    private int n, m, circleN, circleM;
    private double zb, zf, sw, sh;
    private List<FigurePoint> points = new LinkedList<>();
    private List<FigurePoint> splinePoints = new LinkedList<>();

    public State(State state) {
        this.copy(state);
    }

    public static State createInitialState() {
        State state = new State();
        state.m = 10;
        state.n = 10;
        state.circleN = 10;
        state.circleM = 2;
        state.sw = 8;
        state.sh = 8;
        state.zf = 100;
        state.zb = 100;
        state.points = new ArrayList<>();
        for (int i = -1; i <= 1; i += 2){
            for (int j = -1; j <= 1; j += 2){
                state.points.add(new FigurePoint(i, j, 16));
            }
        }
        return state;
    }

    public void copy(State state) {
        n = state.n;
        m = state.m;
        zf = state.zf;
        zb = state.zb;
        sh = state.sh;
        sw = state.sw;
        circleN = state.circleN;
        circleM = state.circleM;
        for (FigurePoint splinePoint : state.points) {
            points.add(new FigurePoint(splinePoint));
        }
        for (FigurePoint splinePoint : state.splinePoints) {
            splinePoints.add(new FigurePoint(splinePoint));
        }
    }
}
