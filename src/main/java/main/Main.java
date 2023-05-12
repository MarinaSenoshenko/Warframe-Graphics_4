package main;

import view.frame.InitWindow;
import org.apache.log4j.Logger;

public class Main {
    public static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        new InitWindow().setVisible(true);
    }
}
