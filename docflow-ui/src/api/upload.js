export async function uploadDocument(file, clientId, username) {
  const form = new FormData();
  form.append("file", file);
  form.append("clientId", clientId);
  form.append("username", username);
  const res = await fetch("/api/documents/upload", {
    method: "POST",
    body: form,
  });
  if (!res.ok) throw new Error("Upload failed");
  return await res.json();
}
