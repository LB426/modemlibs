package ru.teleknot.testmodem;

import ru.teleknot.modem.acorp.Sprinter56kExt;

import java.io.IOException;

/**
 * Created by lb426 on 31.12.2015.
 */
public class TestModem {
    public static void main(String[] args) {
        System.out.println("Testing modem");
        try {
            Sprinter56kExt m = new Sprinter56kExt("COM4");
            m.StartWaitNMBR();
            m.Close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("finally");
        }
        System.out.println("END of Programm Testing modem");
    }
}
