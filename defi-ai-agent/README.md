# DeFi AI Agent

AI agent that can recommend and execute optimal yield strategies.

## Setup

1. Install [Bun](https://bun.sh/)
2. Run `bun install`
3. Copy `.env.example` to `.env` and fill the variables
4. Run `bun run dev`

## API

- `GET /health`
- `POST /agent/chat`
- `POST /agent/plan`
- `POST /agent/execute`