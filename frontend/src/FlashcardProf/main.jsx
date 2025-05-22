import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'; // adăugat Navigate
import Login from './Login';
import Dashboard from './Dashboard';
import Profile from './Profile';
import Library from './Library/Library.jsx';
import GraphAlgorithmsPage from "./GraphAlgorithmsPage.jsx";
import Register from './Register';
import Explore from "./Explore.jsx";
import FlashcardsProf from "./FlashcardsProf.jsx";





ReactDOM.createRoot(document.getElementById('root')).render(
    <BrowserRouter>
        <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} /> {/* redirect root */}
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/library" element={<Library />} />
            <Route path="/graph-algorithms" element={<GraphAlgorithmsPage />} />
            <Route path="/register" element={<Register />} />
            <Route path="/explore" element={<Explore />} />
            <Route path="/flashcardsProf" element={<FlashcardsProf />} />



        </Routes>
    </BrowserRouter>
);
