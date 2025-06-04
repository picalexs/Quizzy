import React, { useState, useEffect } from 'react';
import './Flashcards.css';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { api } from '../utils/api';

const Flashcards = () => {
    const navigate = useNavigate();
    const { materialId } = useParams(); // dacă dorești să preiei flashcards după materialId din URL
    const location = useLocation();
    const { courseId, courseTitle, startingFlashcardId } = location.state || {};
    const userId = localStorage.getItem('userId');

    // State pentru flashcards
    const [flashcards, setFlashcards] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Statele existente
    const [index, setIndex] = useState(0);
    const [showAnswer, setShowAnswer] = useState(false);
    const [selectedOption, setSelectedOption] = useState(null);
    const [feedbackMessage, setFeedbackMessage] = useState(null);
    const [ratings, setRatings] = useState([]);
    const [showKeyboardInput, setShowKeyboardInput] = useState(false);
    const [inputText, setInputText] = useState('');
    const [isMobile, setIsMobile] = useState(false);
    const [score, setScore] = useState(null);
    const [selectedOptions, setSelectedOptions] = useState([]);
    const [streakUpdated, setStreakUpdated] = useState(false);

    const ratingColors = {
        0: "#2D852D",  // Good (😊) - Green
        1: "#E2A54D",  // Neutral (😐) - Yellow/Orange
        2: "#940A00",  // Bad (😡) - Red
        "-1": "#CCCCCC" // Default - Gray (not rated)
    };

    // Preluăm flashcards prioritizate de la API
    useEffect(() => {
        const fetchFlashcards = async () => {
            try {
                setLoading(true);
                let response;

                // Numărul de flashcards care vor fi încărcate
                const limitCards = 20;
                console.log('Încărcare flashcards prioritizate...');

                // Obținem ID-ul utilizatorului din localStorage
                const userId = localStorage.getItem('userId');
                if (!userId) {
                    console.error('Nu s-a găsit ID-ul utilizatorului');
                    setError('Nu s-a găsit ID-ul utilizatorului. Vă rugăm să vă autentificați.');
                    setLoading(false);
                    return;
                }

                // Verificăm dacă avem materialId pentru a face cererea corespunzătoare
                if (materialId) {
                    // Folosim noul endpoint pentru flashcards prioritizate după material și utilizator
                    console.log(`Încărcare ${limitCards} flashcards prioritizate pentru materialul ${materialId} și utilizatorul ${userId}`);
                    response = await api.get(`/Flashcard/prioritized/material/${materialId}/user/${userId}?limit=${limitCards}`);
                } else {
                    // Dacă nu avem materialId, luăm flashcards prioritizate după utilizator
                    console.log(`Încărcare ${limitCards} flashcards prioritizate pentru utilizatorul ${userId}`);
                    response = await api.get(`/Flashcard/prioritized/user/${userId}?limit=${limitCards}`);
                }

                // Procesăm datele primite pentru a le face compatibile cu componenta
                const processedFlashcards = response.data.map(card => {
                    // Procesăm răspunsurile
                    const processedCard = {
                        id: card.id,
                        question: card.question,
                        level: card.level,
                        lastStudiedAt: card.lastStudiedAt,
                        questionType: card.questionType
                    };

                    if (card.questionType === 'Multiple') {
                        const options = card.answers.map(answer => answer.optionText);
                        const correctAnswer = card.answers.find(answer => answer.correct)?.optionText;
                        processedCard.options = options;
                        processedCard.correctAnswer = correctAnswer;
                    } else {
                        // Caută fie .text, fie .optionText
                        processedCard.answer = card.answers.find(answer => answer.correct)?.text ||
                            card.answers.find(answer => answer.correct)?.optionText ||
                            card.answers[0]?.text ||
                            card.answers[0]?.optionText ||
                            "No answer provided";
                    }

                    return processedCard;
                });

                setFlashcards(processedFlashcards);
                setRatings(Array(processedFlashcards.length).fill(-1));
                setLoading(false);
            } catch (err) {
                console.error('Error fetching flashcards:', err);
                setError('Failed to load flashcards. Please try again later.');
                setLoading(false);
            }
        };

        fetchFlashcards();
    }, [materialId]);

    // Set starting flashcard index if provided
    useEffect(() => {
        if (startingFlashcardId && flashcards.length > 0) {
            const startingIndex = flashcards.findIndex(card => card.id === startingFlashcardId);
            if (startingIndex !== -1) {
                setIndex(startingIndex);
            }
        }
    }, [startingFlashcardId, flashcards]);

    // Detectăm dispozitivul mobil (cod existent)
    useEffect(() => {
        const checkScreenSize = () => {
            setIsMobile(window.innerWidth <= 480);
        };

        checkScreenSize();
        window.addEventListener('resize', checkScreenSize);

        return () => {
            window.removeEventListener('resize', checkScreenSize);
        };
    }, []);

    const current = flashcards[index];

    const nextCard = () => {
        if (index < flashcards.length - 1) {
            setIndex(index + 1);
            setShowAnswer(false);
            setSelectedOption(null);
            setSelectedOptions([]);
            setFeedbackMessage(null);
            setShowKeyboardInput(false);
            setScore(null);
            setInputText('');
        }
    };

    const prevCard = () => {
        if (index > 0) {
            setIndex(index - 1);
            setShowAnswer(false);
            setSelectedOption(null);
            setSelectedOptions([]);
            setFeedbackMessage(null);
            setShowKeyboardInput(false);
            setScore(null);
            setInputText('');
        }
    };

    const handleOptionSelect = async (option) => {
        if (showAnswer) return;

        setSelectedOptions(prev => {
            if (prev.includes(option)) {
                return prev.filter(item => item !== option);
            } else {
                return [...prev, option];
            }
        });
    };

    const checkAndUpdateStreak = async () => {
        if (streakUpdated) return; // nu actualiza de mai multe ori pe sesiune
        try {
            await api.post(`/users/streak/check?userId=${userId}`, null, { params: { userId } });
            console.log("Streak actualizat");
            setStreakUpdated(true);
        } catch (err) {
            console.error("Eroare la actualizarea streak-ului:", err);
        }
    };


    const handleCheckAnswers = async () => {
        if (showAnswer) return;

        setShowAnswer(true);

        const correctAnswers = Array.isArray(current.correctAnswer)
            ? current.correctAnswer
            : [current.correctAnswer];

        const isAllCorrect =
            selectedOptions.length === correctAnswers.length &&
            selectedOptions.every(option => correctAnswers.includes(option));

        if (isAllCorrect) {
            setFeedbackMessage("Correct!");
            handleFeedback('good', 0);
            checkAndUpdateStreak();
        } else {
            setFeedbackMessage("Wrong!");
            handleFeedback('bad', 2);
        }

        // Trimitem răspunsul către backend pentru a actualiza progresul
        if (current && current.id) {
            try {
                // Pentru răspunsurile cu opțiuni multiple, trimitem direct la selectare
                // quality: 5 pentru corect, 1 pentru greșit
                const quality = isAllCorrect ? 5 : 1;

                console.log(`Trimitere răspuns pentru flashcard ID ${current.id}, quality: ${quality}, isCorrect: ${isAllCorrect}`);

                const response = await api.post('/Flashcard/response', {
                    flashcardId: current.id,
                    userId: localStorage.getItem('userId'),
                    quality: quality,
                    isCorrect: isAllCorrect
                });

                console.log('Răspunsul a fost înregistrat cu succes:', response.data);
            } catch (error) {
                console.error('Eroare la trimiterea răspunsului:', error);
            }
        }

        setTimeout(() => {
            nextCard();
        }, 1500);
    };

    const handleFeedback = async (type, ratingIndex) => {
        // Actualizăm starea locală
        const newRatings = [...ratings];
        newRatings[index] = ratingIndex;
        setRatings(newRatings);

        // Trimitem răspunsul către backend pentru a actualiza progresul
        if (current && current.id) {
            try {
                console.log(`Trimitere răspuns pentru flashcard ID ${current.id}, quality: ${4 - ratingIndex}`);

                // Convertim ratingIndex (0=bun, 1=neutru, 2=rău) la quality (5=perfect, 3=greu, 1=foarte greu)
                // în algoritmul SM-2, quality este între 0-5, unde 0-2 = eșec, 3-5 = succes
                const quality = 4 - ratingIndex; // 0 -> 4, 1 -> 3, 2 -> 2
                const isCorrect = ratingIndex <= 1; // Considerăm răspunsurile bune și neutre ca fiind corecte

                // Trimitem răspunsul către backend
                const response = await api.post('/Flashcard/response', {
                    flashcardId: current.id,
                    userId: localStorage.getItem('userId'),
                    quality: quality,
                    isCorrect: isCorrect
                });

                console.log('Răspunsul a fost înregistrat cu succes:', response.data);
            } catch (error) {
                console.error('Eroare la trimiterea răspunsului:', error);
            }
        }
    };

    const resetRatings = () => {
        setRatings(Array(flashcards.length).fill(-1));
        setIndex(0);
        setShowAnswer(false);
        setSelectedOption(null);
        setSelectedOptions([]);
        setFeedbackMessage(null);
        setShowKeyboardInput(false);
        setScore(null);
        setInputText('');
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

    // NOUĂ: Funcție pentru gestionarea tastei Enter
    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSubmitInput();
        }
    };

    // ACTUALIZATĂ: Funcția handleSubmitInput cu auto-hide mai rapid
    const handleSubmitInput = async () => {
        if (!inputText.trim()) return;

        setShowAnswer(true);
        console.log('Sending:', {
            question: current.question,
            officialAnswer: current.answer,
            usersAnswer: inputText
        });

        try {
            const res = await api.post('/gemini/compare-users-answer-to-the-official-answer',  {

                question: current.question,
                officialAnswer: current.answer,
                usersAnswer: inputText,
                flashcardId: current.id,
                userId: userId
            });

            const scoreValue = res.data;
            setScore(scoreValue);

            let autoRating;
            if (scoreValue >= 80) {
                autoRating = 0;
            } else if (scoreValue >= 50) {
                autoRating = 1;
            } else {
                autoRating = 2;
            }
            if (autoRating === 0) {
                checkAndUpdateStreak(); // <-- Aici
            }


            handleFeedback('auto', autoRating);

            // Hide keyboard input immediately
            setShowKeyboardInput(false);
            setInputText('');

            // Move to next card after showing the score
            setTimeout(() => {
                nextCard();
            }, 1500);

        } catch (err) {
            console.error('Comparison error:', err);
            setFeedbackMessage("Error comparing your answer");

            // Hide keyboard input immediately on error too
            setShowKeyboardInput(false);
            setInputText('');
        }
    };

    const navigateBack = () => {
        if (courseId) {
            navigate(`/course/${courseId}`);
        } else {
            // Fallback to library if no course ID is provided
            navigate('/library');
        }
    };

    if (loading) return <div className="flashcard-app"><div className="loading">Loading flashcards...</div></div>;
    if (error) return <div className="flashcard-app"><div className="error">{error}</div></div>;
    if (flashcards.length === 0) return <div className="flashcard-app"><div className="no-flashcards">No flashcards available.</div></div>;

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
                                    const isSelected = selectedOptions.includes(option);
                                    const correctAnswers = Array.isArray(current.correctAnswer)
                                        ? current.correctAnswer
                                        : [current.correctAnswer];
                                    const isCorrectAnswer = correctAnswers.includes(option);

                                    // Determine the class names based on state
                                    let className = 'option-card';

                                    if (showAnswer) {
                                        // When answer is shown
                                        if (isCorrectAnswer) {
                                            className += ' correct';
                                        } else {
                                            className += ' incorrect';
                                        }
                                        // Add selected class if this was one of user's choices
                                        if (isSelected) {
                                            className += ' selected';
                                        }
                                    } else {
                                        // Before answer is shown, only show selection
                                        if (isSelected) {
                                            className += ' selected';
                                        }
                                    }

                                    return (
                                        <div
                                            key={i}
                                            onClick={() => handleOptionSelect(option)}
                                            className={className}
                                        >
                                            {option}
                                        </div>
                                    );
                                })}
                            </div>

                            {!showAnswer && selectedOptions.length > 0 && (
                                <button
                                    className="show-answer-btn"
                                    onClick={handleCheckAnswers}
                                    style={{ marginTop: '1rem' }}
                                >
                                    Check Answers
                                </button>
                            )}
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
                                        onKeyPress={handleKeyPress}
                                        placeholder="Type the answer"
                                        autoFocus
                                    />
                                    <button className="submit-answer-btn" onClick={handleSubmitInput}>
                                        Submit
                                    </button>
                                </div>
                            )}

                            {score !== null && (
                                <div className="feedback-message">
                                    Your answer is {score}% correct
                                </div>
                            )}
                        </>
                    )}

                    {showAnswer && feedbackMessage && (
                        <div className={`feedback-message ${feedbackMessage === "Correct!" ? "correct-feedback" : "incorrect-feedback"}`}>
                            {feedbackMessage}
                        </div>
                    )}
                </div>

                <div className="rating-container">
                    <button className="nav-btn-circle" onClick={prevCard}>&lt;</button>
                    {/* Pentru întrebările multiple choice, butoanele de rating sunt doar vizuale
                        deoarece feedback-ul se aplică automat */}
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