import { z } from "zod";

export const envSchema = z.object({
  OPENAI_API_KEY: z.string().min(1).default("MOCK_KEY"),
  RPC_URL: z.string().url().optional(),
  DEFILLAMA_BASE_URL: z.string().url().default("https://api.llama.fi"),
  AAVE_RPC: z.string().url().optional(),
  PORT: z.coerce.number().default(3000),
});

export type Env = z.infer<typeof envSchema>;
