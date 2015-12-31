package ru.teleknot.testmodem;

import ru.teleknot.modem.acorp.Sprinter56kExt;

/**
 * Created by lb426 on 31.12.2015.
 */
public class TestModem {
    public static void main(String[] args) {
        System.out.println("Testing modem");
        Sprinter56kExt m = new Sprinter56kExt("COM4");
        m.Init();
        m.StartWaitNMBR();
        m.Close();
    }
}
