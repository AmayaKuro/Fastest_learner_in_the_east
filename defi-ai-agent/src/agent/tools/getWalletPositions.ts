export async function fetchWalletPositions(
  address: string,
): Promise<{ asset: string; amount: string }[]> {
  // Mock fetching positions
  return [{ asset: "USDC", amount: "1000.00" }];
}
