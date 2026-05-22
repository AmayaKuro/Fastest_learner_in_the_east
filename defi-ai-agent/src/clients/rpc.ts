import { createPublicClient, http } from "viem";
import { mainnet } from "viem/chains";
import { env } from "../config/env";

export const rpcClient = createPublicClient({
  chain: mainnet,
  transport: http(env.RPC_URL),
});
