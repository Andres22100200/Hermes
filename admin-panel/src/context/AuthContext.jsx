import { createContext, useContext, useState, useEffect } from 'react';
import { loginAdmin, obtenerPerfil } from '../api/adminApi';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [admin, setAdmin] = useState(null);
  const [loading, setLoading] = useState(true);

  // Al cargar la app verificar si hay token guardado
  useEffect(() => {
    const token = sessionStorage.getItem('adminToken');
    if (token) {
      obtenerPerfil()
        .then(res => setAdmin(res.data.admin))
        .catch(() => {
          sessionStorage.removeItem('adminToken');
          setAdmin(null);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (correo, password) => {
    const res = await loginAdmin(correo, password);
    sessionStorage.setItem('adminToken', res.data.token)
    setAdmin(res.data.admin);
    return res.data.admin;
  };

  const logout = () => {
    sessionStorage.removeItem('adminToken');
    setAdmin(null);
  };

  return (
    <AuthContext.Provider value={{ admin, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);