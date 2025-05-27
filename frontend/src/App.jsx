import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import FlashcardsProf from './FlashcardProf/FlashcardsProf';
import CoursePage from './CoursePage/CoursePage';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/courses/:courseId" element={<CoursePage />} />
                <Route path="/flashcardsprof/:courseId/:profId" element={<FlashcardsProf />} />
                </Routes>
        </Router>
    );
}

export default App;