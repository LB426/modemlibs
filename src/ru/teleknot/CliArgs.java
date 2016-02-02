package ru.teleknot;

import org.apache.commons.cli.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Andrey.Seredin on 02.02.2016.
 */
public class CliArgs {
    public CliArgs(String[] args) {
        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("p", "port", true, "show port.");
        options.addOption("c", "channame", true, "show channame.");
        options.addOption("u", "url", true, "show url.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if(cmd.hasOption("h")){
                help();
            }

            if (cmd.hasOption("p")) {
                //log.log(Level.INFO, "Using cli argument -p=" + cmd.getOptionValue("p"));
                portRS232 = cmd.getOptionValue("p");
            } else {
                log.log(Level.SEVERE, "MIssing p option");
                help();
            }

            if (cmd.hasOption("c")) {
                //log.log(Level.INFO, "Using cli argument -c=" + cmd.getOptionValue("c"));
                chanName = cmd.getOptionValue("c");
            } else {
                log.log(Level.SEVERE, "MIssing c option");
                help();
            }

            if (cmd.hasOption("u")) {
                //log.log(Level.INFO, "Using cli argument -u=" + cmd.getOptionValue("u"));
                srvUrl = cmd.getOptionValue("u");
            } else {
                log.log(Level.SEVERE, "MIssing u option");
            }

        } catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse comand line properties", e);
            help();
        }

    }

    private void help(){
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("Main", options);
        System.exit(0);
    }

    private String[] args = null;
    private Options options = new Options();
    private static final Logger log = Logger.getLogger(CliArgs.class.getName());
    public String portRS232 = "undefined";
    public String chanName = "undefined";
    public String srvUrl = "undefined";
}
