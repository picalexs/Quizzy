# Quizzy

Quizzy is a web application designed to help students prepare for their bachelor final exams. This platform provides interactive learning through AI-generated quizzes and flashcards tailored to the course material. Students can also access course PDFs directly from the site, making it a comprehensive study companion.

---

## Features

- **AI-Generated Quizzes:** Personalized quizzes to test and reinforce your knowledge.
- **AI-Generated Flashcards:** Quickly review key concepts with automatically created flashcards.
- **Course Material Access:** Download and view your course PDFs anytime.
- **User-Friendly Interface:** Modern, responsive design for an optimal learning experience.

---

## Screenshots

<!-- Add your screenshots here, for example: -->
![Screenshot 1](path/to/screenshot1.png)
![Screenshot 2](path/to/screenshot2.png)

---

## Technologies Used

- **Backend:** Java SpringBoot
- **Frontend:** Vite + React
- **Containerization:** Docker
- **CI/CD:** GitHub Actions
- **Cloud Infrastructure:** Amazon AWS  
  (Services: EC2, S3, ECR, RDS, CloudWatch, and more)

---

## DevOps & Cloud Architecture

As part of the DevOps team, I contributed to designing and implementing the cloud architecture and essential services for Quizzy. Our infrastructure leverages several AWS services:

- **EC2:** For scalable compute resources.
- **S3:** For file and asset storage, including course PDFs.
- **ECR:** To store and manage Docker container images.
- **RDS:** For reliable and scalable database management.
- **CloudWatch:** For monitoring, logging, and alerting.

We used **Docker** for containerizing both frontend and backend applications, ensuring consistent deployment environments and scalability.

Our CI/CD pipeline is managed with **GitHub Actions**:
- **Continuous Integration:** Runs Maven JUnit unit tests to ensure code quality.
- **Continuous Deployment:** On successful builds and pushes to the `main` branch, a custom script automatically deploys the application to AWS.

---

# Setup Docker

## Instalare

- **Windows**: Instalează [Docker Desktop](https://www.docker.com/products/docker-desktop) și pornește-l.
- **macOS**: Instalează [Docker Desktop](https://www.docker.com/products/docker-desktop) și pornește-l.
- **Linux**: Instalează Docker Engine și Docker Compose ([ghid aici](https://docs.docker.com/engine/install/)).


## Cum rulezi proiectul

1. Asigură-te că Docker este pornit.
2. Deschide un terminal.
3. Mergi în folderul principal al proiectului (cel care conține fișierul `docker-compose.yml`).
4. Rulează comanda:

   ```bash
   docker compose up --build -d
   ```

Aceasta va porni automat:
- baza de date
- backend-ul (disponibil pe [http://localhost:3000](http://localhost:3000))
- frontend-ul (disponibil pe [http://localhost:36882](http://localhost:36882))

Pentru a opri serviciile:

```bash
docker compose down
```

Pentru a opri serviciile si pentru a reseta baza de date:

```bash
docker compose down --volumes
```
