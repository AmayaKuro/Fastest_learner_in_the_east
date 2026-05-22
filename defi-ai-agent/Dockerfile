FROM oven/bun:1 as builder
WORKDIR /app
COPY package.json bun.lockb* ./
RUN bun install --frozen-lockfile

COPY . .
RUN bun test
# Format code or build

FROM oven/bun:disable-telemetry-alpine
WORKDIR /app
COPY --from=builder /app /app
ENV NODE_ENV=production
EXPOSE 3000
CMD ["bun", "src/index.ts"]