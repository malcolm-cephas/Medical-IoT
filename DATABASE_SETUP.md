# Medical IoT System - MySQL Setup Guide

## Prerequisites
1. Install MySQL Server (8.0 or higher recommended)
2. Ensure MySQL is running on localhost:3306

## Quick Setup

### Option 1: Automatic (Recommended)
The application is configured with `createDatabaseIfNotExist=true`, so the database will be created automatically on first run.

**Default Credentials:**
- Username: `root`
- Password: `<your-password>` (as configured in your local MySQL)

### Option 2: Manual Setup
If you prefer to create the database manually:

```sql
CREATE DATABASE medical_iot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Option 3: Environment Variables (.env) - RECOMMENDED
The most secure way to handle credentials is via the `.env` file in the project root:

1. Open `.env` in the root directory.
2. Update `DB_USERNAME` and `DB_PASSWORD`.
3. The Spring Boot application will automatically pick these up via `application.properties`.

### Custom MySQL Credentials
If you prefer not to use `.env`, you can directly update `backend-spring/src/main/resources/application.properties`:

```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Database Schema
The application uses Hibernate's `ddl-auto=update` mode, which will:
- Automatically create tables on first run
- Update schema when entities change
- Preserve existing data

## Tables Created
The system will automatically create the following tables:
- `users` - User accounts (doctors, nurses, patients, admin)
- `sensor_data` - Patient vital signs with timestamps
- ` - Security audit logs
`patient_consent` - Consent management records
- `security_event
## Verification

After starting the application, verify the database:

```sql
USE medical_iot_db;
SHOW TABLES;
SELECT COUNT(*) FROM users;  -- Should show 38 users (35 patients + 1 doctor + 1 nurse + 1 admin)
```

## Troubleshooting

### Connection Refused
- Ensure MySQL is running: `sudo systemctl status mysql` (Linux) or check Services (Windows)
- Verify port 3306 is not blocked by firewall

### Authentication Failed
- Check MySQL user credentials
- Grant privileges if needed:
  ```sql
  GRANT ALL PRIVILEGES ON medical_iot_db.* TO 'root'@'localhost';
  FLUSH PRIVILEGES;
  ```

### Timezone Issues
The connection string includes `serverTimezone=UTC`. If you encounter timezone errors, ensure your MySQL server timezone is configured.

## Production Recommendations
For production deployment:
1. Create a dedicated MySQL user (not root)
2. Use strong passwords
3. Enable SSL: Change `useSSL=false` to `useSSL=true`
4. Set `spring.jpa.hibernate.ddl-auto=validate` (not update)
5. Use connection pooling (HikariCP is included by default)
