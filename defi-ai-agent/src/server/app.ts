import { Hono } from "hono";
import { logger } from "../utils/logger";
import { agentRoutes } from "./routes/agent";

const app = new Hono();

app.use("*", async (c, next) => {
  const start = Date.now();
  await next();
  const ms = Date.now() - start;
  logger.info(`${c.req.method} ${c.req.url} - ${c.res.status} [${ms}ms]`);
});

app.get("/health", (c) => c.json({ status: "ok" }));
app.route("/agent", agentRoutes);

export { app };
