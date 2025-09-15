# Service Request Email Bot - Spring Boot

A comprehensive Spring Boot application that automatically processes emails and creates service requests. The bot integrates with Microsoft Outlook, Claude AI, Slack, and a service request management system to provide intelligent email-to-ticket conversion.

## 🚀 Features

- **📧 Email Processing**: Automatically monitors and processes incoming emails from Outlook
- **🤖 AI Integration**: Uses Claude AI to analyze, categorize, and extract information from emails
- **🎫 Service Request Management**: Creates, updates, and manages service requests with proper categorization
- **📋 Questionnaire Handling**: Processes questionnaire responses and follow-up comments from emails
- **📎 Attachment Support**: Handles email attachments and uploads them to service requests
- **💬 Slack Integration**: Sends notifications and updates via Slack channels
- **🔄 Scheduled Processing**: Runs on configurable intervals to check for new emails
- **🔐 Authentication**: Secure authentication with service request APIs
- **📊 Database Integration**: Reads categories and types from MySQL/PostgreSQL (read-only)
- **💾 Local Storage**: Thread mappings and session data stored locally in JSON files

## 🛠 Technology Stack

- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Scheduling** - Task scheduling
- **H2 Database** - Development database
- **MySQL/PostgreSQL** - Production database
- **Maven** - Build and dependency management
- **Jackson** - JSON processing
- **Apache HTTP Client** - HTTP requests
- **Microsoft Graph API** - Outlook integration
- **Claude API** - AI text analysis
- **Slack API** - Team notifications

## 📁 Project Structure

```
ServiceRequestEmailBotJava/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── servicerequest/
│   │   │           └── emailbot/
│   │   │               ├── ServiceRequestEmailBotApplication.java    # Main application class
│   │   │               ├── config/
│   │   │               │   └── Config.java                          # Configuration properties
│   │   │               ├── controller/
│   │   │               │   └── WebhookController.java               # REST API endpoints
│   │   │               ├── model/
│   │   │               │   ├── EmailData.java                       # Email data model
│   │   │               │   ├── MailForm.java                        # Mail form model
│   │   │               │   ├── OutlookSession.java                  # Outlook session model
│   │   │               │   └── ServiceRequestId.java                # SR ID mapping model
│   │   │               ├── service/
│   │   │               │   ├── EmailSchedulerService.java           # Main email scheduler
│   │   │               │   ├── EmailProcessorService.java           # Email processing logic
│   │   │               │   ├── OutlookAuthService.java              # Outlook authentication
│   │   │               │   ├── OutlookService.java                  # Outlook API integration
│   │   │               │   ├── OutlookServiceImpl.java              # Outlook service implementation
│   │   │               │   ├── OutlookSessionService.java           # Session management
│   │   │               │   ├── ServiceRequestService.java           # Service request operations
│   │   │               │   ├── CommentsService.java                 # Comments and attachments
│   │   │               │   ├── QuestionnaireService.java            # Questionnaire handling
│   │   │               │   ├── AdaptiveCardService.java             # Adaptive cards for Slack
│   │   │               │   ├── AttachmentService.java               # File attachment handling
│   │   │               │   ├── DbService.java                       # Database operations
│   │   │               │   └── SRServiceImpl.java                   # SR service implementation
│   │   │               └── util/
│   │   │                   └── UserCredentials.java                 # User credential management
│   │   └── resources/
│   │       ├── application.properties                               # Spring Boot configuration
│   │       └── static/                                             # Static web resources
│   └── test/
│       └── java/                                                    # Unit tests
├── pom.xml                                                          # Maven dependencies
├── .env.example                                                     # Environment variables template
├── README.md                                                        # This file
└── [Legacy files marked for deletion]                              # Old root-level Java files
```

## 🔧 Prerequisites

### Required Software
- **Java 17 or higher** - [Download OpenJDK](https://openjdk.org/projects/jdk/17/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)
- **MySQL 8.0+ or PostgreSQL 12+** - Database server
- **Git** - Version control

### Required API Access
- **Microsoft Graph API** - For Outlook integration
- **Claude API** - For AI text analysis
- **Slack API** - For notifications (optional)
- **Service Request API** - Your organization's SR system

## 📋 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd ServiceRequestEmailBotJava
```

### 2. Database Setup (Read-Only Access)

#### For MySQL:
```sql
CREATE DATABASE servicerequest;
CREATE USER 'srbot'@'localhost' IDENTIFIED BY 'your_password';
GRANT SELECT ON servicerequest.* TO 'srbot'@'localhost';
FLUSH PRIVILEGES;
```

#### For PostgreSQL:
```sql
CREATE DATABASE servicerequest;
CREATE USER srbot WITH PASSWORD 'your_password';
GRANT SELECT ON ALL TABLES IN SCHEMA public TO srbot;
```

**Note**: The application only requires READ access to the database for category and type lookups. All other data is stored locally in JSON files.

### 3. Environment Configuration

#### Option A: Using Environment Variables
Copy `.env.example` to `.env` and fill in your values:
```bash
cp .env.example .env
```

#### Option B: Direct Configuration
Edit `src/main/resources/application.properties` directly.

### 4. Required Environment Variables

#### 🔐 Authentication & APIs
```properties
# Claude AI (Required)
CLAUDE_API_KEY=your_claude_api_key_here

# Microsoft Outlook (Required)
OUTLOOK_CLIENT_ID=your_outlook_client_id
OUTLOOK_CLIENT_SECRET=your_outlook_client_secret
OUTLOOK_TENANT_ID=your_tenant_id_or_common
OUTLOOK_USER_EMAIL=bot@yourcompany.com

# Service Request APIs (Required)
AUTH_API=http://your-sr-system:2500/auth/login
SERVICE_REQUEST_API=http://your-sr-system:2500/fibi-service-request/saveOrUpdateServiceRequest
SUBMIT_SR_API=http://your-sr-system:2500/fibi-service-request/submitServiceRequest
COMMENTS_API=http://your-sr-system:2500/fibi-service-request/addSRCommentAndAttachment
```

#### 🗄️ Database Configuration
```properties
# MySQL Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=servicerequest
DB_USER=srbot
DB_PASS=your_database_password
```

#### 💬 Slack Integration (Optional)
```properties
SLACK_BOT_TOKEN=xoxb-your-slack-bot-token
SLACK_APP_TOKEN=xapp-your-slack-app-token
SLACK_SIGNING_SECRET=your_slack_signing_secret
```

#### ⚙️ Optional Configuration
```properties
EMAIL_CHECK_INTERVAL=30000                    # Check emails every 30 seconds
TEMP_DIRECTORY=temp_attachments               # Temporary file storage
DEFAULT_REPORTER_PERSON_ID=10000000001        # Default reporter ID
DEFAULT_UNIT_NUMBER=000001                    # Default organizational unit
```

### 5. Microsoft Graph API Setup

1. **Register Application** in Azure Portal:
   - Go to Azure Portal → App Registrations → New Registration
   - Name: "Service Request Email Bot"
   - Supported account types: "Accounts in this organizational directory only"
   - Redirect URI: `http://localhost` (Web)

2. **Configure API Permissions**:
   - Microsoft Graph → Application permissions:
     - `Mail.Read` - Read mail in all mailboxes
     - `Mail.Send` - Send mail as any user
     - `User.Read.All` - Read all users' profiles

3. **Grant Admin Consent** for the permissions

4. **Create Client Secret**:
   - Go to Certificates & secrets → New client secret
   - Copy the secret value (you won't see it again)

### 6. Claude API Setup

1. **Get API Key**:
   - Visit [Anthropic Console](https://console.anthropic.com/)
   - Create an account or sign in
   - Generate an API key
   - Add credits to your account

### 7. Build and Run

#### Development Mode (H2 Database):
```bash
# Comment out MySQL config in application.properties
# Uncomment H2 config lines
mvn clean install
mvn spring-boot:run
```

#### Production Mode (MySQL/PostgreSQL):
```bash
mvn clean install
mvn spring-boot:run -Dspring.profiles.active=prod
```

#### Using JAR file:
```bash
mvn clean package
java -jar target/service-request-emailbot-1.0.0.jar
```

## 🔍 Monitoring & Logs

### Application Logs
```bash
# View real-time logs
tail -f logs/application.log

# Check for errors
grep ERROR logs/application.log
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Database Console (H2 only)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## 🚦 Usage

### Automatic Email Processing
1. **Start Application**: The email scheduler automatically begins
2. **Email Detection**: Monitors configured Outlook mailbox
3. **AI Analysis**: Claude analyzes email content and determines:
   - Service request category
   - Priority level
   - Required actions
4. **SR Creation**: Automatically creates service requests
5. **Notifications**: Sends updates via Slack (if configured)

### Manual Operations
```bash
# Check application status
curl http://localhost:8080/api/health

# Trigger manual email check (if endpoint exists)
curl -X POST http://localhost:8080/api/process-emails
```

## 🔧 Configuration Options

### Email Processing Behavior
- **Check Interval**: How often to check for new emails (default: 30 seconds)
- **Batch Size**: Number of emails to process at once
- **Retry Logic**: Failed email processing retry attempts

### AI Analysis Settings
- **Claude Model**: Text analysis model version
- **Confidence Threshold**: Minimum confidence for auto-categorization
- **Fallback Behavior**: What to do when AI analysis fails

### Database Settings (Read-Only)
- **Connection Pool**: Database connection management for read operations only
- **Query Timeout**: Maximum query execution time for category/type lookups
- **Local Storage**: Thread mappings and session data stored in JSON files
- **No Database Writes**: All persistent data saved locally to avoid database modifications

## 🐛 Troubleshooting

### Common Issues

#### 1. Email Not Processing
```bash
# Check Outlook authentication
grep "Outlook" logs/application.log

# Verify API permissions in Azure Portal
# Ensure mailbox has unread emails
```

#### 2. Database Connection Issues
```bash
# Test database connectivity
mysql -h localhost -u srbot -p servicerequest

# Check connection pool status
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

#### 3. Claude API Errors
```bash
# Verify API key and credits
curl -H "x-api-key: YOUR_KEY" https://api.anthropic.com/v1/messages

# Check rate limiting
grep "rate limit" logs/application.log
```

#### 4. Service Request API Issues
```bash
# Test SR API connectivity
curl -X POST http://your-sr-system:2500/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

### Debug Mode
```bash
# Enable debug logging
export LOGGING_LEVEL_COM_SERVICEREQUEST_EMAILBOT=DEBUG
mvn spring-boot:run
```

## 🔒 Security Considerations

- **API Keys**: Store securely, never commit to version control
- **Database**: Use strong passwords and connection encryption
- **Network**: Restrict access to necessary ports only
- **Logs**: Avoid logging sensitive information
- **Updates**: Keep dependencies updated for security patches

## 🚀 Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/service-request-emailbot-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Production Checklist
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] SSL certificates installed
- [ ] Monitoring and alerting configured
- [ ] Backup strategy implemented
- [ ] Log rotation configured

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health check |
| GET | `/actuator/metrics` | Application metrics |
| POST | `/api/webhook` | External webhook receiver |
| GET | `/api/status` | Processing status |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section above
- Review application logs for error details

---

**Note**: This Spring Boot application provides robust email processing with enterprise features, scalability, and maintainability. All data persistence is handled through local JSON files to avoid database modifications.
- Connection timeouts: 30 seconds
- Token refresh: Automatic on 401 errors

---

## 🚨 Troubleshooting

### **Common Issues:**

1. **HTTP 400 Error on Service Request Creation:**
   - Check API endpoint URLs in environment variables
   - Verify JSON payload structure matches API expectations
   - Ensure category/type codes exist in database
   - Review debug logs for complete request/response details

2. **Comment Posting Issues:**
   - Verify COMMENTS_API endpoint is correct
   - Check debug output for payload structure mismatches
   - Ensure serviceRequestComment format matches API expectations
   - For file attachments: verify multipart form data with formDataJson
   - For text-only: ensure proper JSON structure with empty newAttachments array

3. **Outlook Authentication Failures:**
   - Verify Azure app registration permissions
   - Check client ID, secret, and tenant ID
   - Ensure user has consented to app permissions

4. **Attachment Upload Failures:**
   - Check ATTACHMENT_API endpoint
   - Verify COOKIE_TOKEN is valid
   - Ensure file types are supported

5. **Claude API Errors:**
   - Verify CLAUDE_API_KEY is correct
   - Check API rate limits
   - Ensure proper JSON response format

6. **Thread Mapping Issues:**
   - Check if `thread_sr_mapping.json` file is writable
   - Verify JSON file format is not corrupted
   - Ensure conversation_id is being extracted from emails
   - Check file permissions for JSON storage location

### **Logs and Debugging:**
The bot provides detailed console output for monitoring:
- Email processing status with conversation IDs
- Thread mapping operations (load/save to JSON)
- AI classification results from Claude
- Service request creation and submission
- Questionnaire form generation and sending
- Questionnaire response detection and parsing
- **Enhanced Comment Debugging**: Complete request/response logging including:
  - Request URL and headers (with authentication cookies)
  - Full JSON payload structure (formDataJson)
  - File attachment details and binary data
  - HTTP response status and body
  - Error details for troubleshooting API issues
- Attachment upload progress with file validation
- Bot content filtering (prevents processing own emails)
- Message content extraction and cleaning
- Authentication token management and refresh
- Retry mechanisms with exponential backoff

---

## 📦 Dependencies

### **Spring Boot Requirements:**
- Java 17+
- Spring Boot 3.x with Spring Data JPA
- Spring Scheduling for task automation
- Jackson for JSON processing
- Apache HTTP Client for REST calls
- Maven for build and dependency management

### **External Services:**
- Microsoft Graph API (Outlook integration)
- Anthropic Claude API (AI classification)
- Service Management System APIs
- Database (MySQL/PostgreSQL for categories - read-only access)

---

## 🔐 Security Notes

- Store sensitive credentials in `.env` file (never commit to version control)
- Use environment-specific API endpoints
- Regularly rotate API keys and tokens
- Monitor authentication token expiration
- Implement proper error handling for sensitive operations

---

## 📜 License

Private Use Only – This software is proprietary and not for public distribution.

---

