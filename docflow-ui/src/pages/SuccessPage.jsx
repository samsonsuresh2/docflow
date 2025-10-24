import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

export default function SuccessPage() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (!location.state) {
      navigate("/upload", { replace: true });
    }
  }, [location.state, navigate]);

  if (!location.state) return null;

  const { state } = location;
  return (
    <div className="center-box">
      <h2>Upload Successful ✅</h2>
      <p><b>System ID:</b> {state.systemId || "Not available"}</p>
      <p><b>Client Ref ID:</b> {state.clientRefId || "Not available"}</p>
      <p><b>Status:</b> {state.status || "Not available"}</p>
      <button onClick={() => navigate("/upload")}>Upload Another</button>
    </div>
  );
}