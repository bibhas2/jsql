package com.webage;

import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class JSQL {
    static void showHelp() {
        System.out.println("quit - end session.");
        System.out.println("begin - begin transaction.");
        System.out.println("commit - commit transaction.");
        System.out.println("rollback - abort transaction.");
    }
    
    public static void main(final String[] args) {
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

            final Connection connection = DriverManager.getConnection(url, user, password);
            Statement statement = null;
            ResultSet resultSet = null;

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
                    resultSet = null;
                    statement = null;

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
                    statement = connection.createStatement();
                    if (statement.execute(line)) {
                        resultSet = statement.getResultSet();
                    }
                    if (resultSet == null) {
                        System.out.println(statement.getUpdateCount() + " row(s) affected.");
                        continue;
                    }

                    final ResultSetMetaData metaData = resultSet.getMetaData();

                    while (resultSet.next()) {
                        for (int i = 0; i < metaData.getColumnCount(); ++i) {
                            System.out.print(metaData.getColumnName(i + 1));
                            System.out.print(": ");
                            final Object object = resultSet.getObject(i + 1);
                            System.out.println((object != null) ? object.toString() : "null");
                        }

                        System.out.println("");
                    }
                } catch (SQLException nextException) {
                    do {
                        System.out.println(nextException.getMessage());
                        nextException = nextException.getNextException();
                    } while (nextException != null);
                    continue;
                }
                finally {
                    if (resultSet != null) {
                        resultSet.close();
                        resultSet = null;
                    }
                    if (statement != null) {
                        statement.close();
                        statement = null;
                    }
                }
            }

            connection.close();
        } catch (EndOfFileException eof) {
            //Bye!
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}