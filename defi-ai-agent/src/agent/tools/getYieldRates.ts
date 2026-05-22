export async function fetchYieldRates(): Promise<{ protocol: string; apy: number }[]> {
  // Mock fetching yield rates
  return [
    { protocol: "Aave", apy: 3.5 },
    { protocol: "Morpho", apy: 4.2 },
  ];
}
