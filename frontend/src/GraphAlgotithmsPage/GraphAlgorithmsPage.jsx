import { FaFilePdf } from 'react-icons/fa';
import './GraphAlgorithmsPage.css';
import { useNavigate } from 'react-router-dom';
import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";

function GraphAlgorithmPage() {
    const navigate = useNavigate();

    const [isEnrolled, setIsEnrolled] = useState(() => {
        const savedState = localStorage.getItem("isEnrolled");
        return savedState === "true";
    });

    const [showAll, setShowAll] = useState(false);

    useEffect(() => {
        localStorage.setItem("isEnrolled", isEnrolled);
    }, [isEnrolled]);

    const handleClick = (label) => {
        if (label === "Home") navigate('/dashboard');
        else if (label === "Library") navigate('/library');
        else if (label === "Explore") navigate('/explore');
        else if (label === "Profile") navigate('/profile');
    };

    const flashcards = [
        "What is a minimum spanning tree?",
        "Define a complete bipartite graph.",
        "What is an adjacency matrix?",
        "What is an adjacency matrix?",
        "Difference between Dijkstra and Bellman-Ford?"
    ];

    return (
        <div className="graph-container">
            <div className="graph-logo">
                <img src="/quizzy-logo-homepage.svg" alt="Logo" />
            </div>

            <div className="graph-box" />

            <button className="graph-icon-wrapper graph-icon-home" onClick={() => handleClick("Home")}>
                <img src="/home-logo.svg" alt="Home" className="graph-icon-image" />
                <span className="graph-icon-text">Home</span>
            </button>

            <button className="graph-icon-wrapper graph-icon-library graph-icon-active">
                <div className="graph-rectangle-active"></div>
                <img src="/library-logo.svg" alt="Library" className="graph-icon-image" />
                <div className="graph-icon-text">Library</div>
            </button>

            <button className="graph-icon-wrapper graph-icon-explore" onClick={() => handleClick("Explore")}>
                <img src="/explore-logo.svg" alt="Explore" className="graph-icon-image" />
                <span className="graph-icon-text">Explore</span>
            </button>

            <button className="graph-icon-wrapper graph-icon-profile" onClick={() => handleClick("Profile")}>
                <img src="/profile-logo.svg" alt="Profile" className="graph-icon-image" />
                <span className="graph-icon-text">Profile</span>
            </button>

            <div className="graph-logo-fii">
                <img src="/logo-fac-homepage.svg" alt="FII Logo" />
            </div>

            <div className="graph-content-box">
                <div className="graph-header">
                    <h1 className="graph-title">Graph Algorithms</h1>

                    {!isEnrolled ? (
                        <button
                            className="graph-enroll-button"
                            onClick={() => setIsEnrolled(true)}
                        >
                            Enroll ►
                        </button>
                    ) : (
                        <div className="graph-enrolled-actions">
                            <button
                                className="graph-unenroll-button"
                                onClick={() => setIsEnrolled(false)}
                            >
                                Unenroll ►
                            </button>
                            <Link to="/graph-theory-learning">
                                <button className="graph-start-learning-button">
                                    Start learning ►
                                </button>
                            </Link>
                        </div>
                    )}
                </div>

                <div className="graph-divider"></div>

                <div className="graph-flashcards">
                    <h2 className="graph-section-title">Flashcards</h2>
                    <h2 className="graph-flashcard-count">6</h2>

                    {!showAll ? (
                        <div className="graph-flashcards-scroll">
                            {flashcards.map((flashcard, index) => (
                                <div key={index} className="graph-flashcard">
                                    <h3>{flashcard}</h3>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="graph-flashcards-grid">
                            {flashcards.map((flashcard, index) => (
                                <div key={index} className="graph-flashcard-show-all">
                                    <h3>{flashcard}</h3>
                                </div>
                            ))}
                        </div>
                    )}

                    <button
                        className="show-all-button"
                        onClick={() => setShowAll(!showAll)}
                    >
                        {showAll ? "Show less" : "Show all"}
                    </button>
                </div>

                <div className="graph-divider"></div>

                <div className="graph-files">
                    <div className="graph-file-header">
                        <h2 className="graph-section-title">Files</h2>
                        <h2 className="graph-file-count">6</h2>
                    </div>

                    {[1, 2, 3].map((i) => (
                        <div key={i}>
                            <div className="graph-file-entry">
                                <FaFilePdf size={40} color="#E74C3C" />
                                <div className="graph-file-text">
                                    <p className="graph-file-name">{`Agr${i}`}</p>
                                    <p className="graph-file-details">{`${30 + i * 2} pages | ${3 + i} flashcards`}</p>
                                </div>
                            </div>
                            {i < 3 && <div className="graph-divider-small"></div>}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default GraphAlgorithmPage;
