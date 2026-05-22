export async function generateTransaction(action: string): Promise<{ to: string; data: string }> {
  // Mock building tx
  return { to: "0x0000000000000000000000000000000000000000", data: "0x" };
}
