import React from "react";
import { useLocation, useNavigate } from "react-router-dom";

export default function SuccessPage() {
  const { state } = useLocation();
  const navigate = useNavigate();
  return (
    <div className="center-box">
      <h2>Upload Successful ✅</h2>
      <p><b>System ID:</b> {state.systemId}</p>
      <p><b>Client Ref ID:</b> {state.clientRefId}</p>
      <p><b>Status:</b> {state.status}</p>
      <button onClick={() => navigate("/upload")}>Upload Another</button>
    </div>
  );
}
