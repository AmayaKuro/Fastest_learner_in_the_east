import { logger } from "../utils/logger";

export async function runAgentOnce(prompt: string): Promise<string> {
  logger.info(`Running agent with prompt: ${prompt}`);
  // In a real app we would call OpenAI with tools
  return `Agent evaluated: ${prompt}. Tools mocked successfully.`;
}
