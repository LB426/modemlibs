package ru.teleknot.testmodem;

import ru.teleknot.CliArgs;
import ru.teleknot.modem.acorp.Sprinter56kExt;

import java.io.IOException;

/**
 * Created by lb426 on 31.12.2015.
 */
public class TestModem {

    public static void main(String[] args) {

        CliArgs cliArgs = new CliArgs(args);

        System.out.println("Get CALLER ID from Acorp Sprinter56kExt modem");
        try {
            Sprinter56kExt m = new Sprinter56kExt(cliArgs.portRS232, cliArgs.chanName, cliArgs.srvUrl);
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
