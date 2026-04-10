import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [correo, setCorreo] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(correo, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#1a1a2e',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center'
    }}>
      <div style={{
        backgroundColor: '#16213e',
        padding: '48px',
        borderRadius: '16px',
        width: '100%',
        maxWidth: '400px',
        boxShadow: '0 20px 60px rgba(0,0,0,0.5)'
      }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
  <img src="/hermes_icon.png" alt="Hermes" style={{ width: '32px', height: '32px' }} />
  <h1 style={{ fontSize: '22px', fontWeight: 'bold', color: '#e94560', margin: 0 }}>
    Hermes
  </h1>
</div>
          <p style={{ color: '#888', margin: '8px 0 0 0' }}>Panel de Administración</p>
        </div>

        {/* Error */}
        {error && (
          <div style={{
            backgroundColor: '#e9456020',
            border: '1px solid #e94560',
            color: '#e94560',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '16px',
            fontSize: '14px'
          }}>
            {error}
          </div>
        )}

        {/* Formulario */}
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '16px' }}>
            <label style={{ color: '#ccc', fontSize: '14px', display: 'block', marginBottom: '8px' }}>
              Correo electrónico
            </label>
            <input
              type="email"
              value={correo}
              onChange={e => setCorreo(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '12px',
                backgroundColor: '#0f3460',
                border: '1px solid #2d2d44',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
              placeholder="admin@hermes.com"
            />
          </div>

          <div style={{ marginBottom: '24px' }}>
            <label style={{ color: '#ccc', fontSize: '14px', display: 'block', marginBottom: '8px' }}>
              Contraseña
            </label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '12px',
                backgroundColor: '#0f3460',
                border: '1px solid #2d2d44',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                boxSizing: 'border-box'
              }}
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              padding: '14px',
              backgroundColor: loading ? '#888' : '#e94560',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontSize: '16px',
              fontWeight: 'bold',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;