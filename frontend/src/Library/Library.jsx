import React, { useState, useEffect } from 'react';
import './Library.css'; 
import BurgerMenu from '../components/BurgerMenu/BurgerMenu'; 
import { useNavigate, useLocation } from 'react-router-dom';
import { api } from '../utils/api';

const Explore = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const currentPath = location.pathname;

    // Get user information from localStorage
    const userRole = localStorage.getItem('userRole');
    const userId = localStorage.getItem('userId');

    useEffect(() => {
        const fetchCourses = async () => {
            setLoading(true);
            setError(null);
            try {
                let response;
                
                // Check user role and fetch appropriate courses
                if (userRole === 'student' && userId) {
                    // For students, fetch only enrolled courses
                    response = await api.get(`/enrollments/student/${userId}`);
                } else if (userRole === 'profesor' && userId) {
                    // For professors, fetch courses they teach
                    // TODO: This endpoint needs to be implemented in the backend
                    response = await api.get(`/courses/professor/${userId}`);
                } else {
                    // Fallback to all courses if role/userId not available
                    response = await api.get('/courses');
                }
                
                setCourses(response.data);
            } catch (err) {
                // If student endpoint returns 204 (no content), set empty array
                if (err.response?.status === 204) {
                    setCourses([]);
                } else {
                    setError(err.response?.data?.message || err.message);
                }
            } finally {
                setLoading(false);
            }
        };
        fetchCourses();
    }, [userRole, userId]);

    const handleClick = (label) => { 
        if (label === "Home") {
            navigate('/dashboard');
        } else if (label === "Profile") {
            navigate('/profile');
        } else if (label === "Explore") {
            navigate('/explore');
        } else if (label === "Library") {
            navigate('/library');
        }
    };

    const getActiveClass = (route) => {
        return currentPath === route ? 'active' : '';
    };

    return (
        <>
            {}
            <BurgerMenu currentPage={location.pathname} />

            <div className="explore-container">
                {}
                <div className="explore-logo">
                    <img src="/quizzy-logo-homepage.svg" alt="Logo" style={{ width: '100%', height: '100%' }} />
                </div>

                {}
                <div className="explore-logo-fii">
                    <img src="/logo-fac-homepage.svg" alt="FII" style={{ width: '100%', height: '100%' }} />
                </div>

                {}
                
                <div className="explore-inner-box" /> 
                {/* <div className="explore-white-box" /> */}


                {}
                <button
                    className={`explore-icon-wrapper explore-icon-home ${getActiveClass('/dashboard')}`}
                    onClick={() => handleClick("Home")}
                >
                    {getActiveClass('/dashboard') && <div className="explore-rectangle-home"></div>}
                    <img src="/home-logo.svg" alt="Home" className="explore-icon-image" />
                    <div className="explore-icon-text">Home</div>
                </button>
                <button
                    className={`explore-icon-wrapper explore-icon-library ${getActiveClass('/library')}`}
                    onClick={() => handleClick("Library")}
                >
                    {getActiveClass('/library') && <div className="explore-rectangle-home"></div>}
                    <img src="/library-logo.svg" alt="Library" className="explore-icon-image" />
                    <span className="explore-icon-text">Library</span>
                </button>
                <button
                    className={`explore-icon-wrapper explore-icon-explore ${getActiveClass('/explore')}`}
                    onClick={() => handleClick("Explore")}
                >
                    {getActiveClass('/explore') && <div className="explore-rectangle-home"></div>}
                    <img src="/explore-logo.svg" alt="Explore" className="explore-icon-image" />
                    <span className="explore-icon-text">Explore</span>
                </button>
                <button
                    className={`explore-icon-wrapper explore-icon-profile ${getActiveClass('/profile')}`}
                    onClick={() => handleClick("Profile")}
                >
                    {getActiveClass('/profile') && <div className="explore-rectangle-home"></div>}
                    <img src="/profile-logo.svg" alt="Profile" className="explore-icon-image" />
                    <span className="explore-icon-text">Profile</span>
                </button>                {/* Cards Section */}
                <div className="library-cards-container"> 
                    {loading ? (
                        <div className="library-loading">Loading courses...</div>
                    ) : error ? (
                        <div className="library-error">Error: {error}</div>
                    ) : courses.length === 0 ? (
                        <div className="library-empty">
                            {userRole === 'student' 
                                ? "You are not enrolled in any courses yet" 
                                : userRole === 'profesor'
                                ? "You are not teaching any courses yet"
                                : "No courses available"
                            }
                        </div>
                    ) : (
                        courses.map((course, index) => (
                            <div
                                key={course.id || index}
                                className="library-card" // Recomand redenumirea în explore-card
                                style={{ cursor: 'pointer' }}
                                onClick={() => navigate(`/course/${course.id}`, { state: { course } })}
                            >
                                <div className="library-card-header" /> {/* explore-card-header */}
                                <div className="library-card-header-text"> {/* explore-card-content */}
                                    <div className="library-course-title">{course.title}</div> {/* explore-course-title */}
                                    <div className="library-course-info"> {/* explore-course-info */}
                                        <span className="library-number">{course.flashcardCount || 0}</span> Flashcards |
                                        <span className="library-number">{course.materialCount || 0}</span> Files
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </>
    );
};

export default Explore;