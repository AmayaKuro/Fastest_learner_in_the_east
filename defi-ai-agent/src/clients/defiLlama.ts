import { env } from "../config/env";

export const defiLlamaClient = {
  baseUrl: env.DEFILLAMA_BASE_URL,
  async getProtocols() {
    return [{ name: "Aave", tvl: 1000000000 }];
  },
};
