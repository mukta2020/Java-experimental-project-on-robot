
# Interview Candidate: Deawn Md Alimozzaman

# Robot Grid

Spring Boot 3 / Java 21 application that moves a robot on a first-quadrant 2D grid via a REST API, with Kafka for async processing and golden-point detection.

---

## Project Structure

```
src/main/java/com/robot/grid/
├── RobotGridApplication.java       # Spring Boot entry point
├── RobotSimulator.java             # Task 1: standalone console program (no Spring)
├── config/
│   └── KafkaConfig.java            # Topic declarations + listener container factory
├── controller/
│   └── RobotController.java        # POST /api/v1/robot/move
├── dto/
│   └── Dtos.java                   # All request/response/Kafka records in one place
├── exception/
│   └── GlobalExceptionHandler.java # Structured error responses
├── kafka/
│   ├── MoveProducer.java           # → move_instructions topic
│   ├── ResultProducer.java         # → instruction_results topic
│   ├── MoveConsumer.java           # move_instructions → compute → publish result
│   ├── ResultConsumer.java         # instruction_results → check golden points
│   └── GoldenPointConsumer.java    # golden_points → register coordinates
├── model/
│   ├── Direction.java              # Enum: 8 directions with dx/dy
│   └── Position.java               # Immutable record; boundary-clamped move()
├── service/
│   └── RobotService.java           # Movement logic, golden points, instruction IDs
└── util/
    └── InstructionParser.java      # Tokenises "TR2R2L" → [(TR,2),(R,2),(L,1)]
```

---

## Movement Rules

| Token | Direction    | Δx | Δy |
|-------|-------------|----|----|
| `U`   | Up           | 0  | +1 |
| `D`   | Down         | 0  | -1 |
| `L`   | Left         | -1 | 0  |
| `R`   | Right        | +1 | 0  |
| `TR`  | Top-right    | +1 | +1 |
| `TL`  | Top-left     | -1 | +1 |
| `BR`  | Bottom-right | +1 | -1 |
| `BL`  | Bottom-left  | -1 | -1 |

- Step count: `1`–`3`, defaults to `1` (e.g. `L2`, `TR3`, `U`)
- Robot stays in the **first quadrant** — clamped to `x ≥ 0`, `y ≥ 0`

### Sample I/O

| Start  | Instruction | Final |
|--------|-------------|-------|
| 10, 10 | `UL2RD`     | 9, 10 |
| 0, 0   | `LDDDLRDU`  | 1, 1  |
| 0, 0   | `TR2R2L`    | 3, 2  |

---

## Quick Start

**Prerequisites:** Java 21, Maven 3.9+, Docker

```bash
# 1. Start Kafka
docker compose up -d

# 2. Run the application
./mvnw spring-boot:run

# 3. Run tests
./mvnw test
```

---

## REST API

**`POST /api/v1/robot/move`**

```bash
curl -s -X POST http://localhost:8080/api/v1/robot/move \
  -H 'Content-Type: application/json' \
  -d '{"initial_position":{"x":10,"y":10},"move_instruction":"UL2RD"}' | jq .
```

Request:
```json
{
  "initial_position": { "x": 10, "y": 10 },
  "move_instruction": "UL2RD"
}
```

Response `200 OK`:
```json
{
  "final_position": { "x": 9, "y": 10 },
  "instruction_number": "IN000001"
}
```

---

## Kafka Topics

| Topic                 | Key                  | Payload          |
|-----------------------|----------------------|------------------|
| `move_instructions`   | `instruction_number` | `MoveMessage`    |
| `instruction_results` | `instruction_number` | `ResultMessage`  |
| `golden_points`       | any                  | `PositionDto`    |

**Publish a golden point** (triggers "I have reached a golden point!" on match):
```bash
echo '{"x":9,"y":10}' | kcat -b localhost:9092 -t golden_points -P
```

Kafka UI is available at **http://localhost:8090** after `docker compose up`.

---

## Standalone Console (Task 1)

```bash
./mvnw compile
java -cp target/classes com.robot.grid.RobotSimulator
```

```
Starting position (x, y): 10, 10
Move instruction: UL2RD
9, 10
```
