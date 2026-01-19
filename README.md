# project
 Event Processing & Analytics Service

A high-performance Spring Boot backend service for ingesting, deduplicating, and analyzing machine events in real time.
Designed to handle concurrent event ingestion, detect duplicates, compute statistics, and expose REST APIs for analytics.

## Features

- Batch event ingestion
- Deduplication based on eventId
- Intelligent update handling using receivedTime
- Rejects invalid events
- Thread-safe processing
- Statistical analytics APIs
 - Top defect line analytics
 - Fully tested (unit + concurrency tests)
 - Clean architecture

 ## Architecture
Controller
↓
Service Layer
↓
Repository (JPA)
↓
Database (H2 / MySQL)

Layers

Controller → Exposes REST APIs

Service → Business logic, dedupe, validation

Repository → Database access (JPA)

Entity → Event persistence model

DTOs → API input/output models

## Tech Stack

Java 17

Spring Boot 3.x

Spring Data JPA

H2 / MySQL

JUnit 5

Maven

Lombok

## Data Model
Event Entity
Field	Type	Description
id	Long	Auto-generated
eventId	String	Unique event identifier
eventTime	Instant	Time event occurred
receivedTime	Instant	Auto-set by backend
factoryId	String	Factory identifier
machineId	String	Machine identifier
lineId	String	Production line
durationMs	Long	Duration in ms
defectCount	Integer	Defects (-1 = ignore)
### Deduplication & Update Logic
Scenario	Action
Same eventId + same payload	Deduplicated
Same eventId + newer receivedTime	Update
Same eventId + older receivedTime	Ignored
Invalid duration	Rejected
Future eventTime	Rejected
### Thread Safety

✔ Database-level uniqueness (eventId)
✔ Transactional service layer
✔ Safe concurrent inserts
✔ Tested using parallel executor

### Performance Strategy

Batch ingestion

Minimal DB calls

Indexed fields

Stream-based aggregation

Stateless services

 Handles 1000 events under 1 second

## API Endpoints
🔹 1. Ingest Events (Batch)

POST /events/batch

Request
[
{
"eventId": "E1",
"eventTime": "2026-01-15T10:00:00Z",
"factoryId": "F1",
"machineId": "M1",
"lineId": "L1",
"durationMs": 1000,
"defectCount": 1
}
]

Response
{
"accepted": 1,
"deduped": 0,
"updated": 0,
"rejected": 0,
"rejections": []
}

🔹 2. Get Stats

GET

/stats?machineId=M1&start=2026-01-15T00:00:00Z&end=2026-01-15T23:59:59Z

Response
{
"machineId": "M1",
"eventsCount": 10,
"defectsCount": 5,
"avgDefectRate": 0.8,
"status": "Healthy"
}

🔹 3. Top Defect Lines

GET

/stats/top-defect-lines?factoryId=F1&from=2026-01-15T00:00:00Z&to=2026-01-15T23:59:59Z&limit=5

Response
[
{
"lineId": "L1",
"totalDefects": 10,
"eventCount": 5,
"defectsPercent": 200.0
}
]

## Testing

✔ 9 Test cases
✔ Covers:

Deduplication

Update logic

Invalid data

Time boundaries

Concurrent ingestion

Defect filtering

Run tests:

mvn test

⚙️ Setup Instructions
1️⃣ Clone
git clone https://github.com/RheaTiwari01/project.git
cd project

2️⃣ Run App
mvn spring-boot:run

3️⃣ Access
http://localhost:8080

📂 Project Structure
src/main/java
├── controller
├── service
├── repository
├── entity
├── dto
└── ProjectApplication.java

 Future Improvements

Kafka ingestion


Docker support

Adding analytics

Async processing