-- init.sql
-- This file will be executed when MySQL container starts for the first time

USE messages_db;

-- Grant additional privileges if needed
GRANT ALL PRIVILEGES ON messages_db.* TO 'app_user'@'%';
FLUSH PRIVILEGES;

-- You can add any initial data here if needed
-- INSERT INTO messages (content, timestamp) VALUES ('Welcome message', NOW());