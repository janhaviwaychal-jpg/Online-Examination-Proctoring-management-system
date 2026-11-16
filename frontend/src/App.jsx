
import { useState, useEffect } from "react";
import {
  LayoutDashboard, BookOpen, ClipboardList, User,
  Settings, LogOut, Bell, Clock, Award, FileText,
  ArrowRight, CalendarDays, ArrowLeft
} from "lucide-react";
import "./App.css";

function App() {
  const [studentId, setStudentId] = useState(
    () => localStorage.getItem("studentId")
  );

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loginError, setLoginError] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);

  const [activePage, setActivePage] = useState("Dashboard");
  const [exams, setExams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [results, setResults] = useState([]);
  const [resultsLoading, setResultsLoading] = useState(false);
  const [resultsError, setResultsError] = useState("");

  const [activeExam, setActiveExam] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [answers, setAnswers] = useState({});
  const [questionLoading, setQuestionLoading] = useState(false);
  const [questionError, setQuestionError] = useState("");
  const [submitted, setSubmitted] = useState(false);

  const [submissionResult, setSubmissionResult] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submissionError, setSubmissionError] = useState("");

  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [darkMode, setDarkMode] = useState(false);

  // Login function
  const handleLogin = async (e) => {
    e.preventDefault();

    setLoginError("");
    setLoginLoading(true);

    try {
      const response = await fetch(
        "http://localhost:8080/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            email: loginEmail,
            password: loginPassword
          })
        }
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Invalid email or password");
      }

      if (!data.studentId) {
        throw new Error("Student ID was not received from server");
      }

      localStorage.setItem("studentId", String(data.studentId));

      setStudentId(String(data.studentId));
      setLoginPassword("");
      setActivePage("Dashboard");

    } catch (error) {
      setLoginError(error.message);
    } finally {
      setLoginLoading(false);
    }
  };

  // Logout function
  const handleLogout = () => {
    localStorage.removeItem("studentId");

    setStudentId(null);
    setLoginEmail("");
    setLoginPassword("");
    setLoginError("");

    setActivePage("Dashboard");

    setActiveExam(null);
    setQuestions([]);
    setAnswers({});
    setCurrentQuestion(0);
    setSubmitted(false);
    setSubmissionResult(null);
    setSubmissionError("");
    setSubmitting(false);

    setResults([]);
    setResultsError("");
    setQuestionError("");
  };

  // Fetch available exams
  useEffect(() => {
    fetch("http://localhost:8080/api/exams")
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch exams");
        }
        return response.json();
      })
      .then((data) => {
        setExams(data);
        setLoading(false);
      })
      .catch((error) => {
        setError(error.message);
        setLoading(false);
      });
  }, []);

  // Fetch results of logged-in student
  useEffect(() => {
    if (!studentId) return;

    if (
      activePage !== "My Results" &&
      activePage !== "Dashboard"
    ) {
      return;
    }

    let cancelled = false;

    setResultsLoading(true);
    setResultsError("");

    fetch(
      `http://localhost:8080/api/results/student/${studentId}`
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("Unable to fetch results");
        }
        return response.json();
      })
      .then((data) => {
        if (!cancelled) {
          setResults(data);
          setResultsLoading(false);
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setResultsError(error.message);
          setResultsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [activePage, studentId]);

  const menuItems = [
    { name: "Dashboard", icon: LayoutDashboard },
    { name: "Available Exams", icon: BookOpen },
    { name: "My Results", icon: ClipboardList },
    { name: "My Profile", icon: User }
  ];

  // Start examination
  const startExam = async (exam) => {
    setQuestionLoading(true);
    setQuestionError("");
    setSubmissionError("");
    setSubmissionResult(null);

    setActiveExam(exam);
    setQuestions([]);
    setAnswers({});
    setCurrentQuestion(0);
    setSubmitted(false);

    try {
      const response = await fetch(
        `http://localhost:8080/api/exams/${exam.exam_id}/questions`
      );

      if (!response.ok) {
        throw new Error("Unable to load exam questions");
      }

      const data = await response.json();

      if (data.length === 0) {
        throw new Error("No questions found for this exam");
      }

      setQuestions(data);

    } catch (error) {
      setQuestionError(error.message);
    } finally {
      setQuestionLoading(false);
    }
  };

  // Select answer
  const selectAnswer = (option) => {
    setAnswers((previousAnswers) => ({
      ...previousAnswers,
      [questions[currentQuestion].question_id]: option
    }));
  };

  // Submit examination and save result
  const finishExam = async () => {
    if (submitting || submitted) return;

    setSubmitting(true);
    setSubmissionError("");

    try {
      const response = await fetch(
        `http://localhost:8080/api/exams/${activeExam.exam_id}/submit`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            studentId: Number(studentId),
            answers: answers
          })
        }
      );

      const data = await response.json().catch(() => ({}));

      if (!response.ok || !data.success) {
        throw new Error(
          data.message || "Exam submission failed"
        );
      }

      setSubmissionResult(data);
      setSubmitted(true);

    } catch (error) {
      setSubmissionError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  // Return to dashboard
  const backToDashboard = () => {
    setActiveExam(null);
    setQuestions([]);
    setAnswers({});
    setCurrentQuestion(0);
    setSubmitted(false);
    setSubmissionResult(null);
    setSubmissionError("");
    setSubmitting(false);
    setActivePage("Dashboard");
  };

  const optionLabels = ["A", "B", "C", "D"];

  // Display examination cards
  const renderExamCards = () => (
    <div className="exam-grid">
      {loading && <p>Loading exams...</p>}

      {error && <p>Unable to load exams: {error}</p>}

      {!loading && !error && exams.length === 0 && (
        <p>No examinations available.</p>
      )}

      {!loading && !error && exams.map((exam) => (
        <div className="exam-card" key={exam.exam_id}>
          <div className="exam-card-top">
            <div className="exam-icon">
              <BookOpen />
            </div>
            <span className="available">Available</span>
          </div>

          <p className="subject">
            {exam.description || "Examination"}
          </p>

          <h3>{exam.exam_title}</h3>

          <div className="exam-details">
            <span>
              <Clock size={15} /> {exam.duration_minutes} Minutes
            </span>

            <span>
              <Award size={15} /> {exam.total_marks} Marks
            </span>
          </div>

          <p className="question-count">
            {exam.total_questions} Questions
          </p>

          <button
            className="start-button"
            onClick={() => startExam(exam)}
          >
            Start Exam <ArrowRight size={16} />
          </button>
        </div>
      ))}
    </div>
  );

  // Display pages
  const renderPage = () => {

    // Dashboard
    if (activePage === "Dashboard") {
      return (
        <>
          <section className="welcome">
            <div>
              <p className="welcome-label">STUDENT PORTAL</p>
              <h1>Welcome back, Student! 👋</h1>
              <p>
                Ready to test your knowledge? Let's get started.
              </p>
            </div>

            <div className="welcome-icon">
              <BookOpen size={65} />
            </div>
          </section>

          <section className="stats">
            <div className="stat-card">
              <div className="stat-icon purple">
                <FileText />
              </div>
              <div>
                <p>Total Exams</p>
                <h2>{exams.length}</h2>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon green">
                <Award />
              </div>
              <div>
                <p>Completed Exams</p>
                <h2>{results.length}</h2>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon orange">
                <Clock />
              </div>
              <div>
                <p>Upcoming Exams</p>
                <h2>0</h2>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon blue">
                <ClipboardList />
              </div>
              <div>
                <p>Average Score</p>
                <h2>
                  {results.length > 0
                    ? (
                        results.reduce(
                          (sum, result) =>
                            sum +
                            (
                              Number(result.total_marks) > 0
                                ? Number(result.marks_obtained) /
                                  Number(result.total_marks) * 100
                                : 0
                            ),
                          0
                        ) / results.length
                      ).toFixed(1) + "%"
                    : "0%"}
                </h2>
              </div>
            </div>
          </section>

          <section className="section">
            <div className="section-heading">
              <div>
                <h2>Available Examinations</h2>
                <p>Choose an examination to begin.</p>
              </div>

              <button
                className="view-button"
                onClick={() => setActivePage("Available Exams")}
              >
                View all <ArrowRight size={16} />
              </button>
            </div>

            {renderExamCards()}
          </section>

          <section className="section recent-section">
            <div className="section-heading">
              <div>
                <h2>Recent Examination</h2>
                <p>Your latest examination activity.</p>
              </div>
            </div>

            {results.length > 0 ? (
              <div className="recent-card">
                <div className="recent-icon">
                  <Award />
                </div>

                <div className="recent-info">
                  <h3>{results[0].exam_title}</h3>
                  <p>
                    <CalendarDays size={14} /> Recently completed
                  </p>
                </div>

                <div className="recent-score">
                  <strong>
                    {results[0].marks_obtained}/
                    {results[0].total_marks}
                  </strong>
                  <span>Exam Score</span>
                </div>

                <span className="result-status">Completed</span>
              </div>
            ) : (
              <div className="exam-card">
                <p>No completed examinations found.</p>
              </div>
            )}
          </section>
        </>
      );
    }

    // Available Exams
    if (activePage === "Available Exams") {
      return (
        <section className="section">
          <div className="section-heading">
            <div>
              <h2>Available Examinations</h2>
              <p>Select an examination to start.</p>
            </div>
          </div>

          {renderExamCards()}
        </section>
      );
    }

    // My Results
    if (activePage === "My Results") {
      return (
        <section className="section">
          <h2>My Results</h2>
          <p>Your examination results from the database.</p>

          {resultsLoading && <p>Loading results...</p>}

          {resultsError && (
            <p>Unable to load results: {resultsError}</p>
          )}

          {!resultsLoading &&
            !resultsError &&
            results.length === 0 && (
              <div className="exam-card">
                <ClipboardList size={35} />
                <h3>No results recorded yet</h3>
                <p>
                  Complete and submit an examination to see
                  your result here.
                </p>
              </div>
            )}

          {!resultsLoading &&
            !resultsError &&
            results.map((result) => (
              <div className="exam-card" key={result.result_id}>
                <h3>{result.exam_title}</h3>

                <p>
                  Score: {result.marks_obtained} / {result.total_marks}
                </p>

                <p>
                  Percentage: {
                    Number(result.total_marks) > 0
                      ? (
                          Number(result.marks_obtained) /
                          Number(result.total_marks) * 100
                        ).toFixed(2)
                      : 0
                  }%
                </p>

                <p>Attempt ID: {result.attempt_id}</p>
                <p>Evaluated At: {result.evaluated_at}</p>
              </div>
            ))}
        </section>
      );
    }

    // My Profile
    if (activePage === "My Profile") {
      return (
        <section className="section">
          <h2>My Profile</h2>

          <div className="exam-card">
            <div className="avatar">S</div>
            <h3>Student</h3>
            <p>Student ID: {studentId}</p>
            <p>Account Type: Student</p>
            <p>Portal: ExamPro</p>
            <p>Examinations Available: {exams.length}</p>
          </div>
        </section>
      );
    }

    // Settings
    if (activePage === "Settings") {
      return (
        <section className="section">
          <h2>Settings</h2>
          <p>Manage your examination portal preferences.</p>

          <div className="exam-card">
            <h3>Account Settings</h3>
            <p>Account Type: Student</p>
            <p>Portal: ExamPro</p>
            <p>Student ID: {studentId}</p>
          </div>

          <div className="exam-card">
            <h3>Notification Settings</h3>
            <p>Choose whether you want notifications enabled.</p>

            <label className="exam-option">
              <input
                type="checkbox"
                checked={notificationsEnabled}
                onChange={(e) =>
                  setNotificationsEnabled(e.target.checked)
                }
              />
              Enable notifications
            </label>
          </div>

          <div className="exam-card">
            <h3>Appearance Settings</h3>

            <label className="exam-option">
              <input
                type="checkbox"
                checked={darkMode}
                onChange={(e) => setDarkMode(e.target.checked)}
              />
              Dark mode preference
            </label>

            <p>
              This preference is currently stored only while
              the page is open.
            </p>
          </div>

          <div className="exam-card">
            <h3>Privacy</h3>
            <p>
              Your examination information is managed by the portal.
            </p>
          </div>
        </section>
      );
    }

    return <p>Page not found.</p>;
  };

  // Login screen
  if (!studentId) {
    return (
      <div className="app">
        <div
          className="exam-card"
          style={{
            maxWidth: "400px",
            margin: "100px auto",
            padding: "30px"
          }}
        >
          <div className="logo">
            <div className="logo-icon">
              <BookOpen />
            </div>
            <div>
              <h2>Exam<span>Pro</span></h2>
              <small>Examination Portal</small>
            </div>
          </div>

          <h2 style={{ marginTop: "25px" }}>
            Welcome Back!
          </h2>

          <p>Login to your student account.</p>

          <form onSubmit={handleLogin}>
            <div style={{ marginBottom: "18px" }}>
              <label>Email Address</label>

              <input
                type="email"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                placeholder="Enter your email"
                required
                style={{
                  width: "100%",
                  padding: "12px",
                  marginTop: "8px",
                  boxSizing: "border-box"
                }}
              />
            </div>

            <div style={{ marginBottom: "18px" }}>
              <label>Password</label>

              <input
                type="password"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                placeholder="Enter your password"
                required
                style={{
                  width: "100%",
                  padding: "12px",
                  marginTop: "8px",
                  boxSizing: "border-box"
                }}
              />
            </div>

            {loginError && (
              <p style={{ color: "red" }}>
                {loginError}
              </p>
            )}

            <button
              type="submit"
              className="start-button"
              disabled={loginLoading}
              style={{ width: "100%" }}
            >
              {loginLoading ? "Logging in..." : "Login"}
            </button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="app">

      {/* Sidebar */}
      <aside className="sidebar">
        <div className="logo">
          <div className="logo-icon">
            <BookOpen />
          </div>

          <div>
            <h2>Exam<span>Pro</span></h2>
            <small>Examination Portal</small>
          </div>
        </div>

        <p className="menu-title">MENU</p>

        <nav>
          {menuItems.map((item) => {
            const Icon = item.icon;

            return (
              <button
                key={item.name}
                className={`nav-item ${
                  activePage === item.name && !activeExam
                    ? "active"
                    : ""
                }`}
                onClick={() => {
                  if (!activeExam) {
                    setActivePage(item.name);
                  }
                }}
              >
                <Icon size={19} />
                {item.name}
              </button>
            );
          })}
        </nav>

        <div className="sidebar-bottom">
          <button
            className={`nav-item ${
              activePage === "Settings" && !activeExam
                ? "active"
                : ""
            }`}
            onClick={() => {
              if (!activeExam) {
                setActivePage("Settings");
              }
            }}
          >
            <Settings size={19} /> Settings
          </button>

          <button
            className="nav-item"
            onClick={handleLogout}
          >
            <LogOut size={19} /> Logout
          </button>

          <div className="profile-mini">
            <div className="avatar">S</div>

            <div>
              <strong>Student</strong>
              <small>Student Account</small>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main">
        <header className="topbar">
          <div>
            <h3>
              {activeExam ? activeExam.exam_title : activePage}
            </h3>
            <p>Online Examination Portal</p>
          </div>

          <div className="top-actions">
            <button className="icon-button">
              <Bell size={20} />
            </button>

            <div className="avatar">S</div>
          </div>
        </header>

        {activeExam ? (
          <section className="section">

            {!submitted && (
              <button
                className="view-button"
                onClick={backToDashboard}
              >
                <ArrowLeft size={16} /> Exit Exam
              </button>
            )}

            {questionLoading && <p>Loading questions...</p>}

            {questionError && (
              <div>
                <p>{questionError}</p>

                <button
                  className="start-button"
                  onClick={backToDashboard}
                >
                  Back to Dashboard
                </button>
              </div>
            )}

            {!questionLoading &&
              !questionError &&
              submitted && (
                <div className="exam-card">
                  <h2>🎉 Exam Submitted Successfully!</h2>

                  <p>
                    Your answers have been saved in the database.
                  </p>

                  <div style={{ margin: "25px 0" }}>
                    <h3>Your Result</h3>

                    <h1 style={{ fontSize: "40px" }}>
                      {submissionResult?.marksObtained} / {submissionResult?.totalMarks}
                    </h1>

                    <p>
                      Percentage: {
                        Number(submissionResult?.totalMarks) > 0
                          ? (
                              Number(submissionResult.marksObtained) /
                              Number(submissionResult.totalMarks) * 100
                            ).toFixed(2)
                          : "0.00"
                      }%
                    </p>

                    <p>
                      Attempt ID: {submissionResult?.attemptId}
                    </p>
                  </div>

                  <button
                    className="start-button"
                    onClick={backToDashboard}
                  >
                    Back to Dashboard
                  </button>
                </div>
              )}

            {!questionLoading &&
              !questionError &&
              !submitted &&
              questions.length > 0 && (
                <div className="exam-card">

                  <div className="section-heading">
                    <div>
                      <h2>{activeExam.exam_title}</h2>
                      <p>
                        Question {currentQuestion + 1} of {questions.length}
                      </p>
                    </div>

                    <span className="available">
                      {activeExam.duration_minutes} Minutes
                    </span>
                  </div>

                  <div className="question-count">
                    Progress: {Object.keys(answers).length} of
                    {questions.length} answered
                  </div>

                  <h3>
                    {questions[currentQuestion].question_text}
                  </h3>

                  <p>
                    Marks: {questions[currentQuestion].marks}
                  </p>

                  <div className="exam-options">
                    {optionLabels.map((option) => {
                      const optionKey =
                        `option_${option.toLowerCase()}`;

                      return (
                        <label
                          key={option}
                          className="exam-option"
                        >
                          <input
                            type="radio"
                            name={`question-${questions[currentQuestion].question_id}`}
                            value={option}
                            checked={
                              answers[
                                questions[currentQuestion].question_id
                              ] === option
                            }
                            onChange={() => selectAnswer(option)}
                          />

                          <strong>{option}.</strong>

                          {questions[currentQuestion][optionKey]}
                        </label>
                      );
                    })}
                  </div>

                  {submissionError && (
                    <p style={{ color: "red", marginTop: "15px" }}>
                      {submissionError}
                    </p>
                  )}

                  <div className="exam-navigation">
                    <button
                      className="view-button"
                      disabled={currentQuestion === 0 || submitting}
                      onClick={() =>
                        setCurrentQuestion(currentQuestion - 1)
                      }
                    >
                      Previous
                    </button>

                    {currentQuestion < questions.length - 1 ? (
                      <button
                        className="start-button"
                        disabled={submitting}
                        onClick={() =>
                          setCurrentQuestion(currentQuestion + 1)
                        }
                      >
                        Next Question <ArrowRight size={16} />
                      </button>
                    ) : (
                      <button
                        className="start-button"
                        disabled={
                          submitting ||
                          Object.keys(answers).length !== questions.length
                        }
                        onClick={finishExam}
                      >
                        {submitting ? "Submitting..." : "Submit Exam"}
                      </button>
                    )}
                  </div>
                </div>
              )}
          </section>
        ) : (
          <>
            {renderPage()}

            <footer>
              © 2026 ExamPro | Online Examination and Proctoring System
            </footer>
          </>
        )}
      </main>
    </div>
  );
}

export default App;
