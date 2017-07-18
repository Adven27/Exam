package com.sberbank.pfm.test.concordion.extensions.exam.rest;

import java.util.Scanner;
import java.util.Stack;

import static com.sberbank.pfm.test.concordion.extensions.exam.rest.JsonPrettyPrinter.State.*;

public class JsonPrettyPrinter {

    public String prettyPrint(String json) {
        return json == null ? "" : new Parser(json).parse();
    }

    enum State {
        UNKNOWN, OBJECT, ARRAY
    }

    private static class Parser {
        private static final String ESCAPE_CHARACTER = "\\";
        private Scanner scanner;
        private StringBuilder result;
        private int indent = 0;
        private boolean newLine = false;
        private Stack<State> state = new Stack<>();
        private String token;

        private Parser(String json) {
            scanner = new Scanner(json);
            scanner.useDelimiter("");
            result = new StringBuilder();
            enterState(UNKNOWN);
        }

        String parse() {
            try {
                while (hasToken()) {
                    if (nextTokenIs("{")) {
                        consumeToken();
                        if (!nextTokenIs("}")) {
                            growIndent();
                            append("{");
                            enterState(OBJECT);
                        } else {
                            append("{}");
                            consumeToken();
                        }
                        continue;
                    }
                    if (nextTokenIs("}")) {
                        shrinkIndent();
                    }
                    if (nextTokenIs("[")) {
                        enterState(ARRAY);
                    }
                    if (nextTokenIs("]")) {
                        leaveState();
                    }
                    if (nextTokenIs(",")) {
                        append(",");
                        if (inState(OBJECT)) {
                            newLine = true;
                        } else if (inState(ARRAY)) {
                            append(" ");
                        }
                        consumeToken();
                        continue;
                    }
                    if (nextTokenIs(":")) {
                        append(": ");
                        consumeToken();
                        continue;
                    }

                    if (newLine) {
                        append("\n");
                        indent();
                        newLine = false;
                    }

                    if (nextTokenIs("\"")) {
                        append("\"");
                        eatUnitl("\"");
                        consumeToken();
                        continue;
                    }

                    if (nextTokenIs("'")) {
                        append("'");
                        eatUnitl("'");
                        consumeToken();
                        continue;
                    }
                    append(token);
                    consumeToken();
                }
                return result.toString();
            } finally {
                scanner.close();
            }
        }

        private boolean inState(State testState) {
            return state.peek() == testState;
        }

        private State leaveState() {
            return state.isEmpty() ? UNKNOWN : state.pop();
        }

        private void enterState(State newState) {
            state.push(newState);
        }

        private void indent() {
            for (int i = 0; i < indent; i++) {
                append("  ");
            }
        }

        private void append(String value) {
            result.append(value);
        }

        private boolean nextTokenIs(String testToken) {
            return hasToken() && testToken.equals(token);
        }

        private void eatUnitl(String desiredToken) {
            String prev = "";
            while (scanner.hasNext()) {
                String x = scanner.next();
                result.append(x);
                if (x.equals(desiredToken) && !prev.equals(ESCAPE_CHARACTER)) {
                    break;
                }
                prev = x;
            }
        }

        private void shrinkIndent() {
            indent--;
            if (leaveState() == OBJECT) {
                newLine = true;
            }
        }

        private void growIndent() {
            indent++;
            newLine = true;
        }

        private boolean hasToken() {
            if (token != null) {
                return true;
            }

            while (scanner.hasNext()) {
                token = scanner.next().trim();
                if (token.isEmpty()) {
                    continue;
                }
                return true;
            }
            return false;
        }

        private void consumeToken() {
            token = null;
        }
    }
}