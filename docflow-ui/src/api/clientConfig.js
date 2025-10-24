export async function getClientConfig(clientId) {
  const res = await fetch(`/api/client/config?clientId=${clientId}`);
  if (!res.ok) throw new Error("Config not found");
  return await res.json();
}
