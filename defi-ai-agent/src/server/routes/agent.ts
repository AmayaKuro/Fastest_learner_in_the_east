import { Hono } from "hono";
import { runAgentOnce } from "../../agent/planner";

const  agentRoutes = new Hono();

agentRoutes.post("/chat", async (c) => {
  const { message } = await c.req.json();
  const response = await runAgentOnce(message);
  return c.json({ response });
});

agentRoutes.post("/plan", async (c) => {
  const { intent } = await c.req.json();
  return c.json({ plan: "Mock plan for intent: " + intent });
});

agentRoutes.post("/execute", async (c) => {
  const { signedTx } = await c.req.json();
  return c.json({ status: "executed", txHash: "0xMockHash" });
});

export { agentRoutes };
