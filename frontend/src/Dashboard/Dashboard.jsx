import React, { useEffect, useState } from 'react';
import './Dashboard.css';
import { useNavigate } from 'react-router-dom';
import { api } from '../utils/api';
import BurgerMenu from '../components/BurgerMenu/BurgerMenu';

const Dashboard = () => {
    const navigate = useNavigate();
    const [streak, setStreak] = useState(null);

    const username = localStorage.getItem('user') || '';
    // Extract first name from email address or username
    const extractFirstName = (userStr) => {
        if (!userStr) return '';
        
        // If it's an email, get the part before @
        const emailPart = userStr.includes('@') ? userStr.split('@')[0] : userStr;
        
        // Get the first part before any dot or underscore
        const firstName = emailPart.split(/[._]/)[0];
        
        // Capitalize first letter and make rest lowercase
        return firstName.charAt(0).toUpperCase() + firstName.slice(1).toLowerCase();
    };
    
    const prenume = extractFirstName(username);
    const userId = localStorage.getItem('userId');

    const currentDate = new Date();
    const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    const dayName = daysOfWeek[currentDate.getDay()];
    const day = currentDate.getDate();
    const month = months[currentDate.getMonth()];

    useEffect(() => {
        if (userId) {
            api.get(`/users/streak/latest?userId=${userId}`)
                .then((res) => {
                    if (res.data && typeof res.data.currentStreak === 'number') {
                        setStreak(res.data.currentStreak);
                    } else {
                        setStreak(1);
                    }
                })
                .catch((err) => {
                    console.error('Eroare la preluarea streak-ului:', err);
                    setStreak(1);
                });
        } else {
            setStreak(0);
        }
    }, [userId]);

    const handleClick = (label) => {
        navigate(`/${label.toLowerCase()}`);
    };    return (
        <div className="dashboard-wrapper">
            {/* Burger Menu for tablet and mobile */}
            <BurgerMenu currentPage="dashboard" />
            
            <header className="dashboard-header">
                {/* Header can be used for other elements if needed */}
            </header>

            <main className="dashboard-main">
                {/* Left sidebar with Quizzy logo and navigation */}
                <aside className="dashboard-sidebar left">
                    <img src="/quizzy-logo-homepage.svg" alt="Quizzy Logo" className="sidebar-logo" />
                    <div className="sidebar-navigation">
                        {['Home', 'Library', 'Explore', 'Profile'].map((item) => (
                            <button
                                key={item}
                                className={`sidebar-button ${item === 'Home' ? 'active' : ''}`}
                                onClick={() => handleClick(item)}
                            >
                                <img src={`/${item.toLowerCase()}-logo.svg`} alt={item} className="icon" />
                                <span>{item}</span>
                            </button>
                        ))}
                    </div>
                </aside>

                <section className="dashboard-content">
                    <div className="welcome">
                        <img src="/bufnita.svg" alt="Bufnita" className="owl" />
                        <h1>Hello, {prenume}</h1>
                    </div>

                    <div className="top-bar">
                        <div className="date-info">
                            <span>🗓️ {dayName}, {day} {month}</span>
                            <span className="streak">🔥 {streak}</span>
                        </div>
                    </div>

                    <hr />

                    <div className="week-days">
                        {Array.from({ length: 5 }).map((_, index) => {
                            const newDate = new Date();
                            newDate.setDate(currentDate.getDate() + index);
                            const weekDay = daysOfWeek[newDate.getDay()];
                            const weekDayDate = newDate.getDate();
                            const weekDayMonth = months[newDate.getMonth()];
                            const isToday = newDate.toDateString() === currentDate.toDateString();

                            return (
                                <div className={`day-card ${isToday ? 'active' : ''}`} key={index}>
                                    <div className="day-top"></div>
                                    <div className="day-body">
                                        <h3>{weekDay}</h3>
                                        <p>{weekDayDate} {weekDayMonth}</p>
                                    </div>
                                </div>
                            );                        })}
                    </div>                </section>

                {/* Right sidebar with FII logo */}
                <aside className="dashboard-sidebar right">
                    <img src="/logo-fac-homepage.svg" alt="FII Logo" className="sidebar-fii-logo" />
                </aside>
            </main>
        </div>
    );
};

export default Dashboard;
