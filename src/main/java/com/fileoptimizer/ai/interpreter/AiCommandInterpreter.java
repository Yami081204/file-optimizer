package com.fileoptimizer.ai.interpreter;

import java.util.Locale;


public class AiCommandInterpreter {

    public enum ActionType {
        AUTO_CLEAN,      // "clean temp", "cleanup"
        FIND_DUPLICATES, // "find duplicates", "check doubles"
        SCAN_SYSTEM,     // "scan system", "analyze disk"
        HELP,            // "help", "what can you do"
        GREETING,        // "hello", "hi"
        UNKNOWN
    }

    public static class CommandResult {
        private final ActionType type;
        private final String response;

        public CommandResult(ActionType type, String response) {
            this.type = type;
            this.response = response;
        }

        public ActionType getType() { return type; }
        public String getResponse() { return response; }
    }


    public CommandResult interpret(String input) {
        if (input == null || input.isBlank()) {
            return new CommandResult(ActionType.UNKNOWN, "Please type something so I can help you.");
        }

        String cmd = input.toLowerCase(Locale.ROOT).trim();

        if (cmd.matches(".*(hello|hi|hey|greetings).*")) {
            return new CommandResult(ActionType.GREETING, "Hello! I'm your AI File Optimizer. How can I assist you today?");
        }

        if (cmd.contains("scan") || cmd.contains("analyze")) {
            return new CommandResult(ActionType.SCAN_SYSTEM, "I'll start a full system scan to analyze your files. Please wait a moment...");
        }

        if (cmd.contains("clean") || cmd.contains("cleanup") || cmd.contains("junk")) {
            return new CommandResult(ActionType.AUTO_CLEAN, "Initializing Auto Clean. I'll remove temporary and junk files safely.");
        }

        if (cmd.contains("duplicate") || cmd.contains("double") || cmd.contains("copy")) {
            return new CommandResult(ActionType.FIND_DUPLICATES, "Scanning for duplicate files now. I'll group them for your review.");
        }

        if (cmd.contains("help") || cmd.contains("what") || cmd.contains("do")) {
            return new CommandResult(ActionType.HELP, "You can ask me to 'scan system', 'clean temp files', or 'find duplicates'.");
        }

        return new CommandResult(ActionType.UNKNOWN, "I'm sorry, I didn't quite catch that. Try saying 'clean temp' or 'scan system'.");
    }
}
