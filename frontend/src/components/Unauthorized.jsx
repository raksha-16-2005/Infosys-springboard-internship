import React from 'react';
import { Link } from 'react-router-dom';

const Unauthorized = () => {
  return (
    <div className="container" style={{ paddingTop: '6rem', textAlign: 'center' }}>
      <div className="glass-panel" style={{ maxWidth: 520, margin: '0 auto', padding: '2.5rem' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.75rem' }}>Access restricted</h1>
        <p className="text-muted" style={{ marginBottom: '1.5rem' }}>
          You’re signed in, but your current role doesn’t have permission to view this area.
        </p>
        <p className="text-muted" style={{ marginBottom: '2rem' }}>
          If you think this is a mistake, contact an administrator or switch to the appropriate account.
        </p>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
          <Link to="/" className="btn btn-secondary">
            Go to home
          </Link>
          <Link to="/" className="btn btn-primary">
            Open dashboard
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Unauthorized;

