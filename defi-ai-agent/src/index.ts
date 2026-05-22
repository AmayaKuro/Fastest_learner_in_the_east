import { app } from "./server/app";
import { env } from "./config/env";
import { logger } from "./utils/logger";

const server = Bun.serve({
  port: env.PORT,
  fetch: app.fetch,
});

logger.info(`Server running at http://localhost:${server.port}`);
