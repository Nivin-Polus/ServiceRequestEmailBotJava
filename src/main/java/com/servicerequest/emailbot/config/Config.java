package com.servicerequest.emailbot.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    // ----------------- Slack -----------------
    public static final String SLACK_BOT_TOKEN = dotenv.get("SLACK_BOT_TOKEN");
    public static final String SLACK_APP_TOKEN = dotenv.get("SLACK_APP_TOKEN");
    public static final String SLACK_SIGNING_SECRET = dotenv.get("SLACK_SIGNING_SECRET");

    // ----------------- Anthropic (Claude API) -----------------
    public static final String CLAUDE_API_KEY = dotenv.get("CLAUDE_API_KEY");
    public static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";
    public static final String ANTHROPIC_VERSION = dotenv.get("ANTHROPIC_VERSION", "2023-06-01");

    // ----------------- Service Request API -----------------
    public static final String SERVICE_REQUEST_API_URL = dotenv.get("SERVICE_REQUEST_API_URL");
    public static final String DEFAULT_REPORTER_ID = dotenv.get("DEFAULT_REPORTER_PERSON_ID", "10000000001");
    public static final String DEFAULT_UNIT_NUMBER = dotenv.get("DEFAULT_UNIT_NUMBER", "000001");

    // ----------------- Priority Mapping -----------------
    public static final Map<String, Integer> PRIORITY_MAP = new HashMap<>();

    static {
        PRIORITY_MAP.put("Low", 4);
        PRIORITY_MAP.put("Medium", 3);
        PRIORITY_MAP.put("High", 2);
        PRIORITY_MAP.put("Critical", 1);
    }

    // ----------------- Database Configuration -----------------
    public static final String DB_HOST = dotenv.get("DB_HOST");
    public static final String DB_PORT = dotenv.get("DB_PORT", "3306");
    public static final String DB_NAME = dotenv.get("DB_NAME");
    public static final String DB_USER = dotenv.get("DB_USER");
    public static final String DB_PASS = dotenv.get("DB_PASS");

    // ----------------- Outlook Configuration -----------------
    public static final String OUTLOOK_CLIENT_ID = dotenv.get("OUTLOOK_CLIENT_ID");
    public static final String OUTLOOK_TENANT_ID = dotenv.get("OUTLOOK_TENANT_ID", "common");
    public static final String OUTLOOK_USER_EMAIL = dotenv.get("OUTLOOK_USER_EMAIL");
}
