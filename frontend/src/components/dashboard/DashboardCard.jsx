import React from 'react';

const DashboardCard = ({ title, value, subtitle, accent }) => {
  return (
    <div className="card" style={{ borderTop: `4px solid ${accent || '#4f46e5'}` }}>
      <h3 className="card-title">{title}</h3>
      <div style={{ fontSize: '2rem', fontWeight: 600, marginTop: '0.5rem' }}>{value}</div>
      {subtitle && (
        <p className="text-muted" style={{ marginTop: '0.25rem' }}>
          {subtitle}
        </p>
      )}
    </div>
  );
};

export default DashboardCard;

