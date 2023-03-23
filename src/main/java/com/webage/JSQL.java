package com.webage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class JSQL {
    static void showHelp() {
        System.out.println("quit - end session.");
        System.out.println("begin - begin transaction.");
        System.out.println("commit - commit transaction.");
        System.out.println("rollback - abort transaction.");
    }
    
    public static void main(final String[] args) {
        Connection connection = null;

        try {
            String config = args.length > 0 ? args[0] + "." : "";
            Terminal terminal = TerminalBuilder.terminal();
            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.HISTORY_FILE, "./.history")
                .history(new DefaultHistory())
                .completer(new StringsCompleter(Arrays.asList("quit", "begin", "commit", "rollback")))
                .build();

            System.out.println("jsql (C) Web Age Solutions Inc.");
            System.out.println("Enter help for help.");
            System.out.println("Using config: " + config);

            final ResourceBundle bundle = ResourceBundle.getBundle("JSQL");
            final String driver = bundle.getString(config + "driver");
            final String url = bundle.getString(config + "url");
            final String user = bundle.getString(config + "user");
            final String password = bundle.getString(config + "password");

            Class.forName(driver).newInstance();

            connection = DriverManager.getConnection(url, user, password);

            var scriptFile = getArgValue(args, "-f");

            if (scriptFile.isPresent()) {
                loadScriptFile(scriptFile.get(), connection);

                return;
            }

            var exportQuery = getArgValue(args, "-e");

            if (exportQuery.isPresent()) {
                exportQuery(exportQuery.get(), connection);

                return;
            }

            while (true) {
                String line = reader.readLine("> ");

                if (line == null) {
                    break;
                }
                if (line.equals("quit")) {
                    break;
                }
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }

                try {
                    if (line.equals("begin")) {
                        connection.setAutoCommit(false);
                        System.out.println("Transaction started.");

                        continue;
                    }
                    if (line.equals("commit")) {
                        connection.commit();
                        System.out.println("Transaction committed.");
                        continue;
                    }
                    if (line.equals("rollback")) {
                        connection.rollback();
                        System.out.println("Transaction rolledback.");
                        continue;
                    }
                    if (line.equals("help")) {
                        showHelp();
                        continue;
                    }

                    runScript(line, connection);
                } catch (SQLException nextException) {
                    do {
                        System.out.println(nextException.getMessage());
                        nextException = nextException.getNextException();
                    } while (nextException != null);
                    continue;
                } finally {
                }
            }
        } catch (EndOfFileException eof) {
            //Bye!
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void loadScriptFile(String scriptFile, Connection connection) throws Exception {
        try (var br = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            StringBuffer script = new StringBuffer();
            var pattern = Pattern.compile(";\\s*$");

            while ((line = br.readLine()) != null) {
                if (line.startsWith("--")) {
                    //Comment
                    continue;
                }

                //Echo the line back
                System.out.printf("%s\n", line);

                var matcher = pattern.matcher(line);

                //See if we have reached the end of the SQL.
                if (matcher.find()) {
                    //Get rid of the ;
                    line = matcher.replaceAll("");

                    script.append(line);

                    //End of script
                    try {
                        runScript(script.toString(), connection);                            
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    
                    //Clear script
                    script.setLength(0);
                } else {
                    script.append(line);
                    //Add a separator
                    script.append(" ");
                }
            }
        }
    }

    private static void exportQuery(String sql, Connection connection) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        
        try (var statement = connection.createStatement(); var csv = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE)) {
            if (!statement.execute(sql)) {
                return;
            }

            var resultSet = statement.getResultSet();
            String[] headers = null;
            Object[] row = null;
            
            while (resultSet.next()) {
                var metaData = resultSet.getMetaData();

                if (headers == null) {
                    headers = new String[metaData.getColumnCount()];
                    row = new Object[metaData.getColumnCount()];
        
                    for (int i = 0; i < metaData.getColumnCount(); ++i) {
                        headers[i] = metaData.getColumnLabel(i + 1);
                    }
                    
                    csv.writeHeader(headers);
                }

                for (int i = 0; i < metaData.getColumnCount(); ++i) {
                    row[i] = resultSet.getObject(i + 1);
                }

                csv.write(row);
            }

            writer.flush();
        }
    }

    private static void runScript(String script, Connection connection) throws Exception {
        try (var statement = connection.createStatement()) {
            if (statement.execute(script)) {
                var resultSet = statement.getResultSet();
                final ResultSetMetaData metaData = resultSet.getMetaData();

                while (resultSet.next()) {
                    for (int i = 0; i < metaData.getColumnCount(); ++i) {
                        System.out.print(metaData.getColumnLabel(i + 1));
                        System.out.print(": ");
                        final Object object = resultSet.getObject(i + 1);
                        System.out.println((object != null) ? object.toString() : "null");
                    }

                    System.out.println("");
                }
            } else {
                System.out.println(statement.getUpdateCount() + " row(s) affected.");
            }
        }
    }

    static Optional<String> getArgValue(String[] args, String argName) {
        return Stream
            .of(args)
            .dropWhile(a -> !a.equals(argName))
            .skip(1)
            .findFirst();
    }
    static boolean hasArg(String[] args, String argName) {
        return Stream
            .of(args)
            .filter(a -> a.equals(argName))
            .findFirst()
            .isPresent();
    }
}