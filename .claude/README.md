# Claude Configuration

This directory contains Claude Code configuration for the Dukkan project.

## Structure

- **commands/**: Custom slash commands for common development tasks
- **hooks/**: Event hooks that trigger during Claude Code operations

## Available Commands

- `/build-backend` - Build all backend services
- `/build-frontend` - Build the React frontend
- `/test-backend` - Run backend tests
- `/docker-up` - Start all services with Docker Compose
- `/docker-down` - Stop all Docker services
- `/new-service` - Create a new microservice

## Hooks

- **user-prompt-submit.sh**: Provides context-aware reminders based on prompt content
- **post-edit.sh**: Reminds about related tasks after file edits

## Usage

Commands can be invoked by typing `/command-name` in the chat.
Hooks run automatically during Claude Code operations.
