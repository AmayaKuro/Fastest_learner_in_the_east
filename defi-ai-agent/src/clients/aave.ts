import { env } from "../config/env";

export const aaveClient = {
  rpc: env.AAVE_RPC,
  async getUserData(address: string) {
    return { healthFactor: "2.1" };
  },
};
