import { Link, Route, Routes } from "react-router-dom";
import { DevicesPage } from "./pages/DevicesPage";
import { DeviceDetailPage } from "./pages/DeviceDetailPage";

export default function App() {
  return (
    <div className="app">
      <header className="app__header">
        <Link to="/" className="app__brand">
          BCS · Network Device Monitor
        </Link>
      </header>
      <main className="app__main">
        <Routes>
          <Route path="/" element={<DevicesPage />} />
          <Route path="/devices/:deviceId" element={<DeviceDetailPage />} />
        </Routes>
      </main>
    </div>
  );
}
