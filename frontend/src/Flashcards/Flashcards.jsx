import React, { useState, useEffect } from 'react';
import './Flashcards.css';
import { useNavigate } from 'react-router-dom';

const Flashcards = () => {
    const navigate = useNavigate();

    const flashcards = [
        {
            question: "What is a minimum spanning tree?",
            options: [
                " A flying donkey",
                " 24",
                " A spanning tree with the minimum possible total edge weight.",
                " Belgium"
            ],
            correctAnswer: " A spanning tree with the minimum possible total edge weight."
        },
        {
            question: "What is Big O notation?",
            answer: "Big O describes the upper bound of an algorithm's time or space complexity as input size grows."
        },
        {
            question: "What is recursion in programming?",
            answer: "Recursion is when a function calls itself to solve smaller instances of the same problem."
        },
        {
            question: "What is the difference between stack and queue?",
            answer: "Stack is LIFO (last in, first out); Queue is FIFO (first in, first out)."
        },
        {
            question: "What is a hash table?",
            answer: "A hash table maps keys to values using a hash function to compute an index into an array."
        }
    ];

    // Colors for the ratings: good, neutral, bad, default
    const ratingColors = {
        0: "#2D852D",  // Good (😊) - Green
        1: "#E2A54D",  // Neutral (😐) - Yellow/Orange
        2: "#940A00",  // Bad (😡) - Red
        "-1": "#CCCCCC" // Default - Gray (not rated)
    };

    const [index, setIndex] = useState(0);
    const [showAnswer, setShowAnswer] = useState(false);
    const [selectedOption, setSelectedOption] = useState(null);
    const [feedbackMessage, setFeedbackMessage] = useState(null);
    const [ratings, setRatings] = useState(Array(flashcards.length).fill(-1));
    const [showKeyboardInput, setShowKeyboardInput] = useState(false);
    const [inputText, setInputText] = useState('');
    // State pentru detectarea dispozitivului mobil
    const [isMobile, setIsMobile] = useState(false);

    const current = flashcards[index];

    // Adăugăm un efect pentru a detecta dimensiunea ecranului
    useEffect(() => {
        const checkScreenSize = () => {
            setIsMobile(window.innerWidth <= 480);
        };

        // Verifică inițial
        checkScreenSize();

        // Adaugă un listener pentru redimensionarea ferestrei
        window.addEventListener('resize', checkScreenSize);

        // Curățare la demontare
        return () => {
            window.removeEventListener('resize', checkScreenSize);
        };
    }, []);

    const nextCard = () => {
        if (index < flashcards.length - 1) {
            setIndex(index + 1);
            setShowAnswer(false);
            setSelectedOption(null);
            setFeedbackMessage(null);
            setShowKeyboardInput(false);
        }
    };

    const prevCard = () => {
        if (index > 0) {
            setIndex(index - 1);
            setShowAnswer(false);
            setSelectedOption(null);
            setFeedbackMessage(null);
            setShowKeyboardInput(false);
        }
    };

    const handleOptionSelect = (option) => {
        if (showAnswer) return;

        setSelectedOption(option);
        setShowAnswer(true);

        if (option === current.correctAnswer) {
            setFeedbackMessage("Correct!");
        } else {
            setFeedbackMessage("Wrong!");
        }
    };

    const handleFeedback = (type, ratingIndex) => {
        const newRatings = [...ratings];
        newRatings[index] = ratingIndex;
        setRatings(newRatings);
    };

    const resetRatings = () => {
        setRatings(Array(flashcards.length).fill(-1));
        setIndex(0);
        setShowAnswer(false);
        setSelectedOption(null);
        setFeedbackMessage(null);
        setShowKeyboardInput(false);
    };

    const toggleKeyboardInput = () => {
        setShowKeyboardInput(!showKeyboardInput);
        if (showKeyboardInput) {
            setInputText('');
        }
    };

    const handleInputChange = (e) => {
        setInputText(e.target.value);
    };

    const navigateBack = () => {
        navigate('/graph-algorithms');
    };

    return (
        <div className="flashcard-app">
            <div className="flashcard-container">
                <div className="top-bar">
                    <div className="top-bar-inner">
                        <button className="back-icon" onClick={navigateBack}>&lt;</button>
                    </div>
                </div>

                <div className="flashcard">
                    <div className="progress-bar-container">
                        <div className="progress-bar">
                            {flashcards.map((_, i) => (
                                <div
                                    key={i}
                                    className="progress-segment"
                                    style={{
                                        backgroundColor: ratingColors[ratings[i]],
                                        opacity: i === index ? 1 : 0.8
                                    }}
                                />
                            ))}
                        </div>
                    </div>

                    <div className="flashcard-question">{current.question}</div>

                    {current.options ? (
                        <div className="flashcard-options-container">
                            <div className="instruction-text">Select 1 correct answer</div>

                            <div className="flashcard-options">
                                {current.options.map((option, i) => {
                                    const isCorrect = showAnswer && option === current.correctAnswer;
                                    const isWrong = showAnswer && option !== current.correctAnswer;

                                    return (
                                        <div
                                            key={i}
                                            onClick={() => handleOptionSelect(option)}
                                            className={`option-card ${isCorrect ? 'correct' : ''} ${isWrong && showAnswer ? 'incorrect' : ''} ${selectedOption === option && !showAnswer ? 'selected' : ''}`}
                                        >
                                            {option}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    ) : (
                        <>
                            <div className="flashcard-content">
                                {showAnswer && <hr className={`answer-divider ${isMobile ? 'mobile-divider' : ''}`} />}

                                {showAnswer ? (
                                    <div className={`flashcard-answer centered ${isMobile ? 'mobile-answer' : ''}`}>{current.answer}</div>
                                ) : (
                                    <div className="answer-button-container">
                                        <button
                                            className="show-answer-btn"
                                            onClick={() => setShowAnswer(true)}
                                        >
                                            Show Answer
                                        </button>
                                    </div>
                                )}
                            </div>
                        </>
                    )}

                    <div className="keyboard-icon-container" onClick={toggleKeyboardInput}>
                        <span className="keyboard-icon">⌨️</span>
                    </div>

                    {showKeyboardInput && (
                        <div className="keyboard-input-container">
                            <input
                                type="text"
                                className="keyboard-input"
                                value={inputText}
                                onChange={handleInputChange}
                                placeholder="Type the answer"
                                autoFocus
                                style={{ marginLeft: isMobile ? '40px' : '60px', width: isMobile ? 'calc(100% - 50px)' : 'calc(100% - 75px)' }}
                            />
                        </div>
                    )}

                    {showAnswer && feedbackMessage && (
                        <div className={`feedback-message ${feedbackMessage === "Correct!" ? "correct-feedback" : "incorrect-feedback"}`}>
                            {feedbackMessage}
                        </div>
                    )}
                </div>

                <div className="rating-container">
                    <button className="nav-btn-circle" onClick={prevCard}>&lt;</button>
                    <button className="rating-btn" onClick={() => handleFeedback('bad', 2)}>😡</button>
                    <button className="rating-btn" onClick={() => handleFeedback('neutral', 1)}>😐</button>
                    <button className="rating-btn" onClick={() => handleFeedback('good', 0)}>😊</button>
                    <button className="nav-btn-circle" onClick={nextCard}>&gt;</button>
                </div>
            </div>
        </div>
    );
};

export default Flashcards;