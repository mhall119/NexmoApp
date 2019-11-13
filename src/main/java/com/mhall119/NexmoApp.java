package com.mhall119;

import org.apache.commons.cli.*;

import java.util.List;
import java.util.Scanner;

public class NexmoApp {
    private String NEXMO_API_KEY = System.getenv("NEXMO_API_KEY");
    private String NEXMO_API_SECRET = System.getenv("NEXMO_API_SECRET");
    private String NEXMO_APP_ID = System.getenv("NEXMO_APP_ID");
    private String NEXMO_PRIVATE_KEY = System.getenv("NEXMO_PRIVATE_KEY");

    public NexmoApp(CommandLine cmd) {
        if (cmd.hasOption("key")) {
            this.NEXMO_API_KEY = cmd.getOptionValue("key");
        }
        if (cmd.hasOption("secret")) {
            this.NEXMO_API_SECRET = cmd.getOptionValue("secret");
        }

    }

    public static void main( String[] args ) {
        Options options = new Options();
        options.addOption("k", "key", true, "Nemo API key");
        options.addOption("s", "secret", true, "Nexmo secret key");
        options.addOption("h", "help", false, "Print this message");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            NexmoApp app = new NexmoApp(line);

            if (line.hasOption("help")) {
                printHelp(options);
                System.exit(0);
            }

            List<String> extraArgs = line.getArgList();
            if (extraArgs.size() > 1) {
                System.err.println("Too many commands. Select either 'balance' or 'sms' command.");
                printHelp(options);
                System.exit(1);
            }

            String command = "help";
            if (extraArgs.size() == 1) {
                command = extraArgs.get(0);
            }

            if (command.equalsIgnoreCase("balance")) {
                app.checkBalance();
            } else if (command.equalsIgnoreCase("sms")) {
                app.sendSMS();
            } else if (command.equalsIgnoreCase("help")) {
                printHelp(options);
            } else {
                System.err.println("Unknown command: "+command);
            }

            // System.out.println("Hello Nexmo");

            // System.out.println("API Key: "+app.NEXMO_API_KEY);
            // System.out.println("Secret Key: "+app.NEXMO_SECRET_KEY);
        }
        catch( ParseException exp ) {
            // Command line options parsing failed
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "nexmo <balance|sms> [OPTION]", options );

    }

    private void checkBalance() {

    }

    private void sendSMS() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("What is your phone number?");
        String FROM_NUMBER = scanner.next();

        System.out.println("What number do you want to send to?");
        String TO_NUMBER = scanner.next();

        System.out.println("Type your message:");
        String MSG = scanner.next();

        scanner.close();


    }
}
