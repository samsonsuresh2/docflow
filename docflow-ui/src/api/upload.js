export async function uploadDocument(file, clientId, uploadedBy) {
  const form = new FormData();
  form.append("file", file);
  form.append("clientId", clientId);
  form.append("uploadedBy", uploadedBy);
  const res = await fetch("/api/documents/upload", {
    method: "POST",
    body: form,
  });
  if (!res.ok) throw new Error("Upload failed");
  return await res.json();
}
