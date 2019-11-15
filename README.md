# NexmoApp

NexmoApp is an example application calling the Nexmo REST APIs to check your account balance and send an SMS using the new Messaging API

## Build

You can build NexmoApp from source using Gradle:

```
gradle build
```

This will result in an application Jar file in ./build/libs/nexmoapp.jar

## Running

To run NexmoApp, use:

```
java -jar ./build/libs/nexmoapp.jar
```

Without any options, this will print the help instructions:

```
usage: nexmo <balance|sms> [OPTION]
 -a,--appid <arg>         Nexmo Application ID
 -h,--help                Print this message
 -k,--key <arg>           Nemo API key
 -p,--private-key <arg>   Nexmo private key file
 -s,--secret <arg>        Nexmo secret key`
```

### Checking your balance

To check your account balance, you will need to provide your Nexmo API key and API secret. You can do this with the `--key` and `--secret` options on the command line, or by setting them to `NEXMO_API_KEY` and `NEXMO_API_SECRET` in your environment variables.

**Example:**

```
java -jar ./build/libs/nexmoapp.jar balance --key abc123 --secret asdfjkl
```

Returns:
```
{"value":11.91320000,"autoReload":false}
```

### Sending an SMS

To send an SMS, you can use either your Nexmo API key an API secret as you did above, or an Application ID and private key using the `--appid` and `--private-key` command line options or `NEXMO_APP_ID` and `NEXMO_PRIVATE_KEY` environment variable. Note that the private key value should be the path to your private key file.

You will be prompted to enter the From and To telephone numbers, as well as the text message you wish to send. The text message must be in one line, and may contain unicode characters.

**Example:**

```
java -jar ./build/libs/nexmoapp.jar sms --appid abc12345-1234-1234-1234-1234567890ab --private-key ./my_key
What is your phone number?
18008675309
What number do you want to send to?
17607067425
Type your message:
Hello from Nexmo ☺
```

Returns:
```
{"message_uuid":"def12345-1234-1234-1234-1234567890ef"}
```