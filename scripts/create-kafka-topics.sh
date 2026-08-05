#!/bin/bash
# Creates the Kafka topics used across the platform. Idempotent - safe to re-run.
set -e

BROKER="kafka:9092"
TOPICS=(
  "vehicle.telemetry:6:1"        # raw GPS/speed/telemetry from driver app, via API Gateway
  "driver.alerts:3:1"            # alerts raised by monitoring-service (rule + AI agent driven)
  "route.updates:3:1"            # route recommendations from route-optimization-service
  "approval.decisions:3:1"       # audit trail of human/agent approval decisions
  "notification.dispatched:3:1"  # fan-out record of what was actually sent to whom
)

for entry in "${TOPICS[@]}"; do
  IFS=":" read -r name partitions replication <<< "$entry"
  echo "Creating topic: $name (partitions=$partitions, replication=$replication)"
  kafka-topics --bootstrap-server "$BROKER" \
    --create --if-not-exists \
    --topic "$name" \
    --partitions "$partitions" \
    --replication-factor "$replication"
done

echo "All topics ready."
kafka-topics --bootstrap-server "$BROKER" --list
