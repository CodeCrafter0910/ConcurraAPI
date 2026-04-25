# Social API

A Spring Boot REST API for managing posts, comments, and likes with Redis-based rate limiting and notification batching.

## Setup

You'll need Docker installed. Clone the repo and run:

```bash
docker-compose up --build
```

This starts the app along with Postgres and Redis. The API runs on `http://localhost:8080`.

If you want to run it locally without Docker, make sure you have Java 21, Postgres, and Redis running. Then:

```bash
mvn spring-boot:run
```

## Endpoints

**Create a post**
```
POST /api/posts
{
  "authorId": 1,
  "authorType": "USER",
  "content": "your post text"
}
```

**Add a comment**
```
POST /api/posts/{postId}/comments
{
  "authorId": 1,
  "authorType": "BOT",
  "content": "comment text",
  "depthLevel": 1,
  "postOwnerId": 1
}
```

**Like a post**
```
POST /api/posts/{postId}/like
{
  "userId": 2,
  "userType": "USER"
}
```

There's a `postman_collection.json` file you can import to test everything.

## How it works

### Virality scoring
Each interaction updates a score in Redis:
- Bot comment: +1
- User like: +20
- User comment: +50

### Bot limits
Bots can't spam. There are three rules:
1. Max 100 bot comments per post
2. Comment threads can't go deeper than 20 levels
3. A bot can only interact with the same user once every 10 minutes

These limits are enforced using Redis. The counter for bot comments uses Redis INCR which is atomic, so even if 200 requests hit at once, only 100 will get through. If the counter goes over 100, it gets decremented immediately and the request fails.

For the cooldown, I use Redis keys with a 10 minute TTL. If the key exists, the bot is blocked.

### Notifications
When a bot interacts with a user's post, the system checks if that user got a notification in the last 15 minutes. If yes, the notification gets queued in Redis. If no, it sends immediately and starts a 15 minute cooldown.

A background job runs every 5 minutes to check for queued notifications. If it finds any, it sends one combined message like "Bot X and 3 others interacted with your posts" instead of spamming the user.

## Project structure

```
src/main/java/com/grid07/socialapi/
  config/       - Redis config and data seeding
  controller/   - REST endpoints
  dto/          - Request objects
  entity/       - Database models
  repository/   - Data access
  scheduler/    - Background notification job
  service/      - Business logic
```

## Tech stack

Java 21, Spring Boot 3.2.5, PostgreSQL, Redis, Docker
