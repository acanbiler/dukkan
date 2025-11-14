#!/bin/bash
# This hook runs after files are edited
# Can be used to remind about related tasks

FILE_PATH="$1"

# Check if a Java service file was modified
if [[ "$FILE_PATH" == backend/*/src/main/java/com/dukkan/* ]]; then
    # Check if it's a Service or Controller
    if [[ "$FILE_PATH" == *Service.java ]] || [[ "$FILE_PATH" == *Controller.java ]]; then
        echo "Reminder: Consider updating the corresponding test file in src/test/"
    fi
fi

# Check if package.json was modified
if [[ "$FILE_PATH" == *package.json ]]; then
    echo "Reminder: Run 'npm install' in the frontend directory to update dependencies"
fi

# Check if pom.xml was modified
if [[ "$FILE_PATH" == *pom.xml ]]; then
    echo "Reminder: Run 'mvn clean install' to update Maven dependencies"
fi

exit 0
