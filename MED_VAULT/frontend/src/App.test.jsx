import React from 'react';

function App() {
    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
            <h1>✅ Frontend is Working!</h1>
            <p>Vite dev server is running correctly.</p>
            <p>Backend endpoint: <a href="http://localhost:8081">http://localhost:8081</a></p>
            <button onClick={() => alert('Button works!')}>Test Button</button>
        </div>
    );
}

export default App;
