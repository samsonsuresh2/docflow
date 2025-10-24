import axios from 'axios';

export async function login(username, password) {
  try {
    const response = await axios.post('/api/auth/login', {
      username,
      password
    });
    return response.data;
  } catch (error) {
    console.error('Login failed:', error);
    throw error;
  }
}
