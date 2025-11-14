#!/bin/bash
# This hook runs when the user submits a prompt
# It can be used to provide context-aware reminders

# Example: Check if user is asking about database changes
if echo "$PROMPT" | grep -iq "database\|entity\|jpa\|repository"; then
    echo "Reminder: When working with JPA entities, ensure to:"
    echo "  - Add proper validation annotations"
    echo "  - Update database migrations if using Flyway/Liquibase"
    echo "  - Consider impact on existing data"
fi

# Example: Check if user is asking about API changes
if echo "$PROMPT" | grep -iq "endpoint\|controller\|rest\|api"; then
    echo "Reminder: When creating/modifying REST endpoints:"
    echo "  - Use proper HTTP methods (GET, POST, PUT, DELETE)"
    echo "  - Add OpenAPI/Swagger documentation"
    echo "  - Implement proper error handling and validation"
    echo "  - Consider API versioning"
fi

exit 0
