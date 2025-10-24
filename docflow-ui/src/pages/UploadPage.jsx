import React, { useState, useEffect } from "react";
import { getClientConfig } from "../api/clientConfig";
import { uploadDocument } from "../api/upload";
import { useNavigate } from "react-router-dom";

export default function UploadPage() {
  const [clientId] = useState("ACME");
  const [config, setConfig] = useState(null);
  const [file, setFile] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    getClientConfig(clientId).then(setConfig).catch(console.error);
  }, [clientId]);

  const handleUpload = async () => {
    if (!file) return alert("Please choose a file");
    try {
      const username = localStorage.getItem("user") || "samson";
      const res = await uploadDocument(file, clientId, username);
      navigate("/success", {
        state: {
          ...res,
          systemId: res.systemDocId ?? res.systemId ?? "",
          clientRefId: res.clientDocId ?? res.clientRefId ?? "",
          status: res.status ?? "",
        },
      });
    } catch (err) {
      alert("Upload failed");
    }
  };

  if (!config) return <p>Loading client config...</p>;

  return (
    <div className="center-box">
      <h2>Upload Document ({clientId})</h2>
      <input type="file" accept=".docx" onChange={(e) => setFile(e.target.files[0])} />
      <button onClick={handleUpload}>Upload</button>
    </div>
  );
}