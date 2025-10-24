import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  const [username, setUser] = useState("");
  const [password, setPass] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault(); // stop page reload
    console.log("Attempting login for", username);

    try {
      const res = await axios.post("/api/auth/login", {
        username,
        password,
      });
      console.log("Login success", res.data);
      // Example redirect after success:
       navigate("/home");
    } catch (err) {
      console.error("Login failed:", err);
      alert("Invalid credentials");
    }
  };

  return (
    <div className="center-box">
      <h2>DocFlow Login</h2>
      <form onSubmit={handleLogin}>
        <input
          placeholder="Username"
          value={username}
          onChange={(e) => setUser(e.target.value)}
        />
        <input
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e) => setPass(e.target.value)}
        />
        <button type="submit">Login</button>
      </form>
    </div>
  );
}
